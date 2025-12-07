package com.nyonyix.thermia.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.slf4j.Logger;

import java.util.Set;

public class SearchForBlock
    {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static BlockPos findNearestBlock(Level level, BlockPos center, int radius, Set<Block> targetBlocks)
    {
        int chunkRadius = (radius / 16) + 1;
        int centerChunkX = center.getX() / 16;
        int centerChunkZ = center.getZ() / 16;
        int radiusSq = radius * radius;

        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (int dist = 0; dist <= chunkRadius; dist++)
        {
            for (int distX = -dist; distX <= dist; distX++)
            {
                for (int distZ = -dist; distZ <= dist; distZ++)
                {
                    if (Math.abs(distX) != dist && Math.abs(distZ) != dist) continue;

                    LevelChunk chunk = level.getChunk(centerChunkX + distX, centerChunkZ + distZ);
                    BlockPos result = searchChunk(chunk, center, radiusSq, nearestDistSq, targetBlocks);

                    if (result != null)
                    {
                        double distSq = center.distSqr(result);

                        if (distSq < nearestDistSq)
                        {
                            nearestDistSq = distSq;
                            nearest = result;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private static BlockPos searchChunk(LevelChunk chunk, BlockPos center, int maxRadiusSq, double currentBestDistSq, Set<Block> targetBlocks)
    {
        LevelChunkSection[] sections = chunk.getSections();
        BlockPos chunkPos = chunk.getPos().getWorldPosition();

        BlockPos nearest = null;
        double nearestDistSq = currentBestDistSq;

        for (int sectionsIdX = 0; sectionsIdX < sections.length; sectionsIdX++)
        {
            LevelChunkSection section = sections[sectionsIdX];
            if (section == null || section.hasOnlyAir()) continue;

            int sectionIdY = chunk.getMinBuildHeight() + (sectionsIdX * 16);

            for (int x = 0; x < 16; x++)
            {
                for (int z = 0; z < 16; z++)
                {
                    for (int y = 0; y < 16; y++)
                    {
                        BlockPos pos = new BlockPos(chunkPos.getX() + x, sectionIdY + y, chunkPos.getZ() + z);
                        double distSq = center.distSqr(pos);
                        if (distSq > maxRadiusSq || distSq >= nearestDistSq) continue;

                        BlockState state = section.getBlockState(x, y, z);

                        if (targetBlocks.contains(state.getBlock()))
                        {
                            nearestDistSq = distSq;
                            nearest = pos.immutable();
                        }
                    }
                }
            }
        }
        return nearest;
    }
}
