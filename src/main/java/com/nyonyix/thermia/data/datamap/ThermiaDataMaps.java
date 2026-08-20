package com.nyonyix.thermia.data.datamap;

import com.nyonyix.thermia.Thermia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class ThermiaDataMaps
{
    public static final DataMapType<EntityType<?>, EntityTemperatureDataMap> ENTITY_TEMPERATURE_DATA_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "entity_temperatures"), Registries.ENTITY_TYPE, EntityTemperatureDataMap.CODEC).synced(EntityTemperatureDataMap.CODEC, true).build();

    public static final DataMapType<Block, BlockTemperatureDataMap> BLOCK_TEMPERATURE_DATA_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "block_temperature"), Registries.BLOCK, BlockTemperatureDataMap.CODEC).synced(BlockTemperatureDataMap.CODEC, true).build();

    public static final DataMapType<Item, ItemInsulationDataMap> ITEM_INSULATION_DATA_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "item_insulation"), Registries.ITEM, ItemInsulationDataMap.CODEC).synced(ItemInsulationDataMap.CODEC, true).build();

    public static final DataMapType<Fluid, FluidTemperatureDataMap> FLUID_TEMPERATURE_DATA_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "fluid_temperature"), Registries.FLUID, FluidTemperatureDataMap.CODEC).synced(FluidTemperatureDataMap.CODEC, true).build();

    public static final DataMapType<Block, BlockSealDataMap> BLOCK_POROSITY_DATA_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "block_porosity"), Registries.BLOCK, BlockSealDataMap.CODEC).synced(BlockSealDataMap.CODEC, true).build();

    @SubscribeEvent
    public static void registerDataMapTypes(RegisterDataMapTypesEvent event)
    {
        event.register(ENTITY_TEMPERATURE_DATA_MAP);
        event.register(BLOCK_TEMPERATURE_DATA_MAP);
        event.register(ITEM_INSULATION_DATA_MAP);
        event.register(FLUID_TEMPERATURE_DATA_MAP);
        event.register(BLOCK_POROSITY_DATA_MAP);
    }
}
