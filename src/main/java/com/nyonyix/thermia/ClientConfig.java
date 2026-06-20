package com.nyonyix.thermia;

import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {BUILDER.push("ui").push("temperature");}

    public static final ModConfigSpec.BooleanValue TEMPERATURE_UI_TOGGLE = BUILDER.comment("Toggle temperature UI widget").define("temperatureUiToggle", true);
    public static final ModConfigSpec.DoubleValue TEMPERATURE_UI_SCALE = BUILDER.comment("Scale of temperature UI widget").defineInRange("temperatureUiScale", 1.0, 0.5, 2.0);
    public static final ModConfigSpec.IntValue TEMPERATURE_UI_OFFSET_X = BUILDER.comment("X offset of temperature UI widget").defineInRange("temperatureUiOffsetX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue TEMPERATURE_UI_OFFSET_Y = BUILDER.comment("Y offset of temperature UI widget").defineInRange("temperatureUiOffsetY", -38, Integer.MIN_VALUE, Integer.MAX_VALUE);

    static {BUILDER.pop().pop();}
    static {BUILDER.push("ui").push("wind");}

    public static final ModConfigSpec.BooleanValue WIND_UI_TOGGLE = BUILDER.comment("Toggle wind UI widget").define("windUiToggle", true);
    public static final ModConfigSpec.BooleanValue WIND_UI_SUB_TOGGLE = BUILDER.comment("Toggle if widget is smaller than temperature").define("windUISubToggle", true);
    public static final ModConfigSpec.DoubleValue WIND_UI_SCALE = BUILDER.comment("Scale of wind UI widget").defineInRange("windUiScale", 1.0, 0.5, 2.0);
    public static final ModConfigSpec.IntValue WIND_UI_OFFSET_X = BUILDER.comment("X offset of wind UI widget").defineInRange("windUiOffsetX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue WIND_UI_OFFSET_Y = BUILDER.comment("Y offset of wind UI widget").defineInRange("windUiOffsetY", -48, Integer.MIN_VALUE, Integer.MAX_VALUE);

    static {BUILDER.pop().pop();}
    static {BUILDER.push("ui").push("solar");}

    public static final ModConfigSpec.BooleanValue SOLAR_UI_TOGGLE = BUILDER.comment("Toggle solar UI widget").define("solarUiToggle", true);
    public static final ModConfigSpec.BooleanValue SOLAR_UI_SUB_TOGGLE = BUILDER.comment("Toggle if widget is smaller than temperature").define("solarUISubToggle", true);
    public static final ModConfigSpec.DoubleValue SOLAR_UI_SCALE = BUILDER.comment("Scale of solar UI widget").defineInRange("solarUiScale", 1.0, 0.5, 2.0);
    public static final ModConfigSpec.IntValue SOLAR_UI_OFFSET_X = BUILDER.comment("X offset of solar UI widget").defineInRange("solarUiOffsetX", 7, Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue SOLAR_UI_OFFSET_Y = BUILDER.comment("Y offset of solar UI widget").defineInRange("solarUiOffsetY", -48, Integer.MIN_VALUE, Integer.MAX_VALUE);

    static {BUILDER.pop().pop();}
    static {BUILDER.push("ui").push("wetness");}

    public static final ModConfigSpec.BooleanValue WETNESS_UI_TOGGLE = BUILDER.comment("Toggle wetness UI widget").define("wetnessUiToggle", true);
    public static final ModConfigSpec.BooleanValue WETNESS_UI_SUB_TOGGLE = BUILDER.comment("Toggle if widget is smaller than temperature").define("wetnessUISubToggle", true);
    public static final ModConfigSpec.DoubleValue WETNESS_UI_SCALE = BUILDER.comment("Scale of wetness UI widget").defineInRange("wetnessUiScale", 1.0, 0.5, 2.0);
    public static final ModConfigSpec.IntValue WETNESS_UI_OFFSET_X = BUILDER.comment("X offset of wetness UI widget").defineInRange("wetnessUiOffsetX", -7, Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue WETNESS_UI_OFFSET_Y = BUILDER.comment("Y offset of wetness UI widget").defineInRange("wetnessUiOffsetY", -48, Integer.MIN_VALUE, Integer.MAX_VALUE);

    static {BUILDER.pop().pop();}
    static {BUILDER.push("ui");}

    public static final ModConfigSpec.BooleanValue GLOBAL_UI_TOGGLE = BUILDER.comment("Toggle all UI widgets").define("globalUiToggle", true);
    public static final ModConfigSpec.DoubleValue GLOBAL_UI_SCALE = BUILDER.comment("Scale of all UI elements").defineInRange("globalUiScale", 1.0, 0.5, 2.0);
    public static final ModConfigSpec.IntValue GLOBAL_UI_OFFSET_X = BUILDER.comment("X offset for all UI elements").defineInRange("globalUiOffsetX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue GLOBAL_UI_OFFSET_Y = BUILDER.comment("Y offset for all UI elements").defineInRange("globalUiOffsetY", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

    static {BUILDER.pop();}
    static {BUILDER.push("effect").push("hypo");}

    public static final ModConfigSpec.BooleanValue ENABLE_HYPO_RENDER = BUILDER.comment("Enable or disable hypothermia rendering").define("enableHypoRender", true);
    public static final ModConfigSpec.DoubleValue HYPO_EFFECT_INTENSITY = BUILDER.comment("Hypothermia effect intensity").defineInRange("hypoEffectIntensity", 1.0, 0.5, 2.0);

    static {BUILDER.pop().pop();}
    static {BUILDER.push("effect").push("hyper");}

    public static final ModConfigSpec.BooleanValue ENABLE_HYPER_RENDER = BUILDER.comment("Enable or disable hyperthermia rendering").define("enableHyperRender", true);
    public static final ModConfigSpec.DoubleValue HYPER_EFFECT_INTENSITY = BUILDER.comment("Hyperthermia effect intensity").defineInRange("hyperEffectIntensity", 1.0, 0.5, 2.0);

    static {BUILDER.pop().pop();}
    static {BUILDER.push("client_misc");}

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG = BUILDER.comment("Enable debug information").define("enableDebug", false);

    static {BUILDER.pop();}

    static final ModConfigSpec CLIENT_CONFIG = BUILDER.build();
}
