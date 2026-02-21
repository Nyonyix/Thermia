package com.nyonyix.thermia.data;

import com.nyonyix.thermia.Thermia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ThermiaTags
{
    public static class Blocks
    {
        public static final TagKey<Block> INTERIOR_TRIGGERS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "interior_triggers"));
    }
}
