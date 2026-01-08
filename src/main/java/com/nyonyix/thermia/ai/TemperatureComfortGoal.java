package com.nyonyix.thermia.ai;

import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.BlockSearchResult;
import com.nyonyix.thermia.data.ClosestSource;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class TemperatureComfortGoal extends Goal
{
    private final PathfinderMob mob;
    private final double speedModifier;
    private BlockPos targetPos;
    private int cooldown = 0;

    private static final int SEARCH_RANGE = 12;
    private static final int COOLDOWN_IN_TICKS = 200;
    private static final float COMFORT_THRESHOLD = (float) ServerConfig.TEMPERATURE_BUFFER_PERCENT.getAsInt() / 100f;

    public TemperatureComfortGoal(PathfinderMob mob, double speedModifier)
    {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean isAtPosition(BlockPos pos)
    {
        if (pos == null) return true;
        return mob.blockPosition().distSqr(pos) < 4.0;
    }

    private boolean isWalkable(BlockPos pos)
    {
        BlockState below = mob.level().getBlockState(pos.below());
        BlockState at = mob.level().getBlockState(pos);
        return !below.getCollisionShape(mob.level(), pos.below()).isEmpty() && at.isAir() && mob.getNavigation().isStableDestination(pos);
    }

    private BlockPos findNearestShade()
    {
        BlockPos mobPos = mob.blockPosition();
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        float windDirection = EnvironmentHelpers.getWindDirection(mob.level(), mobPos);
        Direction windFrom =0f;

        Map<BlockPos, Float> highestWind = new HashMap<>();

        for (BlockPos pos : BlockPos.betweenClosed(mobPos.offset(-SEARCH_RANGE, -3, -SEARCH_RANGE), mobPos.offset(SEARCH_RANGE, 3, SEARCH_RANGE)))
        {
            if (!mob.level().canSeeSky(pos) && isWalkable(pos))
            {
                double dist = mobPos.distSqr(pos);
                if(dist < nearestdist)
                {
                    nearestdist = dist;
                    nearest = pos.immutable();
                }
            }
        }
        return nearest;
    }

    private Direction getWindDirectionCardinal(float radians)
    {
        
    }

    @Override
    public boolean canUse()
    {
        if (cooldown > 0)
        {
            cooldown--;
            return false;
        }

        if (!mob.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperature tempData = mob.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        float currentInternalTemp = tempData.internalTemperature();
        float maxInternalTemperature = tempData.maxInternalTemperature();
        float minInternalTemperature = tempData.minInternalTemperature();
        float midPointTemperature = (maxInternalTemperature + minInternalTemperature) * 0.5f;

        float hotThreshold = midPointTemperature * (1f + COMFORT_THRESHOLD);
        float coldThreshold = midPointTemperature * (1f - COMFORT_THRESHOLD);

        if (currentInternalTemp > hotThreshold)
        {
            // targetPos = findCold
            if (targetPos != null && !isAtPosition(targetPos))
            {
                cooldown = COOLDOWN_IN_TICKS;
                return true;
            }
        }

        if (currentInternalTemp < coldThreshold)
        {
            // targetPos = findHot
            if (targetPos != null && !isAtPosition(targetPos))
            {
                cooldown = COOLDOWN_IN_TICKS;
                return true;
            }

            return false;
        }
    }

    @Override
    public boolean canContinueToUse()
    {
        if (targetPos == null) return false;
        if (isAtPosition(targetPos)) return false;
        if (!mob.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperature tempData = mob.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

        float internalTemperature = tempData.internalTemperature();
        float maxInternalTemperature = tempData.maxInternalTemperature();
        float minInternalTemperature = tempData.minInternalTemperature();
        float midPointTemperature = (maxInternalTemperature + minInternalTemperature) * 0.5f;

        float hotThreshold = midPointTemperature * (1f + COMFORT_THRESHOLD);
        float coldThreshold = midPointTemperature * (1f - COMFORT_THRESHOLD);

        if (internalTemperature < hotThreshold && internalTemperature > coldThreshold) return false;

        return !mob.getNavigation().isDone();
    }

    @Override
    public void start()
    {
        if (targetPos != null)
        {
            mob.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, speedModifier);
        }
    }

    @Override
    public void stop()
    {
        targetPos = null;
        mob.getNavigation().stop();
    }
}
