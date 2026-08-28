package com.nyonyix.thermia.data.climate;


import com.nyonyix.thermia.Thermia;
import net.dries007.tfc.util.climate.ClimateModelType;
import net.dries007.tfc.util.climate.ClimateModels;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ThermiaClimateModels
{
    public static final DeferredRegister<ClimateModelType<?>> TYPES = DeferredRegister.create(ClimateModels.KEY, Thermia.MODID);
    public static final Supplier<ClimateModelType<ThermiaClimateModel>> THERMIA = TYPES.register("thermia", () -> new ClimateModelType<>(ThermiaClimateModel.STREAM_CODEC));
}
