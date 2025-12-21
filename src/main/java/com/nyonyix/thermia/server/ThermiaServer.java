package com.nyonyix.thermia.server;

import  com.mojang.logging.LogUtils;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.datagen.BlockTemperatureDataMapProvider;
import com.nyonyix.thermia.data.datagen.EntityTemperatureDataMapProvider;
import com.nyonyix.thermia.data.datagen.ThermiaDamageTypesDataGen;
import com.nyonyix.thermia.data.manager.ChunkHumidityManager;
import com.nyonyix.thermia.data.manager.EntityTemperatureManager;
import com.nyonyix.thermia.data.map.*;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Thermia.MODID)
public class ThermiaServer
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Set<Item> INSULATING_ITEMS = new HashSet<>();

    private static void initDataMap()
    {
        LOGGER.info("Init Entity Data Map");

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

        LOGGER.info("Init Block Data Map");

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

        LOGGER.info("Init Item Insulation Data Map");

        BuiltInRegistries.ITEM.holders().forEach(itemReference ->
        {
            ItemInsulation insulation = itemReference.getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);
            INSULATING_ITEMS.clear();

            if (insulation != null)
            {
                ResourceLocation itemKey = itemReference.key().location();
                LOGGER.info("Item: {}, insulationModifier: {}", itemKey, insulation.insulationModifier());

                INSULATING_ITEMS.add(itemReference.value());
            }
        });
    }

    @SubscribeEvent
    static void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        gen.addProvider(event.includeServer(), new BlockTemperatureDataMapProvider(packOutput, lookupProvider));
        gen.addProvider(event.includeServer(), new EntityTemperatureDataMapProvider(packOutput, lookupProvider));

        gen.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, ThermiaDamageTypesDataGen::bootstrap), Set.of(Thermia.MODID)));
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {event.addListener(((preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> preparationBarrier.wait(null).thenRunAsync(ThermiaServer::initDataMap)));}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {initDataMap();}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event)
    {
        MinecraftServer server = event.getServer();

        for (ServerLevel level : server.getAllLevels())
        {
            for (Entity entity : level.getAllEntities())
            {
                if (server.getTickCount() % 20 == entity.getId() % 20)
                {
                    EntityTemperatureManager.init(entity);
                    EntityTemperatureManager.onTick(level, entity);
                }
            }

            if (ChunkHumidityManager.lastTickedTFCHour != Calendars.get(level).getHourOfDay())
            {
                ChunkHumidityManager.lastTickedTFCHour = Calendars.get(level).getHourOfDay();
                ChunkHumidityManager.refreshWorkingCache(level);
            }

            ChunkHumidityManager.processChunkBatch(level, 100);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event)
    {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        ChunkPos pos = chunk.getPos();

        chunk.getData(ThermiaAttachments.CHUNK_HUMIDITY);
        ChunkHumidityManager.loadedChunkCache.computeIfAbsent(level, k -> new HashSet<>()).add(pos);
        ChunkHumidityManager.workingChunkCache.computeIfAbsent(level, k -> new HashSet<>()).add(pos);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event)
    {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        ChunkPos pos = chunk.getPos();

        Set<ChunkPos> chunks = ChunkHumidityManager.loadedChunkCache.get(level);
        if (chunks != null) chunks.remove(pos);

        Set<ChunkPos> workingChunks = ChunkHumidityManager.workingChunkCache.get(level);
        if (workingChunks != null) workingChunks.remove(pos);
    }
}
