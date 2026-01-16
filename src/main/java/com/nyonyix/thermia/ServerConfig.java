package com.nyonyix.thermia;

import net.neoforged.neoforge.common.ModConfigSpec;

import javax.swing.plaf.BorderUIResource;

public class ServerConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {BUILDER.push("search_and_range");}

    public static final ModConfigSpec.IntValue SEARCH_RANGE = BUILDER.comment("Max range to search for blocks from player entities").defineInRange("SearchRange", 32, 8, 64);

    public static final ModConfigSpec.IntValue ISMOB_SEARCH_RANGE = BUILDER.comment("Max range to search for blocks from animal entities").defineInRange("isMobSearchRange", 16, 4, 32);

    public static final ModConfigSpec.IntValue MAX_BLOCKS_ABOVE = BUILDER.comment("Maximum number of blocks above for depth humidity").defineInRange("maxBlocksAbove", 24576, 8192, 65536);

    static {BUILDER.pop();}
    static {BUILDER.push("entity_temperature");}

    public static final ModConfigSpec.IntValue MAX_RADIANT_HEATING = BUILDER.comment("Maximum heat by nearby sources").defineInRange("maxRadiantHeating", 128, 64, 256);

    public static final ModConfigSpec.IntValue MAX_INVENTORY_HEATING = BUILDER.comment("Maximum heating from hot TFC items in inventory").defineInRange("maxInvetoryHeating", 128, 64, 256);

    public static final ModConfigSpec.IntValue TEMPERATURE_BUFFER_PERCENT = BUILDER.comment("Percent of delta as integer for effect levels").defineInRange("temp_buffer_percent", 15, 0, 50);

    public static final ModConfigSpec.IntValue MAX_TEMPERATURE_EFFECT_LEVEL = BUILDER.comment("Maximum amount of hyper/hypothermia effect levels.").defineInRange("max_temperature_effect_level", 4, 2, 8);

    public static final ModConfigSpec.DoubleValue ENTITY_TEMPERATURE_CHANGE_MULTI = BUILDER.comment("Entity temperature change multiplier").defineInRange("entityTemperatureChangeMulti", 1.0, 0.1, 2.0);

    static {BUILDER.pop();}
    static {BUILDER.push("environment_temperature");}

    public static final ModConfigSpec.DoubleValue MAX_SOLAR_HEATING = BUILDER.comment("Max heating applied by sun").defineInRange("maxSolarHeating", 18.0, 0.0, 22.0);

    public static final ModConfigSpec.DoubleValue INVENTORY_HEATING_SCALE = BUILDER.comment("Scale at which each item contributes to overall inventory heating").defineInRange("inventoryHeatingScale", 20000.0, 5000.0, 50000.0);

    public static final ModConfigSpec.DoubleValue EVAP_COOLING_MULTI = BUILDER.comment("Evaporative cooling multiplier").defineInRange("evapCoolingMulti", 1.0, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue SOLAR_RADIATION_MULTI = BUILDER.comment("Solar radiation multiplier").defineInRange("solarRadiationMulti", 1.0, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue DRYING_MULTI = BUILDER.comment("Drying multiplier").defineInRange("dryingMulti", 1.0, 0.0, 2.0);

    static {BUILDER.pop();}
    static {BUILDER.push("misc");}

    public static final ModConfigSpec.BooleanValue OVERRIDE_TFC_TEMP_THIRST = BUILDER.comment("Disable TFC's temperature dependant thirst.").define("overrideTFCTempThirst", true);

    static final ModConfigSpec SERVER_CONFIG = BUILDER.build();
}
