package com.nyonyix.thermia.ai;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.BlockSearchResult;
import com.nyonyix.thermia.data.attachment.BlockTemperature;
import com.nyonyix.thermia.data.map.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

public class AiHelpers
{
    public static Direction getWindDirectionCardinal(Level level, BlockPos pos)
    {
        float windAngle = EnvironmentHelpers.getWindDirection(level, pos);
        float degrees = (float) Math.toDegrees(windAngle);

        if (degrees >= 315 || degrees < 45) return Direction.EAST;
        if (degrees >= 45 && degrees < 135) return Direction.SOUTH;
        if (degrees >= 135 && degrees < 225) return Direction.WEST;
        return Direction.NORTH;
    }

    public static boolean isWalkable(PathfinderMob mob, BlockPos pos)
    {
        BlockState below = mob.level().getBlockState(pos.below());
        BlockState at = mob.level().getBlockState(pos);

        return !below.getCollisionShape(mob.level(), pos.below()).isEmpty() && at.isAir() && mob.getNavigation().isStableDestination(pos);
    }

    public static boolean isAtPosition(PathfinderMob mob, BlockPos pos)
    {
        if (pos == null) return true;
        return mob.blockPosition().distSqr(pos) < 4.0;
    }

    public static int countAdjacentSolidBlocks(Level level, BlockPos pos)
    {
        int count = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            BlockPos adjacent = pos.relative(dir);

            if (level.getBlockState(adjacent).isSolidRender(level, adjacent)) count++;
        }

        return count;
    }

    public static int countBlocksAbove(Level level, BlockPos pos)
    {
        int count = 0;
        for (int y = 1; y <= 3; y++)
        {
            if (!level.getBlockState(pos.above()).isAir()) count++;
        }

        return count;
    }

    public static int evaluateShelter(Level level, BlockPos pos, Direction windFrom)
    {
        int score = 0;

        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            BlockPos adjacent = pos.relative(dir);

            if (level.getBlockState(adjacent).isSolidRender(level, pos)) score += dir == windFrom ? 4 : 1;
        }

        score += countAdjacentSolidBlocks(level, pos);

        return score;
    }

    public static BlockPos findNearestHeatSource(PathfinderMob mob, BlockSearchResult blockSearchResult, int maxRange)
    {
        BlockPos mobPos = mob.blockPosition();
        BlockPos nearest = BlockPos.ZERO;
        double nearestDist = Double.MAX_VALUE;
        int maxRangeSq = maxRange * maxRange;

        for (Map.Entry<Block, List<BlockPos>> entry : blockSearchResult.allPositions().entrySet())
        {
            BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (blockDataMap == null || blockDataMap.temperature() < )
        }
    }
}
