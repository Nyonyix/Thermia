package com.nyonyix.thermia.data.datamap;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.slf4j.Logger;

public class VerifyDataMap
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void isValidBlockTemperature(BlockTemperatureDataMap dataMap, Block block)
    {
        if (dataMap.hasTFCHeat() && dataMap.temperature() != 0) LOGGER.error("Block: {}, Block cannot have both temperature grater than 0 and TFC heat", block.getDescriptionId());

        if (dataMap.isRadiative() && dataMap.temperature() < 0f) LOGGER.error("Block: {}, Block cannot be radiative while having negative temperature", block.getDescriptionId());

        if (dataMap.searchCap() <= 0f) LOGGER.error("Block: {}, Block cannot have 0 searchCap", block.getDescriptionId());
    }

    public static void isValidFluidTemperature(FluidTemperatureDataMap dataMap, Fluid fluid)
    {
        if (dataMap.isRadiative() && dataMap.temperature() <= 0f) LOGGER.error("Fluid: {}, Fluid cannot be radiative while having negative temperature", fluid.getFluidType().getDescriptionId());

        if (dataMap.searchCap() <= 0f) LOGGER.error("Fluid: {}, Fluid cannot have 0 searchCap", fluid.getFluidType().getDescriptionId());
    }

    public static void isValidEntityTemperature(EntityTemperatureDataMap dataMap, EntityType<?> entityType)
    {
        if (dataMap.maxEntityTemperature() < dataMap.minEntityTemperature()) LOGGER.error("Entity: {}, Entity cannot have a minimum temperature higher than maximum", entityType.getDescriptionId());

        if (dataMap.isTamed() && !dataMap.isMob()) LOGGER.error("Entity: {}, Entity cannot have isTamed is true while isMob is false", entityType.getDescriptionId());
    }

    public static void isValidItemInsulation(ItemInsulationDataMap dataMap, Item item)
    {
        if (dataMap.insulationModifier() > 1f) LOGGER.error("Item: {}, Item cannot have insulationModifier exceeding 1.0", item.getDescriptionId());

        if (dataMap.insulationModifier() < -1f) LOGGER.error("Item: {}, Item cannot have insulationModifier less than -1.0", item.getDescriptionId());
    }

    public static void isValidBlockPorosity(BlockPorosityDataMap dataMap, Block block)
    {
    }
}
