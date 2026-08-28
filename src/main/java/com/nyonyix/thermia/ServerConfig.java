package com.nyonyix.thermia;

import net.minecraft.commands.execution.tasks.BuildContexts;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {BUILDER.push("search_and_range");}

    public static final ModConfigSpec.IntValue SEARCH_RANGE = BUILDER.comment("Max range to search for blocks from player entities").defineInRange("searchRange", 32, 8, 64);

    public static final ModConfigSpec.IntValue ISMOB_SEARCH_RANGE = BUILDER.comment("Max range to search for blocks from animal entities").defineInRange("isMobSearchRange", 16, 4, 32);

    public static final ModConfigSpec.IntValue MAX_BLOCKS_ABOVE = BUILDER.comment("Number of blocks above to be considered underground").defineInRange("maxBlocksAbove", 24576, 8192, 65536);

    public static final ModConfigSpec.IntValue MAX_FLUID_DEPTH_CHECK = BUILDER.comment("Maximum blocks to check for fluid depth temp modifier").defineInRange("maxFluidDepthCheck", 32, 8, 64);

    public static final ModConfigSpec.IntValue MAX_INTERIOR_VOLUME = BUILDER.comment("Maximum volume or number of blocks an interior can have").defineInRange("maxInteriorVolume", 13824, 4096, 32768);

//    public static final ModConfigSpec.DoubleValue MAX_PERCENTAGE_OPEN_ALLOWED = BUILDER.comment("Maximum percent of edge to allowed to be air").defineInRange("maxPercentageOpenAllowed", 0.25, 0.0, 0.5);

    static {BUILDER.pop();}
    static {BUILDER.push("entity_temperature");}

    public static final ModConfigSpec.IntValue PEAK_BLOCK_TEMPERATURE = BUILDER.comment("Maximum heat by nearby sources").defineInRange("maxRadiantHeating", 75, 50, 150);

    public static final ModConfigSpec.IntValue PEAK_INVENTORY_TEMPERATURE = BUILDER.comment("Maximum heating from hot TFC items in inventory").defineInRange("maxInventoryHeating", 75, 50, 150);

    public static final ModConfigSpec.IntValue TEMPERATURE_SEGMENTS_PERCENT = BUILDER.comment("Percentage used for various calculations that use buffers or segments").defineInRange("temperatureSegmentsPercent", 15, 0, 50);

    public static final ModConfigSpec.IntValue MAX_TEMPERATURE_EFFECT_LEVEL = BUILDER.comment("Maximum amount of hyper/hypothermia effect levels.").defineInRange("maxTemperatureEffectLevel", 4, 2, 8);

    public static final ModConfigSpec.DoubleValue PLAYER_SWEAT_HYDRATION_LOSS_MULTI = BUILDER.comment("Hydration loss through sweat multiplier").defineInRange("playerSweatHydrationLossMulti", 1.0, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue INVENTORY_HEAT_MULTI = BUILDER.comment("Inventory heat multiplier").defineInRange("inventoryHeatMulti", 1.0, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue ENTITY_TEMPERATURE_CHANGE_MULTI = BUILDER.comment("Entity temperature change multiplier").defineInRange("entityTemperatureChangeMulti", 0.5, 0.1, 2.0);

    public static final ModConfigSpec.DoubleValue ENTITY_ACCLIMATISATION_MULTI = BUILDER.comment("Entity climate acclimatisation multiplier").defineInRange("entityAcclimatisationMulti", 1.0, 0.1, 2.0);

    static {BUILDER.pop();}
    static {BUILDER.push("ai_and_memory");}

    public static final ModConfigSpec.IntValue MEMORY_DECAY_DAYS = BUILDER.comment("Number of days animal remembers warm and cold locations").defineInRange("memoryDecayDays", 7, 1, 96);

    public static final ModConfigSpec.IntValue MAX_STAY_TIME_MINUTES = BUILDER.comment("Maximum time (in TFC minutes) an animal will stay at a comfortable location").defineInRange("maxStayTimeMinutes", 120, 5, 480);

    static {BUILDER.pop();}
    static {BUILDER.push("environment_temperature");}

    public static final ModConfigSpec.DoubleValue MAX_SOLAR_HEATING = BUILDER.comment("Max heating applied by sun").defineInRange("maxSolarHeating", 32.0, 0.0, 64.0);

    public static final ModConfigSpec.DoubleValue EVAP_COOLING_MULTI = BUILDER.comment("Evaporative cooling multiplier").defineInRange("evapCoolingMulti", 1.0, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue DRYING_MULTI = BUILDER.comment("Drying multiplier").defineInRange("dryingMulti", 1.0, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue INTERIOR_SOURCE_MULTI = BUILDER.comment("Interior source pull multiplier").defineInRange("interiorSourceMulti", 1, 0.5, 10);

    public static final ModConfigSpec.IntValue INTERIOR_SECONDS_TO_KEEP = BUILDER.comment("Interior seconds to keep, How long should teh server keep an interior in limbo before invalidating").defineInRange("interiorSecondsToKeep", 60, 0, 300);

    public static final ModConfigSpec.DoubleValue INTERIOR_VENT_CURVE = BUILDER.comment("Interior ventilation curve, Higher and interiors are more ventilated, Lower is less ventilated.").defineInRange("interiorVentCurve", 8.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue INTERIOR_WIND_FACTOR = BUILDER.comment("Interior wind factor. Higher and wind matters more for interior leakiness, lower is less.").defineInRange("interiorWindFactor", 0.2, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue INTERIOR_SOLAR_MULTI = BUILDER.comment("Interior solar heating multiplier. Higher values mean more solar heating, lower is less.").defineInRange("interiorSolarMulti", 32.0, 0.0, 64.0);

    static {BUILDER.pop();}
    static {BUILDER.push("server_misc");}

    public static final ModConfigSpec.BooleanValue OVERRIDE_TFC_TEMP_THIRST = BUILDER.comment("Disable TFC's temperature dependant thirst.").define("overrideTFCTempThirst", true);

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG = BUILDER.comment("Toggles server sending data for client debug render").define("serverEnableDebug", false);

    static {BUILDER.pop();}

    static final ModConfigSpec SERVER_CONFIG = BUILDER.build();
}
