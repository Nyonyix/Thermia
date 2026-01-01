package com.nyonyix.thermia.data.datagen;

import com.nyonyix.thermia.data.map.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockTemperatureDataMapProvider extends DataMapProvider
{
    public BlockTemperatureDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {super(packOutput, lookupProvider);}

    @Override
    protected void gather()
    {
        builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).replace(true)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/granite")).orElseThrow(), new BlockTemperatureDataMap(800f, 16, false,true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/diorite")).orElseThrow(), new BlockTemperatureDataMap(800f, 16, false, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/gabbro")).orElseThrow(), new BlockTemperatureDataMap(800f, 16, false, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/rhyolite")).orElseThrow(), new BlockTemperatureDataMap(800f, 16, false, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/andesite")).orElseThrow(), new BlockTemperatureDataMap(800f, 16, false, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/dacite")).orElseThrow(), new BlockTemperatureDataMap(800f, 16, false, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/basalt")).orElseThrow(), new BlockTemperatureDataMap(800f, 16, false, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("minecraft:magma_block")).orElseThrow(), new BlockTemperatureDataMap(800f, 16, false, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:pit_kiln")).orElseThrow(), new BlockTemperatureDataMap(1800f, 16, false, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("minecraft:fire")).orElseThrow(), new BlockTemperatureDataMap(600f, 32, false, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:burning_log_pile")).orElseThrow(), new BlockTemperatureDataMap(900f, 32, false, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:bloomery")).orElseThrow(), new BlockTemperatureDataMap(1200f, 8, false, true, Map.of("lit", true), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:molten")).orElseThrow(), new BlockTemperatureDataMap(1540f, 8, false, true, Map.of("lit", true), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:crucible")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, true, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:firepit")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, true, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:pot")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, true, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:grill")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, true, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:blast_furnace")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, true, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:charcoal_forge")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, true, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:stove")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, true, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:stove_pot")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, true, true, Map.of(), Map.of(), Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:firebox")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, true, true, Map.of(), Map.of(), Map.of()), false);

//                .add(BlockTags.SNOW, new BlockTemperatureDataMap(-5f, 4, false, false, Map.of(), Map.of(), Map.of()), false)
//                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("minecraft:ice")).orElseThrow(), new BlockTemperatureDataMap(-10f, 4, false, false, Map.of(), Map.of(), Map.of()), false)
//                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("minecraft:packed_ice")).orElseThrow(), new BlockTemperatureDataMap(-15f, 4, false, false, Map.of(), Map.of(), Map.of()), false)
//                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("minecraft:blue_ice")).orElseThrow(), new BlockTemperatureDataMap(-20f, 4, false, false, Map.of(), Map.of(), Map.of()), false);
    }

    @Override
    public @NotNull String getName() { return "Block_Temperature_DataMap";}
}
