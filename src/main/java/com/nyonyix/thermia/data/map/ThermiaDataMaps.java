package com.nyonyix.thermia.data.map;

import com.nyonyix.thermia.Thermia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class ThermiaDataMaps
{
    public static final DataMapType<EntityType<?>, EntityTemperatureDataMap> ENTITY_TEMPERATURE_DATA_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "Entity_Temperatures"), Registries.ENTITY_TYPE, EntityTemperatureDataMap.CODEC).synced(EntityTemperatureDataMap.CODEC, true).build();

    public static final DataMapType<Block, BlockTemperatureDataMap> BLOCK_TEMPERATURE_DATA_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "Block_Temperature"), Registries.BLOCK, BlockTemperatureDataMap.CODEC).synced(BlockTemperatureDataMap.CODEC, true).build();

    @SubscribeEvent
    public static void registerDataMapTypes(RegisterDataMapTypesEvent event)
    {
        event.register(ENTITY_TEMPERATURE_DATA_MAP);
        event.register(BLOCK_TEMPERATURE_DATA_MAP);
    }
}
