package com.nyonyix.thermia.server;

import  com.mojang.logging.LogUtils;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.ThermiaCommands;
import com.nyonyix.thermia.data.datagen.lang.ThermiaLanguageProvider;
import com.nyonyix.thermia.data.ThermiaTags;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.datagen.loot.ThermiaLootModifierProvider;
import com.nyonyix.thermia.data.datagen.recipe.ThermiaRecipeProvider;
import com.nyonyix.thermia.data.datagen.damage.ThermiaDamageTypesDataGen;
import com.nyonyix.thermia.data.datagen.datamap.*;
import com.nyonyix.thermia.data.datagen.model.ThermiaItemModelProvider;
import com.nyonyix.thermia.data.datagen.tags.ThermiaBlockTagProvider;
import com.nyonyix.thermia.data.datagen.tags.ThermiaItemTagProvider;
import com.nyonyix.thermia.data.manager.ChunkHumidityManager;
import com.nyonyix.thermia.data.manager.EntityTemperatureManager;
import com.nyonyix.thermia.data.datamap.*;
import com.nyonyix.thermia.data.manager.InteriorManager;
import net.dries007.tfc.util.calendar.Calendar;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Thermia.MODID)
public class ThermiaServer
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private static void initDataMap()
    {
        BuiltInRegistries.ENTITY_TYPE.holders().forEach(entityType ->
        {
            EntityTemperatureDataMap dataMap = entityType.getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
            if (dataMap == null) return;

            LOGGER.debug("Entity: {}, maxTemperature: {}, minTemperature: {}, isMob: {}, isTamed: {}}", entityType.value().getDescriptionId(), dataMap.maxEntityTemperature(), dataMap.minEntityTemperature(), dataMap.isMob(), dataMap.isTamed());
            VerifyDataMap.isValidEntityTemperature(dataMap, entityType.value());
        });

        BuiltInRegistries.BLOCK.holders().forEach(block ->
        {
            BlockTemperatureDataMap dataMap = block.getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (dataMap == null) return;

            LOGGER.debug("Block: {}, temperature: {}, searchCap: {}, hasTFCHeat: {}, isRadiative: {}, isHomeBlock: {}, stateTemps: {}", block.value().getDescriptionId(), dataMap.temperature(), dataMap.searchCap(), dataMap.hasTFCHeat(), dataMap.isRadiative(), dataMap.stateTemps().toString());
            VerifyDataMap.isValidBlockTemperature(dataMap, block.value());
        });

        BuiltInRegistries.FLUID.holders().forEach(fluid ->
        {
            FluidTemperatureDataMap dataMap = fluid.getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (dataMap == null) return;

            LOGGER.debug("Fluid: {}, temperature: {}, searchCap: {}, isRadiative: {}", fluid.value().getFluidType().getDescriptionId(), dataMap.temperature(), dataMap.searchCap(), dataMap.isRadiative());
            VerifyDataMap.isValidFluidTemperature(dataMap, fluid.value());
        });

        BuiltInRegistries.ITEM.holders().forEach(item ->
        {
            ItemInsulationDataMap dataMap = item.getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);
            if (dataMap == null) return;

            LOGGER.debug("Item: {}, conductionProtection: {}", item.value(), dataMap.conductionProtection());
            VerifyDataMap.isValidItemInsulation(dataMap, item.value());
        });

        BuiltInRegistries.BLOCK.holders().forEach( block ->
        {
            BlockPorosityDataMap dataMap = block.getData(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP);
            if (dataMap == null) return;

            LOGGER.debug("Block: {}, statePorosity: {}, defaultPorosity: {}", block.value().getDescriptionId(), dataMap.statePorosity().toString(), dataMap.defaultPorosity());
        });
    }

    @SubscribeEvent
    static void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        var blockTagProvider = new ThermiaBlockTagProvider(packOutput, lookupProvider, event.getExistingFileHelper());

        gen.addProvider(event.includeServer(), blockTagProvider);
        gen.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, ThermiaDamageTypesDataGen::bootstrap), Set.of(Thermia.MODID)));
        gen.addProvider(event.includeServer(), new ThermiaItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), event.getExistingFileHelper()));
        gen.addProvider(event.includeServer(), new BlockTemperatureDataMapProvider(packOutput, lookupProvider));
        gen.addProvider(event.includeServer(), new EntityTemperatureDataMapProvider(packOutput, lookupProvider));
        gen.addProvider(event.includeServer(), new ItemInsulationDataMapProvider(packOutput, lookupProvider));
        gen.addProvider(event.includeServer(), new FluidTemperatureDataMapProvider(packOutput, lookupProvider));
        gen.addProvider(event.includeServer(), new BlockPorosityDataMapProvider(packOutput, lookupProvider));
        gen.addProvider(event.includeServer(), new ThermiaLootModifierProvider(packOutput, lookupProvider));
        gen.addProvider(event.includeServer(), new ThermiaRecipeProvider(packOutput, lookupProvider));

        gen.addProvider(event.includeClient(), new ThermiaItemModelProvider(packOutput, event.getExistingFileHelper()));
        gen.addProvider(event.includeClient(), new ThermiaLanguageProvider(packOutput));
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {event.addListener(((preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> preparationBarrier.wait(null).thenRunAsync(ThermiaServer::initDataMap)));}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {initDataMap();}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {ThermiaCommands.register(event.getDispatcher());}

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event)
    {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event)
    {
        MinecraftServer server = event.getServer();

        for (ServerLevel level : server.getAllLevels())
        {
            List<Entity> entitySnapshot = new ArrayList<>();
            try
            {
                for (Entity entity : level.getAllEntities())
                {
                    if (entity != null && entity.isAlive()) entitySnapshot.add(entity);
                }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                LOGGER.debug("Entity iteration interrupted: {}", e.getMessage());
                continue;
            }

            for (Entity entity : entitySnapshot)
            {
                if (entity.isRemoved()) continue;

                EntityTemperatureManager.onTick(entity);

                if (server.getTickCount() % 20 == entity.getId() % 20)
                {
                    EntityTemperatureManager.init(entity);
                    EntityTemperatureManager.onUpdate(level, entity);

//                    if (entity instanceof Player player) LOGGER.debug("Player: {}, inside interior {}", player.getDisplayName().getString(), InteriorManager.getInteriorByPos(level, player.getOnPos().relative(Direction.UP)).homePos());
                }

                if (server.getTickCount() % 20 == entity.getId() % 100) EntityTemperatureManager.queueBlockSearch(entity);

                int ticksInHour = Calendar.CALENDAR_TICKS_IN_HOUR;
                int refreshTicks = 12 * ticksInHour;

                if (Calendars.get(level).getCalendarTicks() % refreshTicks == 0) EntityTemperatureManager.handleEntityAcclimatization(entity);
            }

            if (ChunkHumidityManager.lastTickedTFCHour != Calendars.get(level).getHourOfDay())
            {
                ChunkHumidityManager.lastTickedTFCHour = Calendars.get(level).getHourOfDay();
                ChunkHumidityManager.refreshWorkingCache(level);
            }

            InteriorManager.onTick(level, server.getTickCount());

            ChunkHumidityManager.processChunkBatch(level, 64);
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

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (!event.getEntity().isShiftKeyDown()) return;
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        if (!player.getMainHandItem().isEmpty()) return;

        Level level = event.getLevel();
        BlockPos startPos = event.getPos().relative(Direction.UP, 1);
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(ThermiaTags.Blocks.INTERIOR_TRIGGERS))
        {
            InteriorManager.onCreateEvent(level, startPos);
            player.sendSystemMessage(Component.literal("Right clicked: ").append(state.getBlock().getName().withStyle(ChatFormatting.DARK_GREEN)));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        InteriorManager.invalidateAndRescan(serverLevel, event.getPos());
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event)
    {
        if (event.getLevel().isClientSide) return;

        InteriorManager.invalidateAndRescan(event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event)
    {
        if (event.getLevel().isClientSide) return;

        for (BlockPos pos : event.getAffectedBlocks())
        {
            InteriorManager.invalidateAndRescan(event.getLevel(), pos);        }
    }

    @SubscribeEvent
    public static void onEntityPlace(BlockEvent.EntityPlaceEvent event)
    {
        if (!(event.getLevel() instanceof ServerLevel serverLEvel)) return;

        InteriorManager.invalidateAndRescan(serverLEvel, event.getPos());
    }

    @SubscribeEvent
    public static void onEntityMuliPlace(BlockEvent.EntityMultiPlaceEvent event)
    {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        InteriorManager.invalidateAndRescan(serverLevel, event.getPos());
    }
}
