package com.nyonyix.thermia.ai.sensors;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.ai.behaviours.ThermiaActivities;
import com.nyonyix.thermia.ai.memories.ThermiaMemoryModules;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.util.AiHelpers;
import net.dries007.tfc.util.calendar.Calendar;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;

public class TemperatureComfortDecisionSensor extends Sensor<PathfinderMob>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SCAN_RATE = 40;
    private static final float TEMPERATURE_THRESHOLD = 5f;
    private static final float COMFORT_THRESHOLD = AiHelpers.COMFORT_THRESHOLD;

    public TemperatureComfortDecisionSensor() {super(SCAN_RATE);}

    @Override
    protected void doTick(ServerLevel level, PathfinderMob mob)
    {
        if (!mob.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;

        Brain<?> brain = mob.getBrain();
        EntityTemperature tempData = mob.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        float[] thresholds = AiHelpers.getComfortThresholds(tempData, COMFORT_THRESHOLD);
        float currentInternalTemperature = tempData.internalTemperature();
        float currentEnvironmentTemperature = tempData.environmentTemperature();

        boolean isTooHot = currentInternalTemperature > thresholds[1];
        boolean isTooCold = currentInternalTemperature < thresholds[0];
        boolean isUncomfortable = isTooCold || isTooHot;

        if (isUncomfortable)
        {
            boolean seekingWarmth = isTooCold;
            brain.setMemory(ThermiaMemoryModules.IS_SEEKING_WARM.get(), seekingWarmth);

            Optional<BlockPos> targetPos = seekingWarmth ? brain.getMemory(ThermiaMemoryModules.WARMEST_SPOT_BLOCK_POS.get()) : brain.getMemory(ThermiaMemoryModules.COOLEST_SPOT_BLOCK_POS.get());
            Optional<Float> targetTemperature = seekingWarmth ? brain.getMemory(ThermiaMemoryModules.WARMEST_SPOT_TEMPERATURE.get()) : brain.getMemory(ThermiaMemoryModules.COOLEST_SPOT_TEMPERATURE.get());

            if (targetPos.isPresent() && targetTemperature.isPresent())
            {
                float temperatureDiff = seekingWarmth ? targetTemperature.get() - currentEnvironmentTemperature : currentEnvironmentTemperature - targetTemperature.get();

                if (temperatureDiff > TEMPERATURE_THRESHOLD)
                {
                    brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos.get(), 1.0f, 1));
                    brain.setActiveActivityIfPossible(ThermiaActivities.SEEK_COMFORT.get());

                    LOGGER.debug("Entity {}: Seeking {} at {} with a temperature difference of {}", mob.getType().getDescriptionId(), seekingWarmth ? "warmth" : "cooling", targetPos.get().toString(), temperatureDiff);
                }
            }
            else
            {
                BlockPos foundPos = seekingWarmth ? AiHelpers.findNearestHeatSource(mob, tempData.blockSearchResult()) : AiHelpers.findNearestColdOrWater(mob, tempData.blockSearchResult());

                if (foundPos != null)
                {
                    long decayTicks = (long) Calendar.CALENDAR_TICKS_IN_DAY * ServerConfig.MEMORY_DECAY_DAYS.getAsInt();

                    if (seekingWarmth)
                    {
                        brain.setMemoryWithExpiry(ThermiaMemoryModules.WARMEST_SPOT_BLOCK_POS.get(), foundPos, decayTicks);
                        brain.setMemoryWithExpiry(ThermiaMemoryModules.WARMEST_SPOT_TEMPERATURE.get(), currentEnvironmentTemperature, decayTicks);
                    }
                    else
                    {
                        brain.setMemoryWithExpiry(ThermiaMemoryModules.COOLEST_SPOT_BLOCK_POS.get(), foundPos, decayTicks);
                        brain.setMemoryWithExpiry(ThermiaMemoryModules.WARMEST_SPOT_TEMPERATURE.get(), currentEnvironmentTemperature, decayTicks);
                    }

                    brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(foundPos, 1.0f, 1));
                    brain.setActiveActivityIfPossible(ThermiaActivities.SEEK_COMFORT.get());

                    LOGGER.debug("Entity {}: Found new location via search {}", mob.getType().getDescriptionId(), foundPos);
                }
            }
        }
        else
        {
            brain.eraseMemory(ThermiaMemoryModules.IS_SEEKING_WARM.get());

            Activity currentActivity = brain.getActiveNonCoreActivity().orElse(Activity.IDLE);
            if (currentActivity == ThermiaActivities.SEEK_COMFORT.get())
            {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                brain.setActiveActivityIfPossible(Activity.IDLE);

                LOGGER.debug("Entity {}: Is comfortable, Going to idle", mob.getType().getDescriptionId());
            }
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return Set.of(
                ThermiaMemoryModules.IS_SEEKING_WARM.get(),
                MemoryModuleType.WALK_TARGET
        );
    }
}
