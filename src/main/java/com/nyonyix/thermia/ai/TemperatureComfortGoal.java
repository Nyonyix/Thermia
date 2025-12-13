package com.nyonyix.thermia.ai;

import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.ClosestSource;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class TemperatureComfortGoal extends Goal
{
    private final PathfinderMob mob;
    private final double speedModifier;
    private BlockPos targetPos;
    private int cooldown = 0;
    private static final int SEARCH_RANGE = 8;
    private static final int COOLDOWN_IN_TICKS = 200;
    private static final float MIN_MAX_BUFFER = 0.25f;

    public TemperatureComfortGoal(PathfinderMob mob, double speedModifier)
    {

        if (!ServerConfig.APPLY_TO_ANIMALS.getAsBoolean()) throw new IllegalStateException("Attempted TemperatureComfortGoal when APPLY_TO_ANIMALS is false");

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
        double nearestdist = Double.MAX_VALUE;

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

    @Override
    public boolean canUse()
    {
        if (cooldown > 0)
        {
            cooldown--;
            return false;
        }

        EntityTemperature tempData = mob.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        if (tempData == null) return false;

        float currentInternalTemp = tempData.internalTemperature();
        float maxInternalTemperature = tempData.maxInternalTemperature();
        float minInternalTemperature = tempData.minInternalTemperature();
        ClosestSource source = tempData.closestSource();

        float maxEntityTempBeforeHurt = 0.0f;
        float minEntityTempBeforeHurt = 0.0f;

        if (maxInternalTemperature < 0.0f)
        {
            maxEntityTempBeforeHurt = maxInternalTemperature * (1f + MIN_MAX_BUFFER);
        }
        else
        {
            maxEntityTempBeforeHurt = maxInternalTemperature * (1f - MIN_MAX_BUFFER);
        }

        if (minInternalTemperature < 0.0f)
        {
            minEntityTempBeforeHurt = minInternalTemperature * (1f - MIN_MAX_BUFFER);
        }
        else
        {
            minEntityTempBeforeHurt = minInternalTemperature * (1f + MIN_MAX_BUFFER);
        }

        if (currentInternalTemp >= maxEntityTempBeforeHurt)
        {
            targetPos = findNearestShade();
            if (targetPos != null)
            {
                if (!isAtPosition(targetPos))
                {
                    cooldown = COOLDOWN_IN_TICKS;
                    return true;
                }
            }
        }

        if (currentInternalTemp <= minEntityTempBeforeHurt)
        {
            targetPos = source.closestPos();
            if (targetPos != null)
            {
                if (!isAtPosition(targetPos))
                {
                    cooldown = COOLDOWN_IN_TICKS;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse()
    {
        if (targetPos == null) return false;
        if (isAtPosition(targetPos)) return false;

        EntityTemperature tempData = mob.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        if (tempData != null)
        {
            float internalTemperature = tempData.internalTemperature();
            float maxEntityTempBeforeHurt = tempData.maxInternalTemperature() * (1f - MIN_MAX_BUFFER);
            float minEntityTempBeforeHurt = tempData.minInternalTemperature() * (1 + MIN_MAX_BUFFER);

            if (internalTemperature > minEntityTempBeforeHurt && internalTemperature < maxEntityTempBeforeHurt) return false;
        }
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
