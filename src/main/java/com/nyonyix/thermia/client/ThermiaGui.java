package com.nyonyix.thermia.client;

import com.nyonyix.thermia.Thermia;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@Mod(value = Thermia.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Thermia.MODID, value = Dist.CLIENT)
public class ThermiaGui
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "textures/gui/icons.png");

    @SubscribeEvent
    public static void registerGuis(RegisterGuiLayersEvent event)
    {

    }

    private static void renderPlayerTemp(GuiGraphics graphics, DeltaTracker delta)
    {

    }

    public static void renderSolarShade (GuiGraphics graphics, DeltaTracker delta)
    {

    }
}
