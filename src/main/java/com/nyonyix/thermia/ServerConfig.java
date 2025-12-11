package com.nyonyix.thermia;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue MAX_ENTITY_TEMP = BUILDER
            .comment("Max entity temperature")
            .defineInRange("maxEntityTemp", 40.0, 20.0, 45.0);

    public static final ModConfigSpec.DoubleValue MIN_ENTITY_TEMP = BUILDER
            .comment("Min entity temperature")
            .defineInRange("minEntityTemp", 10, 1.0, 20.0);

    public static final ModConfigSpec.IntValue MAX_SEARCH_RANGE = BUILDER
            .comment("Max range to search for blocks from player entities")
            .defineInRange("maxSearchRange", 16, 4, 32);

    public static final ModConfigSpec.BooleanValue APPLY_TO_ANIMALS = BUILDER
            .comment("Apply temperature system to domesticated animals")
            .define("applyToAnimals", true);

    public static final ModConfigSpec.IntValue MAX_ANIMAL_SEARCH_RANGE = BUILDER
            .comment("Max range to search for blocks from animal entities")
            .defineInRange("maxAnimalSearchRange", 8, 4, 32);

    static final ModConfigSpec SERVER_CONFIG = BUILDER.build();
}
