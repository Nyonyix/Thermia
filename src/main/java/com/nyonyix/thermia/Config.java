package com.nyonyix.thermia;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue MAX_ENTITY_TEMP = BUILDER
            .comment("Max entity temperature")
            .defineInRange("maxEntityTemp", 38.0, 20.0, 45.0);

    public static final ModConfigSpec.DoubleValue MIN_ENTITY_TEMP = BUILDER
            .comment("Min entity temperature")
            .defineInRange("minEntityTemp", 0, -10.0, 20.0);

    public static final ModConfigSpec.IntValue MAX_SEARCH_RANGE = BUILDER
            .comment("Max range to search for blocks")
            .defineInRange("maxSearchRange", 16, 4, 32);

    public static final ModConfigSpec.BooleanValue APPLY_TO_ANIMALS = BUILDER
            .comment("Apply temperature system to domesticated animals")
            .define("applyToAnimals", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
