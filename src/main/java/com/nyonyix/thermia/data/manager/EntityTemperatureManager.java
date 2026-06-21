package com.nyonyix.thermia.data.manager;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.BlockSearchResult;
import com.nyonyix.thermia.data.Interior;
import com.nyonyix.thermia.data.SolarShadeResult;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.datamap.EntityTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import com.nyonyix.thermia.effect.ThermiaEffects;
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
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
        float multi  = (float) ServerConfig.ENTITY_TEMPERATURE_CHANGE_MULTI.getAsDouble();
        float environmentDelta = entityTemperature.environmentTemperature() - entityTemperature.internalTemperature();
        float entityMedian = (entityTemperature.maxInternalTemperature() + entityTemperature.minInternalTemperature()) / 2f;
        float medianDelta = (entityMedian - entityTemperature.internalTemperature()) * multi;

        float insulation = 1f - (Mth.clamp(ItemInventoryManager.getInventoryInsulation(entity), -1.0f, 1.0f) * (1f - entityTemperature.wetness()));

        float effectiveDelta = medianDelta + (environmentDelta * insulation);

        float absDelta = Math.abs(effectiveDelta);

        float normalisedRate = 1.0f - (float) Math.exp(-0.005 * absDelta);
        float baseRate = Mth.lerp(normalisedRate, 0.01f, 0.5f);

        return  entityTemperature.withInternalTemperature(entityTemperature.internalTemperature() + (effectiveDelta * baseRate));
    }

    private static float getEntitySubmersion(Entity entity, Level level)
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

    private static EntityTemperature handleEntitySweat(Entity entity, EntityTemperature entityData)
    {
        float currentTemperature = entityData.internalTemperature();
        float minTemperature = entityData.minInternalTemperature();
        float maxTemperature = entityData.maxInternalTemperature();
        float delta = maxTemperature - minTemperature;
        float bufferPercentFromConfig = (float) ServerConfig.TEMPERATURE_SEGMENTS_PERCENT.getAsInt() / 100;
        float heatBufferZone = delta * bufferPercentFromConfig;
        float heatComfortThreshold = maxTemperature - heatBufferZone;
        float heatStress = currentTemperature - heatComfortThreshold;

        if (entity instanceof IPlayerInfo player)
        {
            float currentHydration = player.getThirst();
            float sweatEfficiency = Mth.clampedMap(currentHydration, 0f, 60f, 0.1f, 1f);

            if (heatStress > 0)
            {
                float normalisedHeatStress = Mth.clamp(heatStress / heatBufferZone, 0f, 1f);
                float baseSweatRate = normalisedHeatStress * 0.05f;
                float actualSweatRate = baseSweatRate * sweatEfficiency;
                float newWetness = Math.min(1.0f, entityData.wetness() + actualSweatRate);

                float hydrationLossMulti = (float) ServerConfig.PLAYER_SWEAT_HYDRATION_LOSS_MULTI.getAsDouble();

                entityData = entityData.withWetness(newWetness);
                player.addThirst(-((baseSweatRate * 10) * hydrationLossMulti));
            }
        }
        else
        {
            if (heatStress > 0)
            {
                float normalisedHeatStress = Mth.clamp(heatStress / heatBufferZone, 0f, 1f);
                float baseSweatRate = normalisedHeatStress * 0.05f;
                float newWetness = Math.min(1.0f, entityData.wetness() + baseSweatRate);

                entityData = entityData.withWetness(newWetness);
            }
        }

        return entityData;
    }

    private static EntityTemperature handleWetness(Level level, BlockPos pos, ClimateModel levelModel, ICalendar levelCalender, EntityTemperature entityData, float shade)
    {
        boolean isRaining = WeatherHelpers.isPrecipitating(levelModel.getRain(levelCalender.getCalendarTicks()), levelModel.getInstantRainfall(level, pos));
        if (isRaining && level.canSeeSky(pos) && entityData.wetness() <= 0.9)
        {
            if (levelModel.getInstantTemperature(level, pos) > 0.0f) entityData = entityData.withWetness(Math.min(0.9f, entityData.wetness() + 0.1f));
            else if (levelModel.getInstantTemperature(level, pos) > -5) entityData = entityData.withWetness(Math.min(0.9f, entityData.wetness() + 0.05f));
        }
        else if (entityData.wetness() > 0.0f ) entityData = entityData.withWetness(Math.max(0.0f, entityData.wetness() - EnvironmentHelpers.calcDryingRate(level, pos.above(), entityData.environmentTemperature(), entityData.environmentHumidity(), shade, entityData.windOcclusionResult().occlusionMultiplier())));

        return entityData;
    }

    private static void handleEntityTemperatureEffect(Entity entity)
    {
        if (!(entity instanceof LivingEntity living)) return;
        if (living instanceof Player player)
        {
            if (player.isCreative() || player.isSpectator())
            {
                if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ThermiaEffects.HYPERTHERMIA.get())))
                {
                    player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ThermiaEffects.HYPERTHERMIA.get()));
                }
                else if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ThermiaEffects.HYPOTHERMIA.get())))
                {
                    player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ThermiaEffects.HYPOTHERMIA.get()));
                }
                return;
            }
        }
        EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

        float entityTemperatureMidpoint = (entityData.maxInternalTemperature() + entityData.minInternalTemperature()) / 2f;
        float effectScale = getTemperatureEffectScale(entity);
        int amplifier = getEffectAmplifierFromScale(effectScale);

        Holder<MobEffect> hyperthermia = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ThermiaEffects.HYPERTHERMIA.get());
        Holder<MobEffect> hypothermia = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ThermiaEffects.HYPOTHERMIA.get());
        Holder<MobEffect> confusion = MobEffects.CONFUSION;
        Holder<MobEffect> slowness = MobEffects.MOVEMENT_SLOWDOWN;
        Holder<MobEffect> fatigue = MobEffects.DIG_SLOWDOWN;

        if (entityData.internalTemperature() > entityData.minInternalTemperature() && entityData.internalTemperature() < entityData.maxInternalTemperature())
        {
            living.removeEffect(hypothermia);
            living.removeEffect(slowness);
            living.removeEffect(fatigue);
            living.removeEffect(hyperthermia);
            living.removeEffect(confusion);
            return;
        }

        if (entityData.internalTemperature() > entityTemperatureMidpoint)
        {
            MobEffectInstance currentEffect = living.getEffect(hyperthermia);
            MobEffectInstance hyper = new MobEffectInstance(hyperthermia, -1, amplifier, false, false, true);
            MobEffectInstance conf = new MobEffectInstance(confusion, -1, 0, false, false, false);

            if (currentEffect == null)
            {
                living.addEffect(hyper);
                living.addEffect(conf);
            }
            else if (currentEffect.getAmplifier() < amplifier)
            {
                living.addEffect(hyper);
            }
            else if (currentEffect.getAmplifier() > amplifier)
            {
                living.removeEffect(hyperthermia);
                living.addEffect(hyper);
            }
        }
        else if (entityData.internalTemperature() < entityTemperatureMidpoint)
        {
            MobEffectInstance currentEffect = living.getEffect(hypothermia);
            MobEffectInstance hypo = new MobEffectInstance(hypothermia, -1, amplifier, false, false, true);
            MobEffectInstance slow = new MobEffectInstance(slowness, -1, amplifier, false, false, false);
            MobEffectInstance digSlow = new MobEffectInstance(fatigue, -1, 0, false, false, false);

            if (currentEffect == null)
            {
                living.addEffect(hypo);
                living.addEffect(slow);
                living.addEffect(digSlow);
            }
            else if (currentEffect.getAmplifier() < amplifier)
            {
                living.addEffect(hypo);
            }
            else if (currentEffect.getAmplifier() > amplifier)
            {
                living.removeEffect(hypothermia);
                living.addEffect(hypo);
            }
        }
    }

    private static EntityTemperature handleEnvironmentTemperature(Entity entity, EntityTemperatureDataMap dataMap, EntityTemperature entityData)
    {
        Level level = entity.level();
        ICalendar levelCalender = Calendars.get(level);
        ClimateModel levelModel = Climate.get(level);
        BlockPos pos = entity.blockPosition();
        RandomSource random = level.random;
        long calendarTick = levelCalender.getTicks();
        float fractionOfDay = levelCalender.getCalendarFractionOfDay();
        float fractionOfYear = levelCalender.getCalendarFractionOfYear();
        float fractionOfMonth = levelCalender.getCalendarFractionOfMonth();
        float hemisphereScale = levelModel.hemisphereScale();
        float baseTemperature = 0f;
        float shade = 0.3f;
        float nearbyBlockTemperature = 0f;

        nearbyBlockTemperature = entityData.blockSearchResult().getRadiance(level, entity);
        nearbyBlockTemperature += entityData.blockSearchResult().getImmersion(level, entity);
        nearbyBlockTemperature += entityData.blockSearchResult().getContact(level, entity, dataMap.isMob());

        if (InteriorManager.isInInterior(level, pos.relative(Direction.UP)))
        {
            Interior interior = InteriorManager.getInteriorByPos(level, pos.relative(Direction.UP));

            baseTemperature = interior.internalTemperature();
        }
        else
        {
            baseTemperature = levelModel.getInstantTemperature(level, pos);
        }

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

        return entityData;
    }

    public static void handleEntityAcclimatization(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;
        EntityTemperature tempData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

        EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
        if (dataMap == null) return;

        float baseMax = dataMap.maxEntityTemperature();
        float baseMin = dataMap.minEntityTemperature();

        float currentMax = tempData.maxInternalTemperature();
        float currentMin = tempData.minInternalTemperature();
        float midPointTemp = (currentMax + currentMin) / 2f;

        float environmentalAverage = Climate.get(entity.level()).getAverageTemperature(entity.level(), entity.blockPosition());

        float multi = (float) ServerConfig.ENTITY_ACCLIMATISATION_MULTI.getAsDouble();
        float delta = (environmentalAverage - midPointTemp) * multi / 1E4f;

        float acclimatisation = tempData.acclimatization() + delta;

        float buffer = ServerConfig.TEMPERATURE_SEGMENTS_PERCENT.getAsInt() / 100f;
        float maxUpper = baseMax * (1f + buffer);
        float maxLower = baseMax * (1f - buffer);
        float minUpper = baseMin * (1f + buffer);
        float minLower = baseMin * (1f - buffer);

        float acclimatisedMax = Mth.clamp(currentMax + delta, maxLower, maxUpper);
        float acclimatisedMin = Mth.clamp(currentMin + delta, minLower, minUpper);

        tempData = tempData.withAcclimatization(acclimatisation);
        tempData = tempData.withMaxInternalTemperature(acclimatisedMax);
        tempData = tempData.withMinInternalTemperature(acclimatisedMin);

        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, tempData);
    }

    public static float getTemperatureEffectScale(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return 0f;
        EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

        float bufferFromConfig = (float) ServerConfig.TEMPERATURE_SEGMENTS_PERCENT.getAsInt() / 100;
        int maxEffectLevels = ServerConfig.MAX_TEMPERATURE_EFFECT_LEVEL.getAsInt();

        float minTemperature = entityData.minInternalTemperature();
        float maxTemperature = entityData.maxInternalTemperature();
        float currentTemperature = entityData.internalTemperature();

        float minMaxDelta = maxTemperature - minTemperature;
        float bufferAmount = minMaxDelta * bufferFromConfig;

        if (currentTemperature >= minTemperature && currentTemperature <= maxTemperature) return 0f;

        if (currentTemperature < minTemperature)
        {
            float coldDelta = minTemperature - currentTemperature;
            float maxColdRange = bufferAmount * maxEffectLevels;

            return Mth.clamp(coldDelta / maxColdRange, 0f, 1f);
        }

        if (currentTemperature > maxTemperature)
        {
            float hotDelta = currentTemperature - maxTemperature;
            float maxHotRange = bufferAmount * maxEffectLevels;

            return Mth.clamp(hotDelta / maxHotRange, 0f, 1f);
        }

        return 0f;
    }

    public static int getEffectAmplifierFromScale(float scale)
    {
        int maxEffectLevels = ServerConfig.MAX_TEMPERATURE_EFFECT_LEVEL.getAsInt();
        return Math.min((int) (scale * maxEffectLevels), maxEffectLevels - 1);
    }

    public static void queueBlockSearch(Entity entity)
    {
        if (!(entity instanceof LivingEntity)) return;

        EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
        if (dataMap == null) return;

        if (!pendingBlockSearches.containsKey(entity.getUUID()))
        {
            int searchRadius = dataMap.isMob() ? ServerConfig.ISMOB_SEARCH_RANGE.getAsInt() : ServerConfig.SEARCH_RANGE.getAsInt();
            CompletableFuture<BlockSearchResult> future = BlockSearch.searchAllAsync(entity.level(), entity.position().add(0, (double) entity.getBbHeight() / 2, 0), searchRadius);
            pendingBlockSearches.put(entity.getUUID(), future);
        }
    }

    public static void init(Entity entity)
    {
        EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
        if (dataMap == null) return;
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;

        float defaultTemp = (dataMap.maxEntityTemperature() + dataMap.minEntityTemperature()) / 2;

        if (dataMap.isTamed())
        {
            if (entity instanceof TFCAnimalProperties tfcAnimal)
            {
                if (tfcAnimal.getFamiliarity() >= 0.1)
                {
                    entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, EntityTemperature.createDefault().withMinInternalTemperature(dataMap.minEntityTemperature()).withMaxInternalTemperature(dataMap.maxEntityTemperature()).withInternalTemperature(defaultTemp));
                }
            } else  LOGGER.error("Entity {} is marked 'tamable' but no tamable entity found", entity.getName());
            return;
        }

        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, EntityTemperature.createDefault().withMinInternalTemperature(dataMap.minEntityTemperature()).withMaxInternalTemperature(dataMap.maxEntityTemperature()).withInternalTemperature(defaultTemp));
    }

    public static void onUpdate(Level level, Entity entity)
    {
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE) && entity.isAlive() && entity instanceof LivingEntity living)
        {
            EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
            EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
            CompletableFuture<BlockSearchResult> pendingBlockSearch = pendingBlockSearches.get(entity.getUUID());
            ICalendar levelCalender = Calendars.get(level);
            ClimateModel levelModel = Climate.get(level);
            BlockPos pos = entity.blockPosition();
            int nonEmptyAbove = 0;

            if (entity instanceof TFCAnimalProperties tfcAnimal && tfcAnimal.getFamiliarity() < 0.1) entityData = entityData.withToRemove(true);
            if (entityData.toRemove())
            {
                CompletableFuture<BlockSearchResult> pending = pendingBlockSearches.remove(entity.getUUID());
                if(pending != null && !pending.isDone()) pending.cancel(true);
                entity.removeData(ThermiaAttachments.ENTITY_TEMPERATURE);
                return;
            }

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
            entityData = entityData.withEnvironmentHumidity(EnvironmentHelpers.getEntityHumidity(level.getChunkAt(pos).getData(ThermiaAttachments.CHUNK_HUMIDITY).humidity(), nonEmptyAbove));
            entityData = handleEnvironmentTemperature(entity, dataMap, entityData);
            entityData = handleEntitySweat(entity, entityData);
            entityData = handleWetness(level, pos, levelModel, levelCalender, entityData, entityData.solarShadeResult().shade());
            entityData = handleTemperatureChange(entity, entityData);

            entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData);
        }
    }

    public static void onTick(Entity entity)
    {
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE) && entity.isAlive())
        {
            EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

            float submersion = getEntitySubmersion(entity, entity.level());
            if (entityData.wetness() < submersion ) entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData.withWetness(submersion));

            handleEntityTemperatureEffect(entity);
        }
    }
}
