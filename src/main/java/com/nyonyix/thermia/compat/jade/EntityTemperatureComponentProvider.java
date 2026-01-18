package com.nyonyix.thermia.compat.jade;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.util.AiHelpers;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EntityTemperatureComponentProvider implements IEntityComponentProvider
{
    INSTANCE;

    private static final int WARM_COLOUR = 0xFF8080;
    private static final int HOT_COLOUR = 0xFF4545;
    private static final int COOL_COLOUR = 0x8080FF;
    private static final int COLD_COLOUR = 0x4545FF;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config)
    {
        Entity entity = accessor.getEntity();
        if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE) || !(entity instanceof LivingEntity living) || entity instanceof Player) return;
        EntityTemperature tempData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

        float[] thresholds = AiHelpers.getComfortThresholds(tempData, 0.75f);
        float internalTemperature = tempData.internalTemperature();
        float maxInternalTemperature = tempData.maxInternalTemperature();
        float minInternalTemperature = tempData.minInternalTemperature();

        if (internalTemperature > thresholds[1] && internalTemperature < maxInternalTemperature) tooltip.add(Component.translatable("thermia.jade.comfort.warm").withColor(WARM_COLOUR));
        else if (internalTemperature > thresholds[1] && internalTemperature > maxInternalTemperature) tooltip.add(Component.translatable("thermia.jade.comfort.hot").withColor(HOT_COLOUR));
        else if (internalTemperature < thresholds[0] && internalTemperature > minInternalTemperature) tooltip.add(Component.translatable("thermia.jade.comfort.cool").withColor(COOL_COLOUR));
        else if (internalTemperature < thresholds[0] && internalTemperature < minInternalTemperature) tooltip.add(Component.translatable("thermia.jade.comfort.cold").withColor(COLD_COLOUR));
        else tooltip.add(Component.translatable("thermia.jade.comfort.comfortable").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ResourceLocation getUid() {return ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "entity_temperature_tooltip");}
}
