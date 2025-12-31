package com.nyonyix.thermia.util;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.data.SolarShadeResult;
import com.nyonyix.thermia.data.WindOcclusionResult;
import com.nyonyix.thermia.data.map.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.nyonyix.thermia.data.BlockSearchResult;

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
        private Map<BlockPos, Float> blockExposures = new HashMap<>();
        private List<BlockPositionsWithDistance> allFound = new ArrayList<>();

        record BlockPositionsWithDistance(BlockPos pos, Block block, double distSq, float occlusion) {}

        void addBlockCandidate(BlockPos pos, Block block, double distSq, float exposure)
        {
            allFound.add(new BlockPositionsWithDistance(pos.immutable(), block, distSq, exposure));
        }

        BlockSearchResult build(Level level)
        {
            allFound.sort(Comparator.comparingDouble(BlockPositionsWithDistance::distSq));

            Map<Block, Integer> tempCounts = new HashMap<>();

            for (BlockPositionsWithDistance entry : allFound)
            {
                BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.block()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
                if (dataMap == null) continue;

                int currentCount = tempCounts.getOrDefault(entry.block, 0);
                if (currentCount >= dataMap.searchCap()) continue;

                tempCounts.put(entry.block(), currentCount + 1);
                allPositions.computeIfAbsent(entry.block(), k -> new ArrayList<>()).add(entry.pos());

                blockExposures.put(entry.pos(), entry.occlusion());
            }

            return new BlockSearchResult(origin, levelID, allPositions, blockExposures);
        }
    }

    private static void searchChunk(LevelChunk chunk, Vec3 origin, int radiusSq, BlockSearchBuilder builder, List<LevelChunk> chunks)
    {
        LevelChunkSection[] sections = chunk.getSections();
        BlockPos chunkPos = chunk.getPos().getWorldPosition();

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
                        BlockPos pos = new BlockPos(chunkPos.getX() + x, sectionY + y, chunkPos.getZ() + z);
                        double distSq = origin.distanceToSqr(Vec3.atCenterOf(pos));
                        if (distSq > radiusSq) continue;

                        Block block = section.getBlockState(x, y, z).getBlock();
                        BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);

                        if (dataMap != null)
                        {
                            float exposure = calcExposure(origin, pos.immutable(), chunks);
                            builder.addBlockCandidate(pos.immutable(), block, distSq, exposure);
                        }
                    }
                }
            }
        }
    }

private static float calcExposure(Vec3 origin, BlockPos sourcePos, List<LevelChunk> cachedChunks)
{
    float totalExposure = 0f;

    for (Direction direction : Direction.values())
    {
        BlockPos adjacentPos = sourcePos.relative(direction);
        LevelChunk adjacentChunk = findChunkInList(cachedChunks, adjacentPos.getX() >> 4, adjacentPos.getZ() >> 4);
        if (adjacentChunk == null) continue;

        BlockState adjacentState = adjacentChunk.getBlockState(adjacentPos);
        if (adjacentState.canOcclude()) continue;

        Vec3 faceNormal = Vec3.atLowerCornerOf(direction.getNormal());
        Vec3 faceCenter = Vec3.atCenterOf(sourcePos).add(faceNormal.scale(0.5));
        Vec3 toEntity = origin.subtract(faceCenter).normalize();

        double dot = faceNormal.dot(toEntity);
        if (dot <= 0) continue;

        if (!hasLineOfSight(faceCenter, origin, sourcePos, cachedChunks)) continue;

        totalExposure += (float) dot;
    }

    return totalExposure;
}

    private static boolean hasLineOfSight(Vec3 from, Vec3 to, BlockPos sourcePos, List<LevelChunk> cachedChunks)
    {
        BlockPos hitPos = raycastToPoint(from, to, cachedChunks);

        return hitPos == null || hitPos.equals(sourcePos);
    }

    private static BlockPos raycastToPoint(Vec3 from, Vec3 to, List<LevelChunk> cachedChunks)
    {
        BlockGetter blockGetter = new BlockGetter() {

            @Override
            public BlockState getBlockState(BlockPos blockPos)
            {
                LevelChunk chunk = findChunkInList(cachedChunks, blockPos.getX() >> 4, blockPos.getZ() >> 4);
                return chunk != null ? chunk.getBlockState(blockPos) : Blocks.AIR.defaultBlockState();
            }

            @Override
            public FluidState getFluidState(BlockPos blockPos)
            {
                return getBlockState(blockPos).getFluidState();
            }

            @Override
            public int getHeight()
            {
                return cachedChunks.isEmpty() ? 384 : cachedChunks.getFirst().getHeight();
            }

            @Override
            public int getMinBuildHeight()
            {
                return cachedChunks.isEmpty() ? -64 : cachedChunks.getFirst().getMinBuildHeight();
            }

            @Override
            public @Nullable BlockEntity getBlockEntity(BlockPos blockPos)
            {
                return null;
            }
        };

        final BlockPos[] hitBlock = {null};

        BlockGetter.traverseBlocks(from, to, blockGetter, (getter, pos) -> {

            BlockState state = getter.getBlockState(pos);

            if (!state.isAir())
            {
                hitBlock[0] = pos.immutable();
                return  pos;
            }

            return null;
        }, (getter) -> null);

        return hitBlock[0];
    }

    private static LevelChunk findChunkInList(List<LevelChunk> chunks, int chunkX, int chunkZ)
    {
        for (LevelChunk chunk : chunks)
        {
            if (chunk.getPos().x == chunkX && chunk.getPos().z == chunkZ) return chunk;
        }

        return null;
    }

    private static float getBlockTransparency(BlockState state)
    {
        if (state.is(BlockTags.LEAVES)) return 0.5f;
        if (!state.isCollisionShapeFullBlock(null, BlockPos.ZERO)) return 0.6f;
        if (!state.canOcclude()) return 0.7f;
        return 0.3f;
    }

    public static CompletableFuture<BlockSearchResult> searchAllAsync(Level level, Vec3 origin, int radius)
    {
        final BlockPos centerBlockPos = BlockPos.containing(origin);
        final int chunkRadius = (radius >> 4 ) + 1;
        final int radiusSq = radius * radius;
        final int centerChunkX = centerBlockPos.getX() >> 4;
        final int centerChunkZ = centerBlockPos.getZ() >> 4;

        final List<LevelChunk> chunksToSearch = new ArrayList<>();
        for (int cx = centerChunkX - chunkRadius; cx <= centerChunkX + chunkRadius; cx++)
        {
            for (int cz = centerChunkZ - chunkRadius; cz <= centerChunkZ + chunkRadius; cz++)
            {
                if (level.hasChunk(cx, cz))
                {
                    chunksToSearch.add(level.getChunk(cx, cz));
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

                for (LevelChunk chunk : chunksToSearch)
                {
                    searchChunk(chunk, origin, radiusSq, builder, chunksToSearch);
                }

                return builder.build(level);
            }catch (Exception e)
            {
                LOGGER.error("Error in async block search:", e);
                return new BlockSearchResult(Vec3.ZERO, level.dimension(), new HashMap<>(), new HashMap<>());
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

        double distance = level.canSeeSky(pos) ? 6.0 : 12.0;

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