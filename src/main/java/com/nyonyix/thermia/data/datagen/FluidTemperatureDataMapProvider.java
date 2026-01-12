package com.nyonyix.thermia.data.datagen;

import com.nyonyix.thermia.data.datamap.FluidTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import net.dries007.tfc.common.fluids.FluidHolder;
import net.dries007.tfc.common.fluids.SimpleFluid;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FluidTemperatureDataMapProvider extends DataMapProvider
{
    private final float WATER_TEMPERATURE = -10f;
    private final float WATER_TEMPERATURE_FLOWING = -15f;

    private static float getMetalTemperature(Metal metal)
    {
        return switch (metal)
        {
            case BISMUTH -> 270f;
            case BISMUTH_BRONZE -> 985f;
            case TIN -> 232f;
            case GOLD -> 1060f;
            case ZINC -> 420f;
            case BRASS -> 930f;
            case STEEL -> 1540f;
            case BRONZE -> 950f;
            case COPPER -> 1085f;
            case NICKEL -> 1435f;
            case SILVER -> 961f;
            case UNKNOWN -> 1200f;
            case PIG_IRON -> 1500f;
            case CAST_IRON -> 1535f;
            case RED_STEEL -> 1540f;
            case ROSE_GOLD -> 960f;
            case BLUE_STEEL -> 1540f;
            case WEAK_STEEL -> 1540f;
            case BLACK_STEEL -> 1485f;
            case BLACK_BRONZE -> 1070f;
            case WROUGHT_IRON -> 1535f;
            case WEAK_RED_STEEL -> 1540f;
            case STERLING_SILVER -> 900f;
            case WEAK_BLUE_STEEL -> 1540f;
            case HIGH_CARBON_STEEL -> 1540f;
            case HIGH_CARBON_RED_STEEL -> 1540f;
            case HIGH_CARBON_BLUE_STEEL -> 1540f;
            case HIGH_CARBON_BLACK_STEEL -> 1485f;
            case null -> 0f;
        };
    }

    public FluidTemperatureDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {super(packOutput, lookupProvider);}

    @Override
    protected void gather()
    {
        for (Map.Entry<Metal, FluidHolder<BaseFlowingFluid>> entry : TFCFluids.METALS.entrySet())
        {
            builder(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP)
                    .add(entry.getValue().source(), new FluidTemperatureDataMap(getMetalTemperature(entry.getKey()) * 0.8f, 16, true), false)
                    .add(entry.getValue().flowing(), new FluidTemperatureDataMap(getMetalTemperature(entry.getKey()), 16, true), false);
        }

        for (Map.Entry<SimpleFluid, FluidHolder<BaseFlowingFluid>> entry : TFCFluids.SIMPLE_FLUIDS.entrySet())
        {
            builder(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP)
                    .add(entry.getValue().source(), new FluidTemperatureDataMap(WATER_TEMPERATURE, 16, false), false)
                    .add(entry.getValue().flowing(), new FluidTemperatureDataMap(WATER_TEMPERATURE_FLOWING, 16, false), false);
        }

        for (Map.Entry<DyeColor, FluidHolder<BaseFlowingFluid>> entry : TFCFluids.COLORED_FLUIDS.entrySet())
        {
            builder(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP)
                    .add(entry.getValue().source(), new FluidTemperatureDataMap(WATER_TEMPERATURE, 16, false), false)
                    .add(entry.getValue().flowing(), new FluidTemperatureDataMap(WATER_TEMPERATURE_FLOWING, 16, false), false);
        }

        builder(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP)
                .add(TFCFluids.SPRING_WATER.source(), new FluidTemperatureDataMap(40f, 16, false), false)
                .add(TFCFluids.SPRING_WATER.flowing(), new FluidTemperatureDataMap(40f, 16, false), false)
                .add(BuiltInRegistries.FLUID.wrapAsHolder(Fluids.LAVA), new FluidTemperatureDataMap(1000f, 16, true), false)
                .add(BuiltInRegistries.FLUID.wrapAsHolder(Fluids.FLOWING_LAVA), new FluidTemperatureDataMap(1200f, 16, true), false)

                .add(BuiltInRegistries.FLUID.wrapAsHolder(Fluids.WATER), new FluidTemperatureDataMap(WATER_TEMPERATURE, 16, false), false)
                .add(BuiltInRegistries.FLUID.wrapAsHolder(Fluids.FLOWING_WATER), new FluidTemperatureDataMap(WATER_TEMPERATURE_FLOWING, 16, false), false);
    }

    public @NotNull String getName() {return "Fluid_Temperature_DataMap";}
}
