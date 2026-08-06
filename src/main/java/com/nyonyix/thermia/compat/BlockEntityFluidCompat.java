package com.nyonyix.thermia.compat;

import com.nyonyix.thermia.data.datamap.FluidTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import net.dries007.tfc.common.capabilities.BlockCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class BlockEntityFluidCompat
{
    @Nullable
    public static Float resolveTemperature(Level level, BlockPos pos)
    {
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (handler == null) handler = level.getCapability(BlockCapabilities.FLUID, pos, null);

        if (handler != null)
        {
            for (int i = 0; i < handler.getTanks(); i++)
            {
                FluidStack stack = handler.getFluidInTank(i);
                if (stack.isEmpty()) continue;

                FluidTemperatureDataMap datamap = BuiltInRegistries.FLUID.wrapAsHolder(stack.getFluid()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
                return  datamap != null ? datamap.temperature() : null;
            }
        }

        return null;
    }
}
