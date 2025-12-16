package com.nyonyix.thermia.server;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.map.ItemInsulation;
import com.nyonyix.thermia.data.map.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.map.EntityTemperatureDataMap;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Thermia.MODID)
public class ThermiaServer
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<LevelChunk, Integer> chunkUpdateHour = new HashMap<>();
    private static final int TICKS_TO_STAGGER = 10;
    private static final int CHUNKS_PER_UPDATE = 100;
    private static int chunkUpdateOffset = 0;
    private static int lastTickedTFCHour = -1;

    private static void verifyDataMap()
    {
        LOGGER.info("Verify Entity Data Map");

        BuiltInRegistries.ENTITY_TYPE.holders().forEach(entityTypeReference ->
        {
            EntityTemperatureDataMap entityTemp = entityTypeReference.getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);

            if (entityTemp != null)
            {
                ResourceLocation entityKey = entityTypeReference.key().location();
                LOGGER.info("Entity: {}, maxTemp: {}, minTemp:  {}, isMob: {}, isTamed: {}", entityKey, entityTemp.maxEntityTemperature(), entityTemp.minEntityTemperature(), entityTemp.isMob(), entityTemp.isTamed());

                if (!entityTemp.isMob() && entityTemp.isTamed())
                {
                    throw new IllegalStateException("isTamed = true while isMob = false");
                }
                if (entityTemp.minEntityTemperature() > entityTemp.maxEntityTemperature())
                {
                    throw new IllegalStateException("minEntityTemperature > maxEntityTemperature");
                }
            }
        });

        LOGGER.info("Verify Block Data Map");

        BuiltInRegistries.BLOCK.holders().forEach(blockReference ->
        {
            BlockTemperatureDataMap blockTemp = blockReference.getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);

            if (blockTemp != null)
            {
                ResourceLocation blockKey = blockReference.key().location();
                LOGGER.info("Block: {}, Temp: {}, searchCap: {}, hasTFCHeat: {}", blockKey, blockTemp.temperature(), blockTemp.searchCap(), blockTemp.hasTFCHeat());

                if (blockTemp.temperature() != 0.0f && blockTemp.hasTFCHeat())
                {
                    throw new IllegalStateException("Block temperature > 0 while hasTFCHeat = true");
                }
                if (blockTemp.searchCap() < 0)
                {
                    throw new IllegalStateException("Block searchCap is < 0");
                }
            }
        });

        LOGGER.info("Verify Item Insulation Data Map");

        BuiltInRegistries.ITEM.holders().forEach(itemReference ->
        {
            ItemInsulation insulation = itemReference.getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);

            if (insulation != null)
            {
                ResourceLocation itemKey = itemReference.key().location();
                LOGGER.info("Item: {}, insulationModifier: {}", itemKey, insulation.insulationModifier());
            }
        });
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {event.addListener(((preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> preparationBarrier.wait(null).thenRunAsync(ThermiaServer::verifyDataMap)));}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {verifyDataMap();}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event)
    {
        MinecraftServer server = event.getServer();

        for (ServerLevel level : server.getAllLevels())
        {
        }
    }
}
