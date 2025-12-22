package com.nyonyix.thermia.data.manager;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.BlockSearchResult;
import com.nyonyix.thermia.data.SolarShadeResult;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.map.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.map.EntityTemperatureDataMap;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import com.nyonyix.thermia.util.BlockSearch;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.client.overworld.SkyPos;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.common.blockentities.CharcoalForgeBlockEntity;
import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTemperatureManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, CompletableFuture<BlockSearchResult>> pendingBlockSearches = new ConcurrentHashMap<>();

    private static boolean shouldGetSystem(Entity entity)
    {
        EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
        if (dataMap == null) return false;

        if (entity instanceof TFCAnimalProperties tfcAnimalProperties)
        {
            if (tfcAnimalProperties.getFamiliarity() >= 0.15f) return true;
            else
            {
                if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).withToRemove(true));
            }
            return tfcAnimalProperties.getFamiliarity() >= 0.15f;
        }

        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        if (!dataMap.isMob()) return true;

        return false;
    }

    private static float parseBlockState(BlockState state, Level level, BlockPos pos, BlockTemperatureDataMap dataMap)
    {
        StateDefinition<Block, BlockState> stateDef = state.getBlock().getStateDefinition();
        float temp = dataMap.temperature();

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IHeatable heatable) return heatable.getTemperature();
        if (blockEntity instanceof CharcoalForgeBlockEntity charcoalForge) return charcoalForge.getTemperature();

        for (Map.Entry<String, Boolean> entry : dataMap.stateBools().entrySet())
        {
            Property<?> property = stateDef.getProperty(entry.getKey());

            if (property != null)
            {
                Comparable<?> value = state.getValue(property);
                if (value.toString().equals(entry.getValue().toString())) return 0.0f;
            }
            else LOGGER.error("Property of {} was not found for block {}", entry.getKey(), state.getBlock().getDescriptionId());
        }

        return temp;
    }

    private static float parseBlockSearchResult(Level curLevel, BlockPos curPos, BlockSearchResult blockSearchResult)
    {
        float totalTemperature = 0f;
        for (Map.Entry<Block, List<BlockPos>> entry : blockSearchResult.allPositions().entrySet())
        {
            if (blockSearchResult.levelID() != curLevel.dimension()) return 0.0f;

            Block block = entry.getKey();
            BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (dataMap == null) continue;

            int blockLimit = Math.min(dataMap.searchCap(), entry.getValue().size());

            for (int i = 0 ; i < blockLimit ; i++)
            {
                BlockPos pos = entry.getValue().get(i);
                if (!curLevel.hasChunk(pos.getX() / 16, pos.getZ() / 16)) continue;

                BlockState state = curLevel.getBlockState(pos);

                float temp = parseBlockState(state, curLevel, pos, dataMap);
                if (temp == 0f) continue;

                float distance = (float) Math.sqrt(blockSearchResult.searchOrigin().distSqr(pos));
                float effectiveDistance = Math.max(distance, 1f);
                float distantTemp = temp / (effectiveDistance * effectiveDistance);

                totalTemperature += distantTemp;
            }
        }

        float maxRadiance = (float) ServerConfig.MAX_RADIANT_HEATING.getAsInt();
        return maxRadiance * (1f - (float) Math.exp(-totalTemperature / maxRadiance));
    }

    private static float calcTemperatureChangeRate(float delta)
    {
        float absDelta = Math.abs(delta);
        float minRate = (float) ServerConfig.TEMP_CHANGE_MIN_RATE.getAsDouble();
        float maxRate = (float) ServerConfig.TEMP_CHANGE_MAX_RATE.getAsDouble();
        float scale = (float) ServerConfig.TEMP_CHANGE_SCALE.getAsDouble();

        float normalisedRate = 1.0f - (float) Math.exp(-scale * absDelta);
        return Mth.lerp(normalisedRate, minRate, maxRate);
    }

    public static void init(Entity entity)
    {
        if (shouldGetSystem(entity))
        {
            EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
            EntityTemperature entityTemp = EntityTemperature.createDefault().withMaxInternalTemperature(dataMap.maxEntityTemperature()).withMinInternalTemperature(dataMap.minEntityTemperature());

            entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityTemp);
        }
        else if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE))
        {
            if (entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).toRemove())
            {
                CompletableFuture<BlockSearchResult> pending = pendingBlockSearches.remove(entity.getUUID());
                if (pending != null && !pending.isDone()) pending.cancel(true);
                entity.removeData(ThermiaAttachments.ENTITY_TEMPERATURE);
            }
        }
    }

    public static void onTick(Level level, Entity entity)
    {
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE))
        {
            EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
            EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
            CompletableFuture<BlockSearchResult> pendingBlockSearch = pendingBlockSearches.get(entity.getUUID());

            BlockPos pos = entity.blockPosition();
            RandomSource random = level.random;
            long calendarTick = Calendars.get(level).getTicks();
            float fractionOfDay = Calendars.get(level).getCalendarFractionOfDay();
            float fractionOfYear = Calendars.get(level).getCalendarFractionOfYear();
            float fractionOfMonth = Calendars.get(level).getCalendarFractionOfMonth();
            float hemisphereScale = Climate.get(level).hemisphereScale();

            float baseTemperature = Climate.getTemperature(level, pos);
            float nearbyBlockTemperature = 0f;

            SkyPos sunPos = SolarCalculator.getSunPosition(pos.getZ(), hemisphereScale, fractionOfYear, fractionOfDay);

            if (pendingBlockSearch != null && pendingBlockSearch.isDone())
            {
                try
                {
                    BlockSearchResult newResult = pendingBlockSearch.join();
                    pendingBlockSearches.remove(entity.getUUID());

                    if (newResult.levelID() == level.dimension())  entityData = entityData.withBlockSearchResult(newResult);
                    else LOGGER.debug("Discarding invalid Block search result");
                }
                catch (Exception e)
                {
                    LOGGER.error("Error in Async block search for entity: {}", entity.getUUID(), e);
                    pendingBlockSearches.remove(entity.getUUID());
                }
            }

            nearbyBlockTemperature = parseBlockSearchResult(level, entity.blockPosition(), entityData.blockSearchResult());
            entityData = entityData.withEnvironmentHumidity(level.getChunkAt(entity.blockPosition()).getData(ThermiaAttachments.CHUNK_HUMIDITY).humidity());

            if (dataMap.isMob())
            {
                entityData = entityData.withSunOcclusionPos(SolarShadeResult.createDefault());
//                entityData = entityData.withEnvironmentTemperature(EnvironmentHelpers.calcWetBulbGlobeTemperature(level, pos, baseTemperature + nearbyBlockTemperature, entityData.environmentHumidity(), level.canSeeSky(pos) ? 1.0f : 0.3f));
                float ambientTemperature = EnvironmentHelpers.calcEffectiveTemperature(level, pos, baseTemperature + nearbyBlockTemperature, entityData.environmentHumidity(), level.canSeeSky(pos) ? 1.0f: 0.3f);
                entityData = entityData.withEnvironmentTemperature(ambientTemperature + nearbyBlockTemperature);
            }
            else
            {
                entityData = entityData.withSunOcclusionPos(BlockSearch.SearchForBlock.getSolarShade(level, pos.above(), sunPos.zenith(), sunPos.azimuth()));
//                entityData = entityData.withEnvironmentTemperature(EnvironmentHelpers.calcWetBulbGlobeTemperature(level, pos, baseTemperature + nearbyBlockTemperature, entityData.environmentHumidity(), entityData.sunOcclusionPos().shade()));
                float ambientTemperature = EnvironmentHelpers.calcEffectiveTemperature(level, pos, baseTemperature + nearbyBlockTemperature, entityData.environmentHumidity(), entityData.sunOcclusionPos().shade());
                entityData = entityData.withEnvironmentTemperature(ambientTemperature + nearbyBlockTemperature);
            }


//            entityData = entityData.withInternalTemperature(Mth.approach(entityData.internalTemperature(), entityData.environmentTemperature(), 0.1f));
            float delta = entityData.environmentTemperature() - entityData.internalTemperature();
            entityData = entityData.withInternalTemperature(entityData.internalTemperature() + delta * calcTemperatureChangeRate(delta));

            entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData);

            if (!pendingBlockSearches.containsKey(entity.getUUID()))
            {
                int searchRadius = dataMap.isMob() ? ServerConfig.ISMOB_SEARCH_RANGE.getAsInt() : ServerConfig.SEARCH_RANGE.getAsInt();
                CompletableFuture<BlockSearchResult> future = BlockSearch.SearchForBlock.searchAllAsync(level, pos, searchRadius);
                pendingBlockSearches.put(entity.getUUID(), future);
            }
        }
    }
}
