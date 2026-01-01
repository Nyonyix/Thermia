package com.nyonyix.thermia.data.manager;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.BlockSearchResult;
import com.nyonyix.thermia.data.SolarShadeResult;
import com.nyonyix.thermia.data.ThermiaDamageTypes;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.map.EntityTemperatureDataMap;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import com.nyonyix.thermia.util.BlockSearch;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.client.overworld.SkyPos;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.common.player.IPlayerInfo;
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
import net.neoforged.neoforge.common.NeoForgeMod;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTemperatureManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, CompletableFuture<BlockSearchResult>> pendingBlockSearches = new ConcurrentHashMap<>();

    private static EntityTemperature handleTemperatureChange(Entity entity, EntityTemperature entityTemperature)
    {
        float environmentDelta = entityTemperature.environmentTemperature() - entityTemperature.internalTemperature();
        float entityMedian = (entityTemperature.maxInternalTemperature() + entityTemperature.minInternalTemperature()) / 2f;
        float medianDelta = (entityMedian - entityTemperature.internalTemperature()) * 0.5f;

        float insulation = 1f - (Mth.clamp(ItemInventoryManager.getInventoryInsulation(entity), -1.0f, 1.0f) * (1f - entityTemperature.wetness()));

        float effectiveDelta = medianDelta + (environmentDelta * insulation);

        float absDelta = Math.abs(effectiveDelta);
        float minRate = (float) ServerConfig.TEMP_CHANGE_MIN_RATE.getAsDouble();
        float maxRate = (float) ServerConfig.TEMP_CHANGE_MAX_RATE.getAsDouble();
        float scale = (float) ServerConfig.TEMP_CHANGE_SCALE.getAsDouble();

        float normalisedRate = 1.0f - (float) Math.exp(-scale * absDelta);
        float baseRate = Mth.lerp(normalisedRate, minRate, maxRate);

        return  entityTemperature.withInternalTemperature(entityTemperature.internalTemperature() + (effectiveDelta * baseRate) / 2.5f);
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
            float sweatEfficiency = Mth.clampedMap(currentHydration, 0f, 60f, 0.1f, 1.0f);

            if (heatStress > -5)
            {
                float baseSweatRate = Mth.clampedMap(heatStress, -5f, 5f, 0f, 0.05f);
                float actualSweatRate = baseSweatRate * sweatEfficiency;
                float newWetness = Math.min(1.0f, entityTemperature.wetness() + actualSweatRate);

                entityTemperature = entityTemperature.withWetness(newWetness);

                float hydrationLoss = baseSweatRate * 10;
                playerInfo.addThirst(-hydrationLoss);
            }

        }
        else
        {
            if (heatStress > -5) entityTemperature = entityTemperature.withWetness(Math.min(1.0f, entityTemperature.wetness() + Mth.clampedMap(heatStress, -5f, 5f, 0f, 0.05f)));
        }
        return entityTemperature;
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
                    float defaultTemp = (dataMap.maxEntityTemperature() + dataMap.minEntityTemperature()) / 2;
                    entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, EntityTemperature.createDefault().withMinInternalTemperature(dataMap.minEntityTemperature()).withMaxInternalTemperature(dataMap.maxEntityTemperature()).withInternalTemperature(defaultTemp));
                }
            } else
            {
                LOGGER.error("Entity {} is marked 'tamable' but no tamable entity found", entity.getName());
            }
            return;
        }

        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, EntityTemperature.createDefault().withMinInternalTemperature(dataMap.minEntityTemperature()).withMaxInternalTemperature(dataMap.maxEntityTemperature()));
    }

    public static void onUpdate(Level level, Entity entity)
    {
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE) && entity.isAlive())
        {
            EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
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
            if (level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) nonEmptyAbove = BlockSearch.depthEncasedBlocks(level.getChunkAt(pos), pos);

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

            entityData = entityData.withWindOcclusionResult(BlockSearch.getWindOcclusion(level, pos));

            nearbyBlockTemperature = entityData.blockSearchResult().parseBlockSearchResult(level);
            entityData = entityData.withEnvironmentHumidity(EnvironmentHelpers.getEntityHumidity(level.getChunkAt(pos).getData(ThermiaAttachments.CHUNK_HUMIDITY).humidity(), nonEmptyAbove));
            float shade = 0.3f;
            if (dataMap.isMob())
            {
                float ambientTemperature = EnvironmentHelpers.calcEffectiveTemperature(level, pos.above(), baseTemperature, entityData.environmentHumidity(), level.canSeeSky(pos) ? 1.0f: shade, entityData.wetness(), entityData.windOcclusionResult().occlusionMultiplier());
                entityData = entityData.withEnvironmentTemperature(ambientTemperature + nearbyBlockTemperature);
            }
            else
            {
                SkyPos sunPos = SolarCalculator.getSunPosition(pos.getZ(), hemisphereScale, fractionOfYear, fractionOfDay);
                SolarShadeResult shadeResult = BlockSearch.getSolarShade(level, pos.above(), sunPos.zenith(), sunPos.azimuth());
                entityData = entityData.withSolarShadeResult(shadeResult);
                shade = shadeResult.shade();

                float ambientTemperature = EnvironmentHelpers.calcEffectiveTemperature(level, pos.above(), baseTemperature, entityData.environmentHumidity(), shadeResult.shade(), entityData.wetness(), entityData.windOcclusionResult().occlusionMultiplier());
                float inventoryHeat = ItemInventoryManager.getInventoryTemperature(entity);
                entityData = entityData.withEnvironmentTemperature(ambientTemperature + nearbyBlockTemperature + inventoryHeat);
            }

            entityData = handlePlayerSweat(entity, entityData);

            boolean isRaining = WeatherHelpers.isPrecipitating(levelModel.getRain(levelCalender.getCalendarTicks()), levelModel.getRainfall(level, pos));
            if (isRaining && level.canSeeSky(pos) && entityData.wetness() <= 0.9)
            {
                if (levelModel.getTemperature(level, pos) > 0.0f) entityData = entityData.withWetness(Math.min(0.9f, entityData.wetness() + 0.1f));
                else if (levelModel.getTemperature(level, pos) > -5) entityData = entityData.withWetness(Math.min(0.9f, entityData.wetness() + 0.05f));
            }
            else if (entityData.wetness() > 0.0f ) entityData = entityData.withWetness(Math.max(0.0f, entityData.wetness() - EnvironmentHelpers.calcDryingRate(level, pos.above(), entityData.environmentTemperature(), entityData.environmentHumidity(), shade, entityData.windOcclusionResult().occlusionMultiplier())));

            entityData = handleTemperatureChange(entity, entityData);

            if (entityData.internalTemperature() >= dataMap.maxEntityTemperature()) entity.hurt(ThermiaDamageTypes.hyperDamageSource(level.registryAccess()), 1.0f);
            if (entityData.internalTemperature() <= dataMap.minEntityTemperature()) entity.hurt(ThermiaDamageTypes.hypoDamageSource(level.registryAccess()), 1.0f);

            entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData);

            if (!pendingBlockSearches.containsKey(entity.getUUID()))
            {
                int searchRadius = dataMap.isMob() ? ServerConfig.ISMOB_SEARCH_RANGE.getAsInt() : ServerConfig.SEARCH_RANGE.getAsInt();
                CompletableFuture<BlockSearchResult> future = BlockSearch.searchAllAsync(level, entity.position().add(0, (double) entity.getBbHeight() / 2, 0), searchRadius);
                pendingBlockSearches.put(entity.getUUID(), future);
            }
        }
    }

    public static void onTick(Entity entity)
    {
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE) && entity.isAlive())
        {
            EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

            float submersion = getEntitySubmersion(entity);
            if (entityData.wetness() < submersion ) entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData.withWetness(submersion));
        }
    }
}
