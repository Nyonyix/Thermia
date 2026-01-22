package com.nyonyix.thermia.util;

import com.nyonyix.thermia.Thermia;
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
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AiHelpers
{
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

    public static BlockPos findBestWarmOrCool(Level level, PathfinderMob mob,boolean isWarm, int radius)
    {
        BlockPos bestPos = null;
        BlockPos origin = mob.blockPosition();
        float bestOcclusion = isWarm ? 0f : 1f;

        int height = (int) (radius * 2) / 3;

        for (BlockPos pos : BlockPos.withinManhattan(origin, radius, height, radius))
        {
            if (!isWalkable(mob, pos)) continue;

            float windOcclusion = BlockSearch.getWindOcclusion(level, pos).occlusionMultiplier();
            boolean canSeeSky = level.canSeeSky(pos);

            if  (isWarm)
            {
                if (canSeeSky && windOcclusion > 0.75f) return pos.immutable();

                if (canSeeSky && windOcclusion > bestOcclusion)
                {
                    bestOcclusion = windOcclusion;
                    bestPos = pos.immutable();
                }
            }
            else
            {
                if (!canSeeSky && windOcclusion < 0.25f) return pos.immutable();

                if (!canSeeSky && windOcclusion < bestOcclusion)
                {
                    bestOcclusion = windOcclusion;
                    bestPos = pos.immutable();
                }
            }
        }

        return bestPos;
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
        BlockPos strongest = null;
        float maxTemp = 0f;

        for (Map.Entry<Block, List<BlockPos>> entry : blockSearchResult.allPositions().entrySet())
        {
            BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (blockDataMap == null) continue;

            for (BlockPos pos : entry.getValue())
            {
                BlockState state = mob.level().getBlockState(pos);
                float temp = BlockSearchResult.parseBlockState(state, mob.level(), pos, blockDataMap);

                if (temp <= maxTemp) continue;

                BlockPos walkablePos = findWalkableNearby(mob, pos);

                if (walkablePos != null)
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
                BlockState state = mob.level().getBlockState(pos);
                BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(state.getBlock()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
                float temp = blockDataMap == null ? fluidDataMap.temperature() : BlockSearchResult.parseBlockState(state, mob.level(), pos, blockDataMap);

                if (temp <= maxTemp) continue;

                BlockPos walkablePos = findWalkableNearby(mob, pos);

                if (walkablePos != null)
                {
                    maxTemp = temp;
                    strongest = pos.immutable();
                }
            }
        }

        return strongest;
    }

    public static BlockPos findNearestColdOrWater(PathfinderMob mob, BlockSearchResult blockSearchResult)
    {
        BlockPos mobPos = mob.blockPosition();
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Map.Entry<Block, List<BlockPos>> entry : blockSearchResult.allPositions().entrySet())
        {
            BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (blockDataMap == null || blockDataMap.temperature() > 0f) continue;

            for (BlockPos pos : entry.getValue())
            {
                if (!isWalkable(mob, pos)) continue;
                double dist = mobPos.distSqr(pos);

                if (dist < nearestDist)
                {
                    nearestDist = dist;
                    nearest = pos.immutable();
                }
            }
        }

        for (Map.Entry<Fluid, List<BlockPos>> entry : blockSearchResult.allFluidPositions().entrySet())
        {
            FluidTemperatureDataMap fluidDataMap = BuiltInRegistries.FLUID.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (fluidDataMap == null || fluidDataMap.temperature() > 0f) continue;

            for (BlockPos pos : entry.getValue())
            {
                if (!isWalkable(mob, pos)) continue;
                double dist = mobPos.distSqr(pos);

                if (dist < nearestDist)
                {
                    nearestDist = dist;
                    nearest = pos.immutable();
                }
            }
        }

        return nearest;
    }

    public static BlockPos findStrongestColdOrWater(PathfinderMob mob, BlockSearchResult blockSearchResult)
    {
        BlockPos strongest = null;
        float minTemp = 0f;

        for (Map.Entry<Block, List<BlockPos>> entry : blockSearchResult.allPositions().entrySet())
        {
            BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (blockDataMap == null || blockDataMap.temperature() > 0f) continue;

            for (BlockPos pos : entry.getValue())
            {
                if (!isWalkable(mob, pos)) continue;

                BlockState state = mob.level().getBlockState(pos);
                float temp = BlockSearchResult.parseBlockState(state, mob.level(), pos, blockDataMap);

                if (temp < minTemp)
                {
                    strongest = pos.immutable();
                    minTemp = temp;
                }
            }
        }

        for (Map.Entry<Fluid, List<BlockPos>> entry : blockSearchResult.allFluidPositions().entrySet())
        {
            FluidTemperatureDataMap fluidDataMap = BuiltInRegistries.FLUID.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (fluidDataMap == null || fluidDataMap.temperature() > 0f) continue;

            for (BlockPos pos : entry.getValue())
            {
                if (!isWalkable(mob, pos)) continue;

                BlockState state = mob.level().getBlockState(pos);
                BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(state.getBlock()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
                float temp = blockDataMap == null ? fluidDataMap.temperature() : BlockSearchResult.parseBlockState(state, mob.level(), pos, blockDataMap);

                if (temp < minTemp)
                {
                    strongest = pos.immutable();
                    minTemp = temp;
                }
            }
        }

        return strongest;
    }

    public static float[] getComfortThresholds(EntityTemperature temperatureData, float comfortThreshold)
    {
        float maxInternalTemperature = temperatureData.maxInternalTemperature();
        float minInternalTemperature = temperatureData.minInternalTemperature();
        float midPoint = (maxInternalTemperature + minInternalTemperature) * 0.5f;

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
