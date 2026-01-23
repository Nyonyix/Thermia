package com.nyonyix.thermia.ai.sensors;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ai.ThermiaMemoryModules;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
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
    private static final long MEMORY_DECAY_TICKS = 240000;

    @Override
    protected void doTick(ServerLevel level, PathfinderMob mob)
    {
        if (!mob.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;

        Brain<?> brain = mob.getBrain();
        EntityTemperature tempData = mob.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        BlockPos currentPos = mob.blockPosition();
        float currentEnvironmentTemperature = tempData.environmentTemperature();
        long gameTime = level.getGameTime();

        float warmestTemperature = brain.getMemory(ThermiaMemoryModules.WARMEST_SPOT_TEMPERATURE.get()).orElse(Float.NEGATIVE_INFINITY);
        if (currentEnvironmentTemperature > warmestTemperature)
        {
            brain.setMemoryWithExpiry(ThermiaMemoryModules.WARMEST_SPOT_BLOCK_POS.get(), currentPos.immutable(), MEMORY_DECAY_TICKS);
            brain.setMemoryWithExpiry(ThermiaMemoryModules.WARMEST_SPOT_TEMPERATURE.get(), currentEnvironmentTemperature, MEMORY_DECAY_TICKS);
        }

        float coolestTemperature = brain.getMemory(ThermiaMemoryModules.COOLEST_SPOT_TEMPERATURE.get()).orElse(Float.POSITIVE_INFINITY);
        if (currentEnvironmentTemperature < coolestTemperature)
        {
            brain.setMemoryWithExpiry(ThermiaMemoryModules.COOLEST_SPOT_BLOCK_POS.get(), currentPos.immutable(), MEMORY_DECAY_TICKS);
            brain.setMemoryWithExpiry(ThermiaMemoryModules.COOLEST_SPOT_TEMPERATURE.get(), currentEnvironmentTemperature, MEMORY_DECAY_TICKS);
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
