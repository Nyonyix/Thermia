package com.nyonyix.thermia.compat;

import com.nyonyix.thermia.api.sources.BlockTemperatures;
import com.nyonyix.thermia.compat.create.CreateHeatCompat;
import com.nyonyix.thermia.compat.powergrid.PowerGridCompat;
import com.nyonyix.thermia.data.datamap.BlockTemperatureDataMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CapabilityBlockTemperatures
{
    private static float resolve(Level level, BlockPos pos, BlockEntity blockEntity, BlockTemperatureDataMap dataMap)
    {
        Float thermal = PowerGridCompat.resolveTemeprature(level, pos);
        if (thermal != null) return thermal;

        Float pipe = CreateHeatCompat.resolvePipeTemperature(level, pos);
        if (pipe != null) return pipe;

        Float fluid = BlockEntityFluidCompat.resolveTemperature(level, pos);
        if (fluid != null) return fluid;

        return Float.NaN;
    }

    public static void register()
    {
        BlockTemperatures.register(BlockEntity.class, CapabilityBlockTemperatures::resolve);
    }
}
