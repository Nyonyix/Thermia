package com.nyonyix.thermia.util;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.data.SolarShadeResult;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.slf4j.Logger;

import com.nyonyix.thermia.data.BlockSearchResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BlockSearch
{
    private static final Logger LOGGER = LogUtils.getLogger();

    static class BlockSearchBuilder
    {
        private BlockPos nearest = null;
        private double nearestDistSq = Double.MAX_VALUE;
        private Map<Block, Integer> counts = new HashMap<>();
        private Map<Block, List<BlockPos>> allPositions = new HashMap<>();
        private int totalCount = 0;

        BlockSearchResult build() {return new BlockSearchResult(nearest, nearestDistSq, counts, allPositions);}

        void initialiseBlock(Block block)
        {
            counts.put(block, 0);
            allPositions.put(block, new ArrayList<>());
        }

        void initialiseBlock(ResourceLocation blockId)
        {
            counts.put(BuiltInRegistries.BLOCK.get(blockId), 0);
            allPositions.put(BuiltInRegistries.BLOCK.get(blockId), new ArrayList<>());
        }

        void setNearest(BlockPos pos, double distSq)
        {
            if (distSq < nearestDistSq)
            {
                this.nearestDistSq = distSq;
                this.nearest = pos;
            }
        }

        void addBlock(BlockPos pos, Block block)
        {
            counts.put(block, counts.getOrDefault(block, 0) + 1);
            allPositions.computeIfAbsent(block, k -> new ArrayList<>()).add(pos);
            totalCount++;
        }

        void addBlock(BlockPos pos, ResourceLocation blockId)
        {
            counts.put(BuiltInRegistries.BLOCK.get(blockId), counts.getOrDefault(BuiltInRegistries.BLOCK.get(blockId), 0) + 1);
            allPositions.computeIfAbsent(BuiltInRegistries.BLOCK.get(blockId), k -> new ArrayList<>()).add(pos);
            totalCount++;
        }
    }

    public static class SearchForBlock
    {
        private static void searchChunk(LevelChunk chunk, BlockPos center, int radiusSq, int searchCap, Set<Block> targetBlocks, BlockSearchBuilder builder)
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

                            double distSq = center.distSqr(pos);
                            if (distSq > radiusSq) continue;

                            Block block = section.getBlockState(x, y, z).getBlock();

                            if (targetBlocks.contains(block))
                            {
                                builder.setNearest(pos.immutable(), distSq);
                                builder.addBlock(pos.immutable(), block);
                            }
                        }
                    }
                }
            }
        }

        public static BlockSearchResult searchAll(Level level, BlockPos center, int radius, int searchCap, Set<Block> targetBlocks)
        {
            BlockSearchBuilder builder = new BlockSearchBuilder();

            int chunkRadius = (radius / 16 ) + 1;
            int radiusSq = radius * radius;
            int centerChunkX = center.getX() / 16;
            int centerChunkZ = center.getZ() / 16;

            for (int cx = centerChunkX - chunkRadius; cx <= centerChunkX + chunkRadius; cx++)
            {
                for (int cz = centerChunkZ - chunkRadius; cz <= centerChunkZ + chunkRadius; cz++)
                {
                    if (!level.hasChunk(cx, cz)) continue;
                    LevelChunk chunk = level.getChunk(cx, cz);
                    searchChunk(chunk, center, radiusSq, searchCap, targetBlocks, builder);
                }
            }
            return builder.build();
        }

        public static CompletableFuture<BlockSearchResult> searchAllAsync(Level level, BlockPos center, int radius, int searchCap, Set<Block> targetBlocks)
        {
            final BlockPos searchCenter = center.immutable();
            final int chunkRadius = (radius / 16 ) + 1;
            final int radiusSq = radius * radius;
            final int centerChunkX = center.getX() / 16;
            final int centerChunkZ = center.getZ() / 16;

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

                    for (LevelChunk chunk : chunksToSearch)
                    {
                        searchChunk(chunk, searchCenter, radiusSq, searchCap, targetBlocks, builder);
                    }

                    return builder.build();
                }catch (Exception e)
                {
                    LOGGER.error("Error in async block search:", e);
                    return new BlockSearchResult(null, 0d, new HashMap<>(), new HashMap<>());
                }

            }, Util.backgroundExecutor());
        }

        public static BlockSearchResult searchAll(Level level, BlockPos center, int radius, int searchCap, Block... targetBlocks)
        {
            return searchAll(level, center, radius, searchCap, new HashSet<>(Arrays.asList(targetBlocks)));
        }

        public static CompletableFuture<BlockSearchResult> searchAllAsync(Level level, BlockPos center, int radius, int searchCap, Block... targetBlocks)
        {
            return searchAllAsync(level, center, radius, searchCap, new HashSet<>(Arrays.asList(targetBlocks)));
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

                    if (!level.getChunkSource().hasChunk(sampleX / 16, sampleZ / 16)) continue;

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

            ClipContext context = new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
            BlockHitResult hit = level.clip(context);

            if (hit.getType() != HitResult.Type.MISS)
            {
                double hitDist = startVec.distanceTo(hit.getLocation());

                if (hitDist < 3.0) return new SolarShadeResult(0.15f, hit.getBlockPos());
                if (hitDist < 6.0) return new SolarShadeResult(Mth.lerp((float) ((hitDist / 3.0) / 5.0), 0.15f, 0.4f), hit.getBlockPos());
                if (hitDist < 16.0) return new SolarShadeResult(Mth.lerp((float) ((hitDist - 8.0) / 8.0), 0.4f, 0.7f), hit.getBlockPos());
                return new SolarShadeResult(Mth.lerp((float) ((hitDist - 16) / (localRayDistance - 16)), 0.7f, 0.9f), hit.getBlockPos());
            }

            return SolarShadeResult.createDefault();
        }
    }

}