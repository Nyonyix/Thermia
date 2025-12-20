package com.nyonyix.thermia.data.manager;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.data.SolarShadeResult;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.map.EntityTemperatureDataMap;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import com.nyonyix.thermia.util.BlockSearch;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.client.overworld.SkyPos;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.data.EntityDamageResistance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class EntityTemperatureManager
{
    private static final Logger LOGGER = LogUtils.getLogger();

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

    public static void init(Entity entity)
    {
        if (shouldGetSystem(entity))
        {
            EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
            EntityTemperature entityTemp = EntityTemperature.createDefault().withMaxInternalTemperature(dataMap.maxEntityTemperature()).withMinInternalTemperature(dataMap.minEntityTemperature());

            entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityTemp);
        }
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE))
        {
            if (entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).toRemove()) entity.removeData(ThermiaAttachments.ENTITY_TEMPERATURE);
        }
    }

    public static void onTick(Level level, Entity entity)
    {
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE))
        {
            EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
            EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);

            BlockPos pos = entity.blockPosition();
            RandomSource random = level.random;
            long calendarTick = Calendars.get(level).getTicks();
            float fractionOfDay = Calendars.get(level).getCalendarFractionOfDay();
            float fractionOfYear = Calendars.get(level).getCalendarFractionOfYear();
            float fractionOfMonth = Calendars.get(level).getCalendarFractionOfMonth();
            float hemisphereScale = Climate.get(level).hemisphereScale();

            SkyPos sunPos = SolarCalculator.getSunPosition(pos.getZ(), hemisphereScale, fractionOfYear, fractionOfDay);

            entityData = entityData.withEnvironmentHumidity(level.getChunkAt(entity.blockPosition()).getData(ThermiaAttachments.CHUNK_HUMIDITY).humidity());

            if (dataMap.isMob())
            {
                entityData = entityData.withSunOcclusionPos(SolarShadeResult.createDefault());
                entityData = entityData.withEnvironmentTemperature(EnvironmentHelpers.calcWetBulbGlobeTemperature(level, pos, Climate.getTemperature(level, pos), entityData.environmentHumidity(), level.canSeeSky(pos) ? 1.0f : 0.3f));
            }
            else
            {
                entityData = entityData.withSunOcclusionPos(BlockSearch.SearchForBlock.getSolarShade(level, pos.above(), sunPos.zenith(), sunPos.azimuth()));
                entityData = entityData.withEnvironmentTemperature(EnvironmentHelpers.calcWetBulbGlobeTemperature(level, pos, Climate.getTemperature(level, pos), entityData.environmentHumidity(), entityData.sunOcclusionPos().shade()));
            }

            entityData = entityData.withInternalTemperature(Mth.approach(entityData.internalTemperature(), entityData.environmentTemperature(), 0.1f));

            entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData);
        }
    }
}
