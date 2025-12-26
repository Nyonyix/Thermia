package com.nyonyix.thermia.data.manager;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.BlockSearchResult;
import com.nyonyix.thermia.data.SolarShadeResult;
import com.nyonyix.thermia.data.ThermiaDamageTypes;
import com.nyonyix.thermia.data.attachment.EntityDebug;
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
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.common.player.IPlayerInfo;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateModel;
import net.dries007.tfc.util.tracker.WeatherHelpers;
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
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
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
                if (!value.toString().equals(entry.getValue().toString())) return 0.0f;
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
                float occlusionFactor = blockSearchResult.blockOcclusions().getOrDefault(pos, 1.0f);
                float distantTemp = (temp * occlusionFactor) / (effectiveDistance * effectiveDistance);

                totalTemperature += distantTemp / 10;
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

    private static float getEntitySubmersion(Entity entity)
    {
        double waterHeight = entity.getFluidTypeHeight(NeoForgeMod.WATER_TYPE.value());
        double saltWaterHeight = entity.getFluidTypeHeight(TFCFluids.SALT_WATER.getType());
        double springWaterHeight = entity.getFluidTypeHeight(TFCFluids.SPRING_WATER.getType());

        double fluidHeight = Math.max(waterHeight, Math.max(saltWaterHeight, springWaterHeight));
        double entityHeight = entity.getBbHeight();

        if (fluidHeight <= 0) return 0.0f;
        if (fluidHeight >= entityHeight) return 1.0f;

        return Mth.clamp((float) (fluidHeight / entityHeight), 0.0f, 1.0f);
    }

    private static EntityTemperature handlePlayerSweat(Entity entity, EntityTemperature entityTemperature)
    {
        float heatStress = entityTemperature.internalTemperature() - entityTemperature.maxInternalTemperature();

        if (entity instanceof IPlayerInfo playerInfo)
        {
            float currentHydration = playerInfo.getThirst();
            float sweatEfficiency = Mth.clampedMap(currentHydration, 0f, 50f, 0.1f, 1.0f);
//            float sweatEfficiency = (currentHydration / 100) * (currentHydration / 100);

            if (heatStress > -5)
            {
                float baseSweatRate = Mth.clampedMap(heatStress, -5f, 5f, 0f, 0.05f);
                float actualSweatRate = baseSweatRate * sweatEfficiency;
                float newWetness = Math.min(1.0f, entityTemperature.wetness() + actualSweatRate);

                entityTemperature = entityTemperature.withWetness(newWetness);

                float hydrationLoss = baseSweatRate * 10;
                playerInfo.addThirst(-hydrationLoss);
            }

            return entityTemperature;
        }
        else
        {
            if (heatStress > -5) entityTemperature = entityTemperature.withWetness(Math.min(1.0f, entityTemperature.wetness() + Mth.clampedMap(heatStress, -5f, 5f, 0f, 0.05f)));
            return entityTemperature;
        }
    }

    public static void init(Entity entity)
    {
        EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
        if (dataMap == null) return;
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;

        if (dataMap.isTamed())
        {
            if (entity instanceof TFCAnimalProperties tfcAnimal)
            {
                if (tfcAnimal.getFamiliarity() >= 0.1)
                {
                    entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, EntityTemperature.createDefault().withMinInternalTemperature(dataMap.minEntityTemperature()).withMaxInternalTemperature(dataMap.maxEntityTemperature()));
                    entity.setData(ThermiaAttachments.ENTITY_DEBUG, EntityDebug.createDefault());
                    return;
                }
                else return;
            } else
            {
                LOGGER.error("Entity {} is marked 'tamable' but no tamable entity found", entity.getName());
                return;
            }
        }

        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, EntityTemperature.createDefault().withMinInternalTemperature(dataMap.minEntityTemperature()).withMaxInternalTemperature(dataMap.maxEntityTemperature()));
        entity.setData(ThermiaAttachments.ENTITY_DEBUG, EntityDebug.createDefault());
    }

    public static void onUpdate(Level level, Entity entity)
    {
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE) && entity.isAlive())
        {
            EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
            EntityDebug entityDebug = EntityDebug.createDefault();
            EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
            CompletableFuture<BlockSearchResult> pendingBlockSearch = pendingBlockSearches.get(entity.getUUID());
            ICalendar levelCalender = Calendars.get(level);
            ClimateModel levelModel = Climate.get(level);

            if (entity instanceof TFCAnimalProperties tfcAnimal) if (tfcAnimal.getFamiliarity() < 0.1) entityData = entityData.withToRemove(true);
            if (entityData.toRemove())
            {
                CompletableFuture<BlockSearchResult> pending = pendingBlockSearches.remove(entity.getUUID());
                if(pending != null && !pending.isDone()) pending.cancel(true);
                entity.removeData(ThermiaAttachments.ENTITY_TEMPERATURE);
                return;
            }

            BlockPos pos = entity.blockPosition();
            RandomSource random = level.random;
            long calendarTick = levelCalender.getTicks();
            float fractionOfDay = levelCalender.getCalendarFractionOfDay();
            float fractionOfYear = levelCalender.getCalendarFractionOfYear();
            float fractionOfMonth = levelCalender.getCalendarFractionOfMonth();
            float hemisphereScale = levelModel.hemisphereScale();
            float baseTemperature = levelModel.getTemperature(level, pos);
            float nearbyBlockTemperature = 0f;

            int nonEmptyAbove = 0;
            if (level.hasChunk(pos.getX() / 16, pos.getZ() / 16)) nonEmptyAbove = BlockSearch.SearchForBlock.depthEncasedBlocks(level.getChunkAt(pos), pos);

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

            nearbyBlockTemperature = parseBlockSearchResult(level, pos, entityData.blockSearchResult());
            entityData = entityData.withEnvironmentHumidity(EnvironmentHelpers.getEntityHumidity(level.getChunkAt(pos).getData(ThermiaAttachments.CHUNK_HUMIDITY).humidity(), nonEmptyAbove));
            float shade = 0.3f;
            if (dataMap.isMob())
            {
                float ambientTemperature = EnvironmentHelpers.calcEffectiveTemperature(level, pos.above(), baseTemperature, entityData.environmentHumidity(), level.canSeeSky(pos) ? 1.0f: shade, entityData.wetness());
                entityData = entityData.withEnvironmentTemperature(ambientTemperature + nearbyBlockTemperature);

                entityDebug = entityDebug.withSolarShadeResult(SolarShadeResult.createDefault());
            }
            else
            {
                SkyPos sunPos = SolarCalculator.getSunPosition(pos.getZ(), hemisphereScale, fractionOfYear, fractionOfDay);
                SolarShadeResult shadeResult = BlockSearch.SearchForBlock.getSolarShade(level, pos.above(), sunPos.zenith(), sunPos.azimuth());
                entityDebug = entityDebug.withSolarShadeResult(shadeResult);
                shade = shadeResult.shade();

                float ambientTemperature = EnvironmentHelpers.calcEffectiveTemperature(level, pos.above(), baseTemperature, entityData.environmentHumidity(), shadeResult.shade(), entityData.wetness());
                entityData = entityData.withEnvironmentTemperature(ambientTemperature + nearbyBlockTemperature);
            }

            entityData = handlePlayerSweat(entity, entityData);

            boolean isRaining = WeatherHelpers.isPrecipitating(levelModel.getRain(levelCalender.getCalendarTicks()), levelModel.getRainfall(level, pos));
            if (isRaining && level.canSeeSky(pos) && entityData.wetness() <= 0.9)
            {
                if (levelModel.getTemperature(level, pos) > 0.0f) entityData = entityData.withWetness(Math.min(0.9f, entityData.wetness() + 0.1f));
                else if (levelModel.getTemperature(level, pos) > -5) entityData = entityData.withWetness(Math.min(0.9f, entityData.wetness() + 0.05f));
            }
            else if (entityData.wetness() > 0.0f ) entityData = entityData.withWetness(Math.max(0.0f, entityData.wetness() - EnvironmentHelpers.calcDryingRate(level, pos.above(), entityData.environmentTemperature(), entityData.environmentHumidity(), shade)));

            float delta = entityData.environmentTemperature() - entityData.internalTemperature();
            float tempChange = calcTemperatureChangeRate(delta);
            entityData = entityData.withInternalTemperature(entityData.internalTemperature() + delta * tempChange * 0.05f);

            if (entityData.internalTemperature() >= dataMap.maxEntityTemperature()) entity.hurt(ThermiaDamageTypes.hyperDamageSource(level.registryAccess()), 1.0f);
            if (entityData.internalTemperature() <= dataMap.minEntityTemperature()) entity.hurt(ThermiaDamageTypes.hypoDamageSource(level.registryAccess()), 1.0f);

            entity.setData(ThermiaAttachments.ENTITY_DEBUG, entityDebug.withBlockSearchResult(entityData.blockSearchResult()).withWindOcclusionResult(EnvironmentHelpers.getWindOcclusion(level, pos.above())));
            entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData);

            if (!pendingBlockSearches.containsKey(entity.getUUID()))
            {
                int searchRadius = dataMap.isMob() ? ServerConfig.ISMOB_SEARCH_RANGE.getAsInt() : ServerConfig.SEARCH_RANGE.getAsInt();
                CompletableFuture<BlockSearchResult> future = BlockSearch.SearchForBlock.searchAllAsync(level, pos, searchRadius);
                pendingBlockSearches.put(entity.getUUID(), future);
            }
        }
    }

    public static void onTick(Level level, Entity entity)
    {
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE) && entity.isAlive())
        {
            EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
            EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);

            float submersion = getEntitySubmersion(entity);
            if (entityData.wetness() < submersion ) entityData = entityData.withWetness(submersion);

            entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData);
        }
    }
}
