package com.nyonyix.thermia.ai.sensors;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.ai.memories.ThermiaMemoryModules;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import net.dries007.tfc.util.calendar.Calendar;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import org.slf4j.Logger;

import java.util.Set;

public class PassiveTemperatureSensor extends Sensor<PathfinderMob>
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected void doTick(ServerLevel level, PathfinderMob mob)
    {
        if (!mob.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;

        Brain<?> brain = mob.getBrain();
        EntityTemperature tempData = mob.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        BlockPos currentPos = mob.blockPosition();
        float currentEnvironmentTemperature = tempData.environmentTemperature();

        int decayDays = ServerConfig.MEMORY_DECAY_DAYS.getAsInt();
        long memoryDecayTicks = (long) Calendar.CALENDAR_TICKS_IN_DAY * decayDays;

        float warmestTemperature = brain.getMemory(ThermiaMemoryModules.WARMEST_SPOT_TEMPERATURE.get()).orElse(Float.NEGATIVE_INFINITY);
        if (currentEnvironmentTemperature > warmestTemperature)
        {
            brain.setMemoryWithExpiry(ThermiaMemoryModules.WARMEST_SPOT_BLOCK_POS.get(), currentPos.immutable(), memoryDecayTicks);
            brain.setMemoryWithExpiry(ThermiaMemoryModules.WARMEST_SPOT_TEMPERATURE.get(), currentEnvironmentTemperature, memoryDecayTicks);
        }

        float coolestTemperature = brain.getMemory(ThermiaMemoryModules.COOLEST_SPOT_TEMPERATURE.get()).orElse(Float.POSITIVE_INFINITY);
        if (currentEnvironmentTemperature < coolestTemperature)
        {
            brain.setMemoryWithExpiry(ThermiaMemoryModules.COOLEST_SPOT_BLOCK_POS.get(), currentPos.immutable(), memoryDecayTicks);
            brain.setMemoryWithExpiry(ThermiaMemoryModules.COOLEST_SPOT_TEMPERATURE.get(), currentEnvironmentTemperature, memoryDecayTicks);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return Set.of(
                ThermiaMemoryModules.WARMEST_SPOT_BLOCK_POS.get(),
                ThermiaMemoryModules.WARMEST_SPOT_TEMPERATURE.get(),
                ThermiaMemoryModules.WARMEST_SPOT_TIME.get(),
                ThermiaMemoryModules.COOLEST_SPOT_BLOCK_POS.get(),
                ThermiaMemoryModules.COOLEST_SPOT_TEMPERATURE.get(),
                ThermiaMemoryModules.COOLEST_SPOT_TIME.get()
        );
    }
}
