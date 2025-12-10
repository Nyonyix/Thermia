package com.nyonyix.thermia.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.slf4j.Logger;

import com.nyonyix.thermia.data.BlockSearchResult;

import java.util.*;

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

    public class SearchForBlock
    {
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
                    LevelChunk chunk = level.getChunk(cx, cz);
                    searchChunk(chunk, center, radiusSq, searchCap, targetBlocks, builder);
                }
            }
            return builder.build();
        }

        public static BlockSearchResult searchAll(Level level, BlockPos center, int radius, int searchCap, Block... targetBlocks)
        {
            return searchAll(level, center, radius, searchCap, new HashSet<>(Arrays.asList(targetBlocks)));
        }

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
                                if (builder.allPositions.get(block).size() <= searchCap)
                                {
                                    builder.setNearest(pos.immutable(), distSq);
                                    builder.addBlock(pos.immutable(), block);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}