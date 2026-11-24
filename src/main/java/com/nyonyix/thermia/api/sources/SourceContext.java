package com.nyonyix.thermia.api.sources;


import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.datamap.EntityTemperatureDataMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Read-only view of the entity being evaluated. Constructed by Thermia on the
 * server thread, once per entity update. Do not retain it.
 */

public record SourceContext(
        Entity entity,
        Level level,
        BlockPos pos,
        EntityTemperature temperature,
        EntityTemperatureDataMap dataMap,
        float radiationProtection,
        float convectionProtection,
        float conductionProtection
)
{
}
