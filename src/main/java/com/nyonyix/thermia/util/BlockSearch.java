package com.nyonyix.thermia.util;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.data.records.ExposedFaces;
import com.nyonyix.thermia.data.records.SolarShadeResult;
import com.nyonyix.thermia.data.records.WindOcclusionResult;
import com.nyonyix.thermia.data.datamap.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.FluidTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.slf4j.Logger;

import com.nyonyix.thermia.data.records.BlockSearchResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BlockSearch
{
    private static final Logger LOGGER = LogUtils.getLogger();

    static class BlockSearchBuilder
    {
        private Vec3 origin = Vec3.ZERO;
        private ResourceKey<Level> levelID;
        private Map<Block, List<BlockPos>> allPositions = new HashMap<>();
        private Map<Fluid, List<BlockPos>> allFluidPositions = new HashMap<>();
        private List<BlockPositionsWithDistance> allFound = new ArrayList<>();
        private List<FluidPositionsWithDistance> allFoundFluid = new ArrayList<>();
        private Map<BlockPos, ExposedFaces> exposedFaces = new HashMap<>();

        record BlockPositionsWithDistance(long pos, Block block, double distSq) {}
        record FluidPositionsWithDistance(long pos, Fluid fluid, double distSq) {}

        void addBlockCandidate(BlockPos pos, Block block, double distSq) {allFound.add(new BlockPositionsWithDistance(pos.asLong(), block, distSq));}

        void addFluidCandidate(BlockPos pos, Fluid fluid, double distSq) {allFoundFluid.add(new FluidPositionsWithDistance(pos.asLong(), fluid, distSq));}

        void addExposedFaces(BlockPos pos, ExposedFaces faces) {exposedFaces.put(pos.immutable(), faces);}

        BlockSearchResult build(Level level)
        {
            allFound.sort(Comparator.comparingDouble(BlockPositionsWithDistance::distSq));
            allFoundFluid.sort(Comparator.comparingDouble(FluidPositionsWithDistance::distSq));

            Map<Block, Integer> tempCounts = new HashMap<>();

            for (BlockPositionsWithDistance entry : allFound)
            {
                BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.block()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);

                int currentCount = tempCounts.getOrDefault(entry.block, 0);
                if (currentCount >= dataMap.searchCap()) continue;

                tempCounts.put(entry.block(), currentCount + 1);
                allPositions.computeIfAbsent(entry.block(), k -> new ArrayList<>()).add(BlockPos.of(entry.pos));
            }

            Map<Fluid, Integer> tempCountsFluid = new HashMap<>();

            for (FluidPositionsWithDistance entry : allFoundFluid)
            {
                FluidTemperatureDataMap dataMap = BuiltInRegistries.FLUID.wrapAsHolder(entry.fluid()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);

                int currentCount = tempCountsFluid.getOrDefault(entry.fluid(), 0);
                if (currentCount >= dataMap.searchCap()) continue;

                tempCountsFluid.put(entry.fluid(), currentCount + 1);
                allFluidPositions.computeIfAbsent(entry.fluid(), k -> new ArrayList<>()).add(BlockPos.of(entry.pos));
            }

            return new BlockSearchResult(origin, levelID, allPositions, allFluidPositions, exposedFaces);
        }
    }

    private static void searchChunk(LevelChunk chunk, Vec3 origin, int radiusSq, BlockSearchBuilder builder, Long2ObjectOpenHashMap<LevelChunk> chunks)
    {
        LevelChunkSection[] sections = chunk.getSections();
        BlockPos chunkPos = chunk.getPos().getWorldPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos exposedFaceAdjacentPos = new BlockPos.MutableBlockPos();

        for (int sectionIdX = 0; sectionIdX < sections.length; sectionIdX++)
        {
            LevelChunkSection section = sections[sectionIdX];
            if (section == null || section.hasOnlyAir()) continue;

            int sectionY = chunk.getMinBuildHeight() + (sectionIdX * 16);

            for (int x = 0; x < 16; x++)
            {
                for (int z = 0; z < 16; z++)
                {
                    for (int y = 0; y < 16; y++)
                    {
                        pos.set(chunkPos.getX() + x, sectionY + y, chunkPos.getZ() + z);
                        double distSq = origin.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        if (distSq > radiusSq) continue;

                        BlockState state = section.getBlockState(x, y, z);
                        Block block = state.getBlock();
                        BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);

                        if (dataMap != null)
                        {
                            if (dataMap.isRadiative())
                            {
                                ExposedFaces exposedFaces = getExposedFaces(exposedFaceAdjacentPos, pos, chunks);

                                if (!exposedFaces.isEmpty())
                                {
                                    builder.addBlockCandidate(pos.immutable(), block, distSq);
                                    builder.addExposedFaces(pos.immutable(), exposedFaces);
                                }
                            }
                            else builder.addBlockCandidate(pos.immutable(), block, distSq);
                        }

                        FluidState fluidState = state.getFluidState();
                        if (!fluidState.isEmpty())
                        {
                            Fluid fluid = fluidState.getType();
                            FluidTemperatureDataMap fluidDataMap = BuiltInRegistries.FLUID.wrapAsHolder(fluid).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);

                            if (fluidDataMap != null)
                            {
                                if (fluidDataMap.isRadiative())
                                {
                                    ExposedFaces exposedFaces = getExposedFaces(exposedFaceAdjacentPos, pos, chunks);

                                    if (!exposedFaces.isEmpty())
                                    {
                                        builder.addFluidCandidate(pos.immutable(), fluid, distSq);
                                        builder.addExposedFaces(pos.immutable(), exposedFaces);
                                    }
                                }
                                else builder.addFluidCandidate(pos.immutable(), fluid, distSq);
                            }
                        }
                    }
                }
            }
        }
    }

    private static LevelChunk findChunk(Long2ObjectOpenHashMap<LevelChunk> chunkMap, BlockPos pos)
    {
        return chunkMap.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
    }

    private static ExposedFaces getExposedFaces(BlockPos.MutableBlockPos adjacentPos, BlockPos pos, Long2ObjectOpenHashMap<LevelChunk> cachedChunks)
    {
        List<Direction> exposedDirections = new ArrayList<>();

        for (Direction dir : Direction.values())
        {
            adjacentPos.setWithOffset(pos, dir);
            LevelChunk chunk = findChunk(cachedChunks, adjacentPos);
            if (chunk == null) continue;

            if (!chunk.getBlockState(adjacentPos).canOcclude()) exposedDirections.add(dir);
        }

        return new ExposedFaces(exposedDirections);
    }

    public static CompletableFuture<BlockSearchResult> searchAllAsync(Level level, Vec3 origin, int radius)
    {
        final BlockPos centerBlockPos = BlockPos.containing(origin);
        final Long2ObjectOpenHashMap<LevelChunk> chunksToSearch = new Long2ObjectOpenHashMap<>();
        final int chunkRadius = (radius >> 4 ) + 1;
        final int radiusSq = radius * radius;
        final int centerChunkX = centerBlockPos.getX() >> 4;
        final int centerChunkZ = centerBlockPos.getZ() >> 4;

        for (int cx = centerChunkX - chunkRadius; cx <= centerChunkX + chunkRadius; cx++)
        {
            for (int cz = centerChunkZ - chunkRadius; cz <= centerChunkZ + chunkRadius; cz++)
            {
                if (level.hasChunk(cx, cz))
                {
                    chunksToSearch.put(ChunkPos.asLong(cx, cz), level.getChunk(cx, cz));
                }
            }
        }

        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                BlockSearchBuilder builder =  new BlockSearchBuilder();
                builder.levelID = level.dimension();
                builder.origin = origin;

                for (LevelChunk chunk : chunksToSearch.values())
                {
                    searchChunk(chunk, origin, radiusSq, builder, chunksToSearch);
                }

                return builder.build(level);
            }
            catch (Exception e)
            {
                LOGGER.error("Error in async block search:", e);
                return new BlockSearchResult(Vec3.ZERO, level.dimension(), new HashMap<>(), new HashMap<>(), new HashMap<>());
            }

        }, Util.backgroundExecutor());
    }

    public static SolarShadeResult getSolarShade(Level level, BlockPos pos, float zenith, float azimuth)
    {
        float sinZenith = (float) Math.sin(-zenith);
        float cosZenith = (float) Math.cos(-zenith);
        float sinAzimuth = (float) Math.sin(azimuth);
        float cosAzimuth = (float) Math.cos(azimuth);

        double sunDirX = sinZenith * sinAzimuth;
        double sunDirY = cosZenith;
        double sunDirZ = sinZenith * cosAzimuth;

        if (sunDirY <=0) return new SolarShadeResult(0.1f, BlockPos.ZERO);

        if (zenith < Math.PI && !level.canSeeSky(pos)) return new SolarShadeResult(0.1f, BlockPos.ZERO);

        double shadowSoftness = Mth.lerp((float) (zenith / (Math.PI / 2.0)), 2.0, 6.0);
        double horizMag = Math.sqrt(sunDirX * sunDirX + sunDirZ * sunDirZ);

        if (horizMag > 1e-4)
        {
            int maxDistance = zenith > Math.PI / 3.0 ? 200 : 100;
            int sampleInterval = zenith > Math.PI / 3.0 ? 12 : 20;

            for (int distance = sampleInterval; distance <= maxDistance; distance += sampleInterval)
            {
                int sampleX = (int) (pos.getX() + sunDirX * distance);
                int sampleZ = (int) (pos.getZ() + sunDirZ * distance);

                if (!level.getChunkSource().hasChunk(sampleX >> 4, sampleZ >> 4)) continue;

                int terrainHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING, sampleX, sampleZ);

                double t = distance / horizMag;
                double expectedHeight = pos.getY() + t * sunDirY;

                double delta = terrainHeight - expectedHeight;

                if (delta > shadowSoftness) return new SolarShadeResult(Mth.clamp((float) (1.0 - delta / 8.0), distance < 40 ? 0.15f : distance < 100 ? 0.30f : 0.50f, 0.9f), new BlockPos(sampleX, terrainHeight, sampleZ));
            }
        }

        double localRayDistance = zenith > Math.PI / 3.0 ? 32.0 : zenith > Math.PI / 6.0 ? 24.0 : 16.0;

        Vec3 startVec = Vec3.atCenterOf(pos);
        Vec3 endVec = startVec.add(sunDirX * localRayDistance, sunDirY * localRayDistance, sunDirZ * localRayDistance);
        endVec = new Vec3(endVec.x, Math.min(endVec.y, level.getMaxBuildHeight()), endVec.z);

        ClipContext context = new ClipContext(startVec, endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty());
        BlockHitResult hit = level.clip(context);

        if (hit.getType() != HitResult.Type.MISS)
        {
            BlockState state = level.getBlockState(hit.getBlockPos());

            if (!state.canOcclude() && !state.is(BlockTags.LEAVES))
            {
                return SolarShadeResult.createDefault();
            }

            double hitDist = startVec.distanceTo(hit.getLocation());

            if (level.getBlockState(hit.getBlockPos()).is(BlockTags.LEAVES))
            {
                if (hitDist < 3.0) return new SolarShadeResult(0.4f, hit.getBlockPos());
                if (hitDist < 6.0) return new SolarShadeResult(Mth.lerp((float) ((hitDist / 3.0) / 5.0), 0.4f, 0.6f), hit.getBlockPos());
                if (hitDist < 16.0) return new SolarShadeResult(Mth.lerp((float) ((hitDist - 8.0) / 8.0), 0.6f, 0.7f), hit.getBlockPos());
                return new SolarShadeResult(Mth.lerp((float) ((hitDist - 16) / (localRayDistance - 16)), 0.7f, 0.9f), hit.getBlockPos());
            }
            else
            {
                if (hitDist < 3.0) return new SolarShadeResult(0.15f, hit.getBlockPos());
                if (hitDist < 6.0) return new SolarShadeResult(Mth.lerp((float) ((hitDist / 3.0) / 5.0), 0.15f, 0.4f), hit.getBlockPos());
                if (hitDist < 16.0) return new SolarShadeResult(Mth.lerp((float) ((hitDist - 8.0) / 8.0), 0.4f, 0.7f), hit.getBlockPos());
                return new SolarShadeResult(Mth.lerp((float) ((hitDist - 16) / (localRayDistance - 16)), 0.7f, 0.9f), hit.getBlockPos());
            }
        }

        return SolarShadeResult.createDefault();
    }

    public static WindOcclusionResult getWindOcclusion(Level level, BlockPos pos)
    {
        float direction = EnvironmentHelpers.getWindDirection(level, pos);
        float directionX = -(float) Math.cos(direction);
        float directionZ = -(float) Math.sin(direction);

        double distance = EnvironmentHelpers.isExposedToSky(level, pos) ? 6.0 : 12.0;

        Vec3 startVec = Vec3.atCenterOf(pos.above());
        Vec3 endVec = startVec.add(directionX * distance, 0, directionZ * distance);

        ClipContext context = new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty());
        BlockHitResult hit = level.clip(context);

        if (hit.getType() != HitResult.Type.MISS)
        {
            float hitDist = (float) startVec.distanceTo(hit.getLocation());

            return WindOcclusionResult.createDefault().withOccludingBlock(hit.getBlockPos()).withOcclusionMultiplier(Mth.clamp((float) (0.1 + (hitDist / distance) * 0.9), 0.1f, 1.0f));
        }

        return WindOcclusionResult.createDefault();
    }

    public static int depthEncasedBlocks(LevelChunk chunk, BlockPos pos)
    {
        int totalNonEmpty = 0;
        int startSection = chunk.getSectionIndex(pos.getY());

        if (startSection < 0 || startSection >= chunk.getSectionsCount()) return 0;

        LevelChunkSection currentSection = chunk.getSection(startSection);
        int localY = pos.getY() & 15;
        int localX = pos.getX() & 15;
        int localZ = pos.getZ() & 15;

        if (!currentSection.hasOnlyAir())
        {
            for (int y = localY + 1 ; y < 16 ; y++)
            {
                if (!currentSection.getBlockState(localX, y, localZ).isAir()) totalNonEmpty++;
            }
        }

        for (int i = startSection +1 ; i < chunk.getSectionsCount() ; i++)
        {
            LevelChunkSection section = chunk.getSection(i);

            if (section.hasOnlyAir()) continue;
            totalNonEmpty += section.nonEmptyBlockCount;
        }

        return totalNonEmpty;
    }
}