package com.nyonyix.thermia.util;

import com.nyonyix.thermia.ai.TemperatureComfortGoal;
import com.nyonyix.thermia.data.BlockSearchResult;
import com.nyonyix.thermia.data.ThermiaDamageTypes;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.datamap.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.EntityTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.FluidTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import net.minecraft.client.OptionInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathFinder;

import java.util.ArrayList;
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

        boolean hasGround = !below.getCollisionShape(mob.level(), pos.below()).isEmpty();
        boolean isPassable = at.isAir() || !at.getFluidState().isEmpty();

        return hasGround && isPassable;
    }

    public static boolean isAtPosition(PathfinderMob mob, BlockPos pos)
    {
        if (pos == null) return true;

        double distSq = mob.blockPosition().distSqr(pos);
        return distSq < 4.0;
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
            if (!level.getBlockState(pos.above(y)).isAir()) count++;
        }

        return count;
    }

    public static int evaluateShelter(Level level, BlockPos pos, Direction windFrom)
    {
        int score = 0;

        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            BlockPos adjacent = pos.relative(dir);
            if (level.getBlockState(adjacent).isSolidRender(level, adjacent)) score += dir == windFrom ? 4 : 1;
        }

        score += countBlocksAbove(level, pos);

        return score;
    }

    public static BlockPos findWalkableNearby(PathfinderMob mob, BlockPos pos)
    {
        if (isWalkable(mob, pos)) return pos.immutable();

        for (Direction dir : Direction.values())
        {
            BlockPos adjacent = pos.relative(dir);
            if (isWalkable(mob, adjacent)) return adjacent.immutable();
        }

        return null;
    }

    public static BlockPos findNearestHomeBlock(PathfinderMob mob, BlockSearchResult blockSearchResult)
    {
        BlockPos mobPos = mob.blockPosition();
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        EntityTemperatureDataMap entityDataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(mob.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
        if (entityDataMap == null || entityDataMap.homeBlock().equals(ResourceLocation.withDefaultNamespace("air"))) return nearest;

        Block homeBlock = BuiltInRegistries.BLOCK.get(entityDataMap.homeBlock());
        if (!blockSearchResult.allPositions().containsKey(homeBlock)) return nearest;

        for (BlockPos pos : blockSearchResult.allPositions().get(homeBlock))
        {
            double distSq = mobPos.distSqr(pos);

            if (isWalkable(mob, pos.above()) && distSq < nearestDist)
            {
                nearestDist = distSq;
                nearest = pos.immutable();
            }
        }

        return nearest;
    }

    public static BlockPos findNearestHeatSource(PathfinderMob mob, BlockSearchResult blockSearchResult)
    {
        BlockPos mobPos = mob.blockPosition();
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Map.Entry<Block, List<BlockPos>> entry : blockSearchResult.allPositions().entrySet())
        {
            BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (blockDataMap == null || blockDataMap.temperature() < 0f) continue;

            for (BlockPos pos : entry.getValue())
            {
                double distSq = mobPos.distSqr(pos);
                BlockPos walkablePos = findWalkableNearby(mob, pos);

                if (walkablePos != null && distSq < nearestDist)
                {
                    nearestDist = distSq;
                    nearest = pos.immutable();
                }
            }
        }

        for (Map.Entry<Fluid, List<BlockPos>> entry : blockSearchResult.allFluidPositions().entrySet())
        {
            FluidTemperatureDataMap fluidDataMap = BuiltInRegistries.FLUID.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (fluidDataMap == null || fluidDataMap.temperature() < 0f) continue;

            for (BlockPos pos : entry.getValue())
            {
                double distSq = mobPos.distSqr(pos);
                BlockPos walkablePos = findWalkableNearby(mob, pos);

                if (walkablePos != null && distSq < nearestDist)
                {
                    nearestDist = distSq;
                    nearest = pos.immutable();
                }
            }
        }

        return nearest;
    }

    public static BlockPos findStrongestHeatSource(PathfinderMob mob, BlockSearchResult blockSearchResult)
    {
        BlockPos mobPos = mob.blockPosition();
        BlockPos strongest = null;
        float maxTemp = 0f;
        double nearestDist = Double.MAX_VALUE;

        for (Map.Entry<Block, List<BlockPos>> entry : blockSearchResult.allPositions().entrySet())
        {
            BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (blockDataMap == null) continue;

            for (BlockPos pos : entry.getValue())
            {
                double distSq = mobPos.distSqr(pos);
                BlockState state = mob.level().getBlockState(pos);
                float temp = BlockSearchResult.parseBlockState(state, mob.level(), pos, blockDataMap);

                if (temp <= maxTemp) continue;

                BlockPos walkablePos = findWalkableNearby(mob, pos);

                if (walkablePos != null && distSq < nearestDist)
                {
                    maxTemp = temp;
                    strongest = pos.immutable();
                }
            }
        }

        for (Map.Entry<Fluid, List<BlockPos>> entry : blockSearchResult.allFluidPositions().entrySet())
        {
            FluidTemperatureDataMap fluidDataMap = BuiltInRegistries.FLUID.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (fluidDataMap == null) continue;

            for (BlockPos pos : entry.getValue())
            {
                double distSq = mobPos.distSqr(pos);
                BlockState state = mob.level().getBlockState(pos);
                BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(state.getBlock()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
                float temp = blockDataMap == null ? fluidDataMap.temperature() : BlockSearchResult.parseBlockState(state, mob.level(), pos, blockDataMap);

                if (temp <= maxTemp) continue;

                BlockPos walkablePos = findWalkableNearby(mob, pos);

                if (walkablePos != null && distSq < nearestDist)
                {
                    maxTemp = temp;
                    strongest = pos.immutable();
                }
            }
        }

        return strongest;
    }

    public static BlockPos findBestShade(PathfinderMob mob, int searchRange)
    {
        BlockPos mobPos = mob.blockPosition();
        Direction windFrom = getWindDirectionCardinal(mob.level(), mobPos);
        BlockPos bestShade = null;
        double bestScore = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(mobPos.offset(-searchRange, -3, -searchRange), mobPos.offset(searchRange, 3, searchRange)))
        {
            if (mob.level().canSeeSky(pos)) continue;
            if (!isWalkable(mob, pos)) continue;

            int shelterQuality = evaluateShelter(mob.level(), pos, windFrom);
            double distSq = mobPos.distSqr(pos);
            double score = distSq / (1.0 + shelterQuality * 0.3);

            if (score < bestScore)
            {
                bestScore = score;
                bestShade = pos.immutable();
            }
        }

        return bestShade;
    }

    public static BlockPos findWaterOrShade(PathfinderMob mob, int searchRange)
    {
        BlockPos mobPos = mob.blockPosition();
        BlockPos bestWater = null;
        BlockPos bestShade = null;
        double waterDist = Double.MAX_VALUE;
        double shadeDist = Double.MAX_VALUE;
        Direction windFrom = getWindDirectionCardinal(mob.level(), mobPos);

        for (BlockPos pos : BlockPos.betweenClosed(mobPos.offset(-searchRange, -3, -searchRange), mobPos.offset(searchRange, 3, searchRange)))
        {
            double distSq = mobPos.distSqr(pos);
            BlockState state = mob.level().getBlockState(pos);
            if (!state.getFluidState().isEmpty() && isWalkable(mob, pos))
            {
                if (distSq < waterDist)
                {
                    waterDist = distSq;
                    bestWater = pos.immutable();
                }
            }

            if (!mob.level().canSeeSky(pos) && isWalkable(mob, pos))
            {
                int shelterQuality = evaluateShelter(mob.level(), pos, windFrom);
                double adjustedDist = distSq / (1.0 + shelterQuality * 0.5);

                if (adjustedDist < shadeDist)
                {
                    shadeDist = adjustedDist;
                    bestShade = pos.immutable();
                }
            }
        }

        if (bestWater != null && waterDist < shadeDist * 1.5) return bestWater;
        return bestShade;
    }

    public static float[] getComfortThresholds(EntityTemperature temperatureData, float comfortThreshold)
    {
        float maxInternalTemperature = temperatureData.maxInternalTemperature();
        float minInternalTemperature = temperatureData.minInternalTemperature();
        float midPoint = (maxInternalTemperature + minInternalTemperature) * 0.5f;;

        float hotRange = maxInternalTemperature - midPoint;
        float coldRange = midPoint - minInternalTemperature;
        
        float hotThreshold = midPoint + (hotRange * (1f - comfortThreshold));
        float coldThreshold = midPoint - (coldRange * (1f - comfortThreshold));

        return new float[] {coldThreshold, hotThreshold};
    }

    public static void addTemperatureGoals(PathfinderMob mob)
    {
        boolean hasGoals = mob.goalSelector.getAvailableGoals().stream().anyMatch(g ->
                g.getGoal() instanceof TemperatureComfortGoal);

        if (hasGoals) return;

        mob.goalSelector.addGoal(3, new TemperatureComfortGoal(mob, 1.0));
    }

    public static void removeTemperatureGoals(PathfinderMob mob) {mob.goalSelector.getAvailableGoals().removeIf(g -> g.getGoal() instanceof TemperatureComfortGoal);}
}
