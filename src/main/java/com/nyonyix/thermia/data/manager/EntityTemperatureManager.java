package com.nyonyix.thermia.data.manager;

import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.map.EntityTemperatureDataMap;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;

public class EntityTemperatureManager
{
    private static boolean shouldGetSystem(Entity entity)
    {
        if (entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperatureDataMap dataMap = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).getData(ThermiaDataMaps.ENTITY_TEMPERATURE_DATA_MAP);
        if (dataMap == null) return false;

        if (!dataMap.isMob()) return true;
        if (!dataMap.isTamed()) return true;

        if (entity instanceof TFCAnimalProperties tfcAnimalProperties) return tfcAnimalProperties.getFamiliarity() >= 0.18f;
        if (entity instanceof OwnableEntity ownableEntity) return ownableEntity.getOwnerUUID() != null;

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
    }

    public static void onTick(Level level, Entity entity)
    {
        if (entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE) != null)
        {
            EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

            BlockPos pos = entity.blockPosition();
            RandomSource random = level.random;

            entityData = entityData.withEnvironmentHumidity(EnvironmentHelpers.getClimateSpecificHumidity(level, pos, random, entityData.environmentHumidity()));
            entityData = entityData.withEnvironmentTemperature(EnvironmentHelpers.calcWetBulbGlobeTemperature(level, pos, Climate.getTemperature(level, pos), entityData.environmentHumidity()));

            entityData = entityData.withInternalTemperature(Mth.approach(entityData.internalTemperature(), entityData.environmentTemperature(), 0.1f));

            entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData);
        }
    }
}
