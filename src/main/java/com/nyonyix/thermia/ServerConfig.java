package com.nyonyix.thermia;

import com.sun.jna.platform.win32.Tlhelp32;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue SEARCH_RANGE = BUILDER.comment("Max range to search for blocks from player entities").defineInRange("SearchRange", 16, 4, 32);

    public static final ModConfigSpec.IntValue ISMOB_SEARCH_RANGE = BUILDER.comment("Max range to search for blocks from animal entities").defineInRange("isMobSearchRange", 8, 2, 16);

    public static final ModConfigSpec.IntValue MAX_RADIANT_HEATING = BUILDER.comment("Maximum heat by nearby sources").defineInRange("maxRadiantHeating", 256, 50, 512);

    public static final ModConfigSpec.DoubleValue MAX_SOLAR_HEATING = BUILDER.comment("Max heating applied by sun").defineInRange("maxSolarHeating", 18.0, 0.0, 22.0);

    public static final ModConfigSpec.DoubleValue WIND_CHILL_FACTOR = BUILDER.comment("Factor to adjust windchill effectiveness").defineInRange("windChillFactor", 0.15, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue TEMP_CHANGE_MIN_RATE = BUILDER.comment("Minimum rate for which entity internal temperature changes").defineInRange("tempChangeMinRate", 0.01, 0.001, 0.1);

    public static final ModConfigSpec.DoubleValue TEMP_CHANGE_MAX_RATE = BUILDER.comment("Maximum rate for which entity internal temperature changes").defineInRange("tempChangeMaxRate", 0.5, 0.1, 1.0);

    public static final ModConfigSpec.DoubleValue TEMP_CHANGE_SCALE = BUILDER.comment("Scale of delta curve").defineInRange("TempChangeScale", 0.05, 0.01, 0.2);

    static final ModConfigSpec SERVER_CONFIG = BUILDER.build();
}
