package com.nyonyix.thermia.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.util.*;

public class BlockSearch
{

    private static final Logger LOGGER = LogUtils.getLogger();

    public record BlockSearchResult(BlockPos nearest, double nearestDistSq, Map<Block, Integer> counts, Map<Block, List<BlockPos>> allPositions)
    {
        public double nearestDistance() {return Math.sqrt(nearestDistSq);}

        public int getCount(Block block) {return counts.getOrDefault(block, 0);}

        public int getCount(ResourceLocation blockID) {return counts.getOrDefault(BuiltInRegistries.BLOCK.get(blockID), 0);}

        public List<BlockPos> getPositions(Block block) {return allPositions.getOrDefault(block, Collections.emptyList());}

        public List<BlockPos> getPositions(ResourceLocation blockId) {return allPositions.getOrDefault(BuiltInRegistries.BLOCK.get(blockId), Collections.emptyList());}
    }

    class BlockSearchBuilder
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

}