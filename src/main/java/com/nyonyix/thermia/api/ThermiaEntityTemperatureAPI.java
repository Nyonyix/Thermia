package com.nyonyix.thermia.api;

import com.nyonyix.thermia.util.AiHelpers;
import com.nyonyix.thermia.data.BlockSearchResult;
import com.nyonyix.thermia.data.SolarShadeResult;
import com.nyonyix.thermia.data.WindOcclusionResult;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;

public class ThermiaEntityTemperatureAPI
{
    public static float getEnvironmentTemperature(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return 0f;
        return entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).environmentTemperature();
    }

    public static float getEnvironmentHumidity(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return 0f;
        return entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).environmentHumidity();
    }

    public static boolean getToRemove(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;
        return entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).toRemove();
    }

    public static float[] getMobComfortThresholds(Entity entity)
    {
        if (!(entity instanceof PathfinderMob mob)) return new float[] {0f, 0f};
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return new float[] {0f, 0f};

        return AiHelpers.getComfortThresholds(entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE), 0.75f);
    }

    public static float getInternalTemperature(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return 0f;
        return entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).internalTemperature();
    }

    public static boolean setInternalTemperature(Entity entity, float temperature)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData.withInternalTemperature(temperature));

        return true;
    }

    public static float getMaxInternalTemperature(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return 0f;
        return entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).maxInternalTemperature();
    }

    public static boolean setMaxInternalTemperature(Entity entity, float temperature)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData.withMaxInternalTemperature(temperature));

        return true;
    }

    public static float getMinInternalTemperature(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return 0f;
        return entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).minInternalTemperature();
    }

    public static boolean setMinInternalTemperature(Entity entity, float temperature)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData.withMinInternalTemperature(temperature));

        return true;
    }

    public static float getWetness(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return 0f;
        return entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).wetness();
    }

    public static boolean setWetness(Entity entity, float wetness)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        wetness = Mth.clamp(wetness, 0f, 1f);

        EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData.withWetness(wetness));

        return true;
    }

    public static BlockSearchResult getBlockSearchResult(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return BlockSearchResult.createDefault();
        return entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).blockSearchResult();
    }

    public static boolean setBlockSearchResult(Entity entity, BlockSearchResult blockSearchResult)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData.withBlockSearchResult(blockSearchResult));

        return true;
    }

    public static SolarShadeResult getSolarShadeResult(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return SolarShadeResult.createDefault();
        return entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).solarShadeResult();
    }

    public static boolean setSolarShadeResult(Entity entity, SolarShadeResult solarShadeResult)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData.withSolarShadeResult(solarShadeResult));

        return true;
    }

    public static WindOcclusionResult getWindOcclusionResult(Entity entity)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return WindOcclusionResult.createDefault();
        return entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE).windOcclusionResult();
    }

    public static boolean setBlockSearchResult(Entity entity, WindOcclusionResult windOcclusionResult)
    {
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperature entityData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        entity.setData(ThermiaAttachments.ENTITY_TEMPERATURE, entityData.withWindOcclusionResult(windOcclusionResult));

        return true;
    }
}
