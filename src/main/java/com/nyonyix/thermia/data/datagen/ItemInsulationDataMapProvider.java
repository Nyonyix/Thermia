package com.nyonyix.thermia.data.datagen;

import com.nyonyix.thermia.data.map.ItemInsulation;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeConfig;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ItemInsulationDataMapProvider extends DataMapProvider
{
    public ItemInsulationDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {super(packOutput, lookupProvider);}

    @Override
    protected void gather()
    {
        builder(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP).replace(true)
                .add(TFCTags.Items.PLANTS, new ItemInsulation(0.04f), false)
                .add(TFCTags.Items.FALLEN_LEAVES, new ItemInsulation(0.03f), false)
                .add(TFCTags.Items.MUD, new ItemInsulation(0.04f), false)
                .add(ItemTags.WOOL, new ItemInsulation(0.09f), false)
                .add(ItemTags.WOOL_CARPETS, new ItemInsulation(0.07f), false)
                .add(ItemTags.LEAVES, new ItemInsulation(0.04f), false)
                .add(Tags.Items.STRINGS, new ItemInsulation(0.03f), false)
                .add(Tags.Items.LEATHERS, new ItemInsulation(0.07f), false)

                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:straw")).orElseThrow(), new ItemInsulation(0.06f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("minecraft:clay_ball")).orElseThrow(), new ItemInsulation(0.05f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:kaolin_clay")).orElseThrow(), new ItemInsulation(0.06f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:fire_clay")).orElseThrow(), new ItemInsulation(0.06f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("minecraft:paper")).orElseThrow(), new ItemInsulation(0.05f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:unrefined_paper")).orElseThrow(), new ItemInsulation(0.06f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:burlap_cloth")).orElseThrow(), new ItemInsulation(0.06f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:jute_fiber")).orElseThrow(), new ItemInsulation(0.04f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:silk_cloth")).orElseThrow(), new ItemInsulation(0.07f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:wool_cloth")).orElseThrow(), new ItemInsulation(0.07f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:wool")).orElseThrow(), new ItemInsulation(0.09f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:small_raw_hide")).orElseThrow(), new ItemInsulation(0.04f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:medium_raw_hide")).orElseThrow(), new ItemInsulation(0.05f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:large_raw_hide")).orElseThrow(), new ItemInsulation(0.06f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:small_prepared_hide")).orElseThrow(), new ItemInsulation(0.04f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:medium_prepared_hide")).orElseThrow(), new ItemInsulation(0.05f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:large_prepared_hide")).orElseThrow(), new ItemInsulation(0.06f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:small_scraped_hide")).orElseThrow(), new ItemInsulation(0.04f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:medium_scraped_hide")).orElseThrow(), new ItemInsulation(0.05f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:large_scraped_hide")).orElseThrow(), new ItemInsulation(0.06f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:small_soaked_hide")).orElseThrow(), new ItemInsulation(0.04f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:medium_soaked_hide")).orElseThrow(), new ItemInsulation(0.05f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:large_soaked_hide")).orElseThrow(), new ItemInsulation(0.06f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:small_sheepskin_hide")).orElseThrow(), new ItemInsulation(0.05f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:medium_sheepskin_hide")).orElseThrow(), new ItemInsulation(0.06f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:large_sheepskin_hide")).orElseThrow(), new ItemInsulation(0.07f), false)

                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("minecraft:leather_helmet")).orElseThrow(), new ItemInsulation(0.3f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("minecraft:leather_chestplate")).orElseThrow(), new ItemInsulation(0.5f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("minecraft:leather_leggings")).orElseThrow(), new ItemInsulation(0.4f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("minecraft:leather_boots")).orElseThrow(), new ItemInsulation(0.2f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/helmet/bismuth_bronze")).orElseThrow(), new ItemInsulation(-0.6f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/chestplate/bismuth_bronze")).orElseThrow(), new ItemInsulation(-0.8f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/greaves/bismuth_bronze")).orElseThrow(), new ItemInsulation(-0.7f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/boots/bismuth_bronze")).orElseThrow(), new ItemInsulation(-0.5f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/helmet/black_bronze")).orElseThrow(), new ItemInsulation(-0.4f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/chestplate/black_bronze")).orElseThrow(), new ItemInsulation(-0.6f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/greaves/black_bronze")).orElseThrow(), new ItemInsulation(-0.5f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/boots/black_bronze")).orElseThrow(), new ItemInsulation(-0.3f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/helmet/bronze")).orElseThrow(), new ItemInsulation(-0.4f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/chestplate/bronze")).orElseThrow(), new ItemInsulation(-0.6f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/greaves/bronze")).orElseThrow(), new ItemInsulation(-0.7f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/boots/bronze")).orElseThrow(), new ItemInsulation(-0.3f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/helmet/copper")).orElseThrow(), new ItemInsulation(-0.6f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/chestplate/copper")).orElseThrow(), new ItemInsulation(-0.8f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/greaves/copper")).orElseThrow(), new ItemInsulation(-0.7f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/boots/copper")).orElseThrow(), new ItemInsulation(-0.5f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/helmet/wrought_iron")).orElseThrow(), new ItemInsulation(-0.4f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/chestplate/wrought_iron")).orElseThrow(), new ItemInsulation(-0.6f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/greaves/wrought_iron")).orElseThrow(), new ItemInsulation(-0.5f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/boots/wrought_iron")).orElseThrow(), new ItemInsulation(-0.3f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/helmet/steel")).orElseThrow(), new ItemInsulation(-0.4f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/chestplate/steel")).orElseThrow(), new ItemInsulation(-0.6f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/greaves/steel")).orElseThrow(), new ItemInsulation(-0.5f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/boots/steel")).orElseThrow(), new ItemInsulation(-0.3f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/helmet/black_steel")).orElseThrow(), new ItemInsulation(-0.5f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/chestplate/black_steel")).orElseThrow(), new ItemInsulation(-0.7f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/greaves/black_steel")).orElseThrow(), new ItemInsulation(-0.6f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/boots/black_steel")).orElseThrow(), new ItemInsulation(-0.4f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/helmet/blue_steel")).orElseThrow(), new ItemInsulation(-0.5f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/chestplate/blue_steel")).orElseThrow(), new ItemInsulation(-0.7f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/greaves/blue_steel")).orElseThrow(), new ItemInsulation(-0.6f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/boots/blue_steel")).orElseThrow(), new ItemInsulation(-0.4f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/helmet/red_steel")).orElseThrow(), new ItemInsulation(-0.5f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/chestplate/red_steel")).orElseThrow(), new ItemInsulation(-0.7f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/greaves/red_steel")).orElseThrow(), new ItemInsulation(-0.6f), false)
                .add(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("tfc:metal/boots/red_steel")).orElseThrow(), new ItemInsulation(-0.4f), false);
    }

    @Override
    public @NotNull String getName() {return "Item_Insulation_Data_Map";}
}
