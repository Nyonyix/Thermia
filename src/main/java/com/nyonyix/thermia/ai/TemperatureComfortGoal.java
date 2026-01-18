package com.nyonyix.thermia.ai;

import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.util.AiHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class TemperatureComfortGoal extends Goal
{
    private final PathfinderMob mob;
    private final double speedModifier;
    private BlockPos targetPos;
    private int cooldown = 0;

    private static final int SEARCH_RANGE = 12;
    private static final int COOLDOWN_IN_TICKS = 100;
    private static final float COMFORT_THRESHOLD = 0.75f;

    public TemperatureComfortGoal(PathfinderMob mob, double speedModifier)
    {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
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
        float[] thresholds = AiHelpers.getComfortThresholds(tempData, COMFORT_THRESHOLD);

        if (currentInternalTemp > thresholds[1])
        {
            targetPos = AiHelpers.findNearestHomeBlock(mob, tempData.blockSearchResult());

            if (targetPos != null && !AiHelpers.isAtPosition(mob, targetPos))
            {
                cooldown = COOLDOWN_IN_TICKS;
                return true;
            }
        }

        if (currentInternalTemp < thresholds[0])
        {
            targetPos = AiHelpers.findNearestHeatSource(mob, tempData.blockSearchResult());

            if (targetPos != null && !AiHelpers.isAtPosition(mob, targetPos))
            {
                cooldown = COOLDOWN_IN_TICKS;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse()
    {
        if (targetPos == null) return false;
        if (AiHelpers.isAtPosition(mob, targetPos)) return false;
        if (!mob.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperature tempData = mob.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

        float internalTemperature = tempData.internalTemperature();
        float[] thresholds = AiHelpers.getComfortThresholds(tempData, COMFORT_THRESHOLD);

        if (internalTemperature < thresholds[1] && internalTemperature > thresholds[0]) return false;

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
