package com.nyonyix.thermia.data.datagen.datamap;

import com.nyonyix.thermia.data.datamap.EntityTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class EntityTemperatureDataMapProvider extends DataMapProvider
{
    public EntityTemperatureDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {super(packOutput, lookupProvider);}

    @Override
    protected void gather()
    {
        builder(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP).replace(true)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:pig")).orElseThrow(), new EntityTemperatureDataMap(35, -10, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:rabbit")).orElseThrow(), new EntityTemperatureDataMap(40, -16, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:cow")).orElseThrow(), new EntityTemperatureDataMap(35, -10, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:goat")).orElseThrow(), new EntityTemperatureDataMap(25, -12, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:yak")).orElseThrow(), new EntityTemperatureDataMap(-11, -30, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:alpaca")).orElseThrow(), new EntityTemperatureDataMap(20, -8, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:sheep")).orElseThrow(), new EntityTemperatureDataMap(30, 1, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:musk_ox")).orElseThrow(), new EntityTemperatureDataMap(-1, -25, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:chicken")).orElseThrow(), new EntityTemperatureDataMap(40, 14, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:duck")).orElseThrow(), new EntityTemperatureDataMap(30, -25, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:quail")).orElseThrow(), new EntityTemperatureDataMap(15, -15, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:donkey")).orElseThrow(), new EntityTemperatureDataMap(40, -15, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:mule")).orElseThrow(), new EntityTemperatureDataMap(40, -15, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("tfc:horse")).orElseThrow(), new EntityTemperatureDataMap(40, -15, true, true), false)
                .add(BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse("minecraft:player")).orElseThrow(), new EntityTemperatureDataMap(26, 10, false, false), false);
    }

    @Override
    public @NotNull String getName() { return "Entity_Temperature_DataMap";}
}
