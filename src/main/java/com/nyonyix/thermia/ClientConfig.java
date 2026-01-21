package com.nyonyix.thermia;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {BUILDER.push("ui");}

    public static final ModConfigSpec.DoubleValue UI_SCALE = BUILDER.comment("Scale of UI elements").defineInRange("uiScale", 1.0, 0.5, 1.5);

    public static final ModConfigSpec.IntValue UI_X_OFFSET = BUILDER.comment("X Offset for UI elements").defineInRange("uiXOffset", 0, 0, 1024);

    public static final ModConfigSpec.IntValue UI_Y_OFFSET = BUILDER.comment("Y Offset for UI elements").defineInRange("uiYOffset", 0, 0, 1024);

    static {BUILDER.pop();}
    static {BUILDER.push("effect");}

    public static final ModConfigSpec.BooleanValue ENABLE_HYPER_RENDER = BUILDER.comment("Enable or disable hyperthermia rendering").define("enableHyperRender", true);

    public static final ModConfigSpec.BooleanValue ENABLE_HYPO_RENDER = BUILDER.comment("Enable or disable hypothermia rendering").define("enableHypoRender", true);

    public static final ModConfigSpec.DoubleValue HYPER_EFFECT_INTENSITY = BUILDER.comment("Hyperthermia effect intensity").defineInRange("hyperEffectIntensity", 1.0, 0.5, 1.5);

    public static final ModConfigSpec.DoubleValue HYPO_EFFECT_INTENSITY = BUILDER.comment("Hypothermia effect intensity").defineInRange("hypoEffectIntensity", 1.0, 0.5, 1.5);

    static {BUILDER.pop();}
    static {BUILDER.push("client_misc");}

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG = BUILDER.comment("Enable debug information").define("enableDebug", false);

    static {BUILDER.pop();}

    static final ModConfigSpec CLIENT_CONFIG = BUILDER.build();
}
