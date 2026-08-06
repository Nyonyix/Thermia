package com.nyonyix.thermia.compat.create;

import com.nyonyix.thermia.data.datamap.FluidTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class CreateHeatCompat
{
    public static final String MOD_ID = "create";
    private static final boolean IS_LOADED = ModList.get().isLoaded(MOD_ID);
    private static final float MAX_BOILER_TEMPERATURE = 1500f;

    @Nullable
    public static Float resolvePipeTemperature(Level level, BlockPos pos)
    {
        if (!IS_LOADED) return null;

        FluidTransportBehaviour transportBehaviour = BlockEntityBehaviour.get(level, pos, FluidTransportBehaviour.TYPE);
        if (transportBehaviour == null) return null;

        for (PipeConnection connection : transportBehaviour.interfaces.values())
        {
            FluidStack stack = connection.getProvidedFluid();
            if (stack.isEmpty()) continue;

            FluidTemperatureDataMap datamap = BuiltInRegistries.FLUID.wrapAsHolder(stack.getFluid()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            return  datamap != null ? datamap.temperature() : null;
        }

        return null;
    }

    @Nullable
    public static Float resolveBoilerTemperature(Level level, BlockPos pos)
    {
        if (!IS_LOADED) return null;

        FluidTankBlockEntity tank = null;
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof FluidTankBlockEntity fluidTank) tank = fluidTank;
        else if (blockEntity instanceof SteamEngineBlockEntity engine) tank = engine.getTank();

        if (tank == null) return null;
        if (!tank.isController()) tank = tank.getControllerBE();
        if (tank == null) return null;
        if (tank.boiler.activeHeat <= 0) return null;

        return tank.boiler.activeHeat / 18f * MAX_BOILER_TEMPERATURE;
    }
}
