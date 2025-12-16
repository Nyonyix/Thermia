//package com.nyonyix.thermia;
//
//import com.mojang.logging.LogUtils;
//import com.nyonyix.thermia.data.map.ItemInsulation;
//import com.nyonyix.thermia.data.ThermiaDamageTypes;
//import com.nyonyix.thermia.data.map.BlockTemperatureDataMap;
//import com.nyonyix.thermia.data.map.EntityTemperatureDataMap;
//import com.nyonyix.thermia.data.map.ThermiaDataMaps;
//import com.nyonyix.thermia.util.BlockSearch;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.animal.Chicken;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.block.Block;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.event.AddReloadListenerEvent;
//import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
//import net.neoforged.neoforge.event.server.ServerStartedEvent;
//import net.neoforged.neoforge.event.tick.ServerTickEvent;
//import org.slf4j.Logger;
//
//@EventBusSubscriber(modid = Thermia.MODID)
//public class ThermiaEvents
//{
//
//    private static final Logger LOGGER = LogUtils.getLogger();
//
//    private static void verifyDataMap()
//    {
//        LOGGER.info("Verify Entity Data Map");
//
//        BuiltInRegistries.ENTITY_TYPE.holders().forEach(entityTypeReference ->
//        {
//            EntityTemperatureDataMap entityTemp = entityTypeReference.getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
//
//            if (entityTemp != null)
//            {
//                ResourceLocation entityKey = entityTypeReference.key().location();
//                LOGGER.info("Entity: {}, maxTemp: {}, minTemp:  {}, isMob: {}, isTamed: {}", entityKey, entityTemp.maxEntityTemperature(), entityTemp.minEntityTemperature(), entityTemp.isMob(), entityTemp.isTamed());
//
//                if (!entityTemp.isMob() && entityTemp.isTamed())
//                {
//                    throw new IllegalStateException("isTamed = true while isMob = false");
//                }
//                if (entityTemp.minEntityTemperature() > entityTemp.maxEntityTemperature())
//                {
//                    throw new IllegalStateException("minEntityTemperature > maxEntityTemperature");
//                }
//            }
//        });
//
//        LOGGER.info("Verify Block Data Map");
//
//        BuiltInRegistries.BLOCK.holders().forEach(blockReference ->
//        {
//            BlockTemperatureDataMap blockTemp = blockReference.getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
//
//            if (blockTemp != null)
//            {
//                ResourceLocation blockKey = blockReference.key().location();
//                LOGGER.info("Block: {}, Temp: {}, searchCap: {}, hasTFCHeat: {}", blockKey, blockTemp.temperature(), blockTemp.searchCap(), blockTemp.hasTFCHeat());
//
//                if (blockTemp.temperature() != 0.0f && blockTemp.hasTFCHeat())
//                {
//                    throw new IllegalStateException("Block temperature > 0 while hasTFCHeat = true");
//                }
//                if (blockTemp.searchCap() < 0)
//                {
//                    throw new IllegalStateException("Block searchCap is < 0");
//                }
//            }
//        });
//
//        LOGGER.info("Verify Item Insulation Data Map");
//
//        BuiltInRegistries.ITEM.holders().forEach(itemReference ->
//        {
//            ItemInsulation insulation = itemReference.getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);
//
//            if (insulation != null)
//            {
//                ResourceLocation itemKey = itemReference.key().location();
//                LOGGER.info("Item: {}, insulationModifier: {}", itemKey, insulation.insulationModifier());
//            }
//        });
//    }
//
//    @SubscribeEvent
//    public static void onReload(AddReloadListenerEvent event)
//    {
//        event.addListener(((preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> preparationBarrier.wait(null).thenRunAsync(ThermiaEvents::verifyDataMap)));
//    }
//
//    @SubscribeEvent
//    public static void onServerStarted(ServerStartedEvent event)
//    {
//        verifyDataMap();
//    }
//
//    private static void lavaScan(Chicken chicken)
//    {
//        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("minecraft:lava"));
//
//        BlockSearch.SearchForBlock.searchAllAsync(chicken.level(), new BlockPos(chicken.getBlockX(), chicken.getBlockY(), chicken.getBlockZ()), ServerConfig.MAX_SEARCH_RANGE.getAsInt(), 32, block)
//                .thenAccept(blockSearchResult ->
//                {
//                    LOGGER.info("There is {} lava blocks", blockSearchResult.nearest());
//                    LOGGER.info("Closest is {} blocks away and is {}", blockSearchResult.nearestDistance(), chicken.level().getBlockState(blockSearchResult.nearest()).getBlock().getName());
//                });
//    }
//
//    @SubscribeEvent
//    public static void onServerTick(ServerTickEvent.Post event)
//    {
//        MinecraftServer server = event.getServer();
//
//        if (server.getTickCount() % 20 == 0)
//        {
//            for (ServerLevel level : server.getAllLevels())
//            {
//                for (Chicken chicken : level.getEntities(EntityType.CHICKEN, chicken -> true))
//                {
//                    lavaScan(chicken);
//                }
//            }
//        }
//
//        if (server.getTickCount() % 100 == 0)
//        {
//            for (ServerLevel level : server.getAllLevels())
//            {
//                for (Player player : level.getEntities(EntityType.PLAYER, player -> true))
//                {
//                    player.hurt(ThermiaDamageTypes.hyperDamageSource(level.registryAccess()), 1f);
//                }
//            }
//        }
//    }
//
////    @SubscribeEvent
////    public static void onEntityJoinLevel(EntityJoinLevelEvent event)
////    {
////        Entity entity = event.getEntity();
////
////        if (entity.level().isClientSide()) return;
////
////    }
//}
