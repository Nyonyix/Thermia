package com.nyonyix.thermia.data;

import com.nyonyix.thermia.Thermia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;

public class ThermiaTags
{
    public static class Blocks
    {
        public static final TagKey<Block> INTERIOR_TRIGGERS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "interior_triggers"));
    }

    public static class Items
    {
        public static final TagKey<Item> CURIOS_CAPE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "cape"));
        public static final TagKey<Item> CURIOS_HEAD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "head"));
        public static final TagKey<Item> CURIOS_BODY = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "body"));
        public static final TagKey<Item> CURIOS_LEGS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "legs"));
        public static final TagKey<Item> CURIOS_FEET = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "feet"));
    }
}
