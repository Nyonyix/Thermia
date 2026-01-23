package com.nyonyix.thermia.ai.sensors;

import com.nyonyix.thermia.ai.ThermiaMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Set;

public class TemperatureComfortDecisionSensor extends Sensor<PathfinderMob>
{
    @Override
    protected void doTick(ServerLevel level, PathfinderMob mob)
    {

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
