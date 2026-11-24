package com.nyonyix.thermia.registry;

import com.nyonyix.thermia.api.sources.BlockTemperatures;
import com.nyonyix.thermia.api.sources.ThermiaSources;
import com.nyonyix.thermia.compat.CapabilityBlockTemperatures;
import com.nyonyix.thermia.compat.create.CreateHeatCompat;
import com.nyonyix.thermia.compat.firmalife.FirmalifeHeatCompat;
import com.nyonyix.thermia.registry.builtins.BlockTemperatureBuiltins;
import com.nyonyix.thermia.registry.builtins.ThermiaSourceBuiltins;

public class ThermiaRegistry
{
    public static void register()
    {
        ThermiaSourceBuiltins.register();
        BlockTemperatureBuiltins.register();
        CapabilityBlockTemperatures.register();

        FirmalifeHeatCompat.register();
        CreateHeatCompat.register();
    }

    public static void freeze()
    {
        ThermiaSources.freeze();
        BlockTemperatures.freeze();
    }
}
