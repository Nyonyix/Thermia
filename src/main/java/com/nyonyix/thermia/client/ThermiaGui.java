package com.nyonyix.thermia.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.client.IngameOverlays;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Locale;

import static net.dries007.tfc.client.IngameOverlays.setup;

@Mod(value = Thermia.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Thermia.MODID, value = Dist.CLIENT)
public class ThermiaGui
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "textures/gui/icons.png");
    public static final int TEXTURE_SIZE_X = 112;
    public static final int TEXTURE_SIZE_Y = 32;
    private static final float SCALE = 0.75f;
    private static final int WIDGET_SIZE = (int) (32 * SCALE);
    private static final int PLAYER_HEAD_SIZE = (int) (16 * SCALE);

    private static boolean setupForSurvival(GuiGraphics gui, Minecraft minecraft) {
        MultiPlayerGameMode gm = Minecraft.getInstance().gameMode;
        return gm != null && gm.canHurtPlayer() && setup(gui, minecraft);
    }

    private static ResourceLocation getTFCResourceLocation(IngameOverlays overlay) {return Helpers.resourceLocation(overlay.name().toLowerCase(Locale.ROOT));}

    @SubscribeEvent
    public static void registerGuis(RegisterGuiLayersEvent event)
    {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "temp"), ThermiaGui::renderPlayerTemp);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "solar_intensity"), ThermiaGui::renderSolarIntensity);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "wetness"), ThermiaGui::renderWetness);
    }

    private static void renderPlayerTemp(GuiGraphics graphics, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!setupForSurvival(graphics, mc)) return;

        LocalPlayer player = mc.player;
        if (!player.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;
        EntityTemperature playerTemp = player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        Gui gui = mc.gui;

        int centerX = (graphics.guiWidth() / 2) - WIDGET_SIZE / 2;
        int y = graphics.guiHeight() - Math.max(gui.rightHeight, gui.leftHeight) - (WIDGET_SIZE / 2);

        float playerTemperature = playerTemp.internalTemperature();
        float environmentTemperature = playerTemp.environmentTemperature();
        float maxTemperature = playerTemp.maxInternalTemperature();
        float minTemperature = playerTemp.minInternalTemperature();
        float playerTemperatureNormalised = Mth.clamp((playerTemperature - minTemperature) / (maxTemperature - minTemperature), 0f, 1f);
        float environmentNormalised = Mth.clamp((environmentTemperature - minTemperature) / (maxTemperature - minTemperature), 0f, 1f);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, y, 0f);

        float playerR, playerG, playerB;
        if (playerTemperatureNormalised > 0.5)
        {
            float t = (playerTemperatureNormalised - 0.5f) * 2f;
            playerR = 1.0f;
            playerG = Mth.lerp(t, 1f, 0.2f);
            playerB = Mth.lerp(t, 1f, 0.2f);
        }
        else
        {
            float t = playerTemperatureNormalised * 2;
            playerR = Mth.lerp(t, 0.2f, 1f);
            playerG = Mth.lerp(t, 0.2f, 1f);
            playerB = 1.0f;
        }

        float envR, envG, envB;
        if (environmentNormalised > 0.5f)
        {
            float t = (environmentNormalised - 0.5f) * 2f;
            envR = 1.0f;
            envG = Mth.lerp(t, 1f, 0.2f);
            envB = Mth.lerp(t, 1f, 0.2f);
        }
        else
        {
            float t = environmentNormalised * 2;
            envR = Mth.lerp(t, 0.2f, 1f);
            envG = Mth.lerp(t, 0.2f, 1f);
            envB = 1.0f;
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        ResourceLocation skinLocation = mc.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
        PlayerFaceRenderer.draw(graphics, skinLocation, PLAYER_HEAD_SIZE / 2, PLAYER_HEAD_SIZE / 2, PLAYER_HEAD_SIZE);

        RenderSystem.setShaderColor(playerR, playerG, playerB, 1.0f);
        graphics.blit(TEXTURE, 0, 0, WIDGET_SIZE, WIDGET_SIZE,0, 0, 32, 32, TEXTURE_SIZE_X, TEXTURE_SIZE_Y);

        RenderSystem.setShaderColor(envR, envG, envB, 1.0f);
        graphics.blit(TEXTURE,0, 0, WIDGET_SIZE, WIDGET_SIZE, 32, 0, 32, 32, TEXTURE_SIZE_X, TEXTURE_SIZE_Y);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static void renderSolarIntensity(GuiGraphics graphics, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!setupForSurvival(graphics, mc)) return;

        LocalPlayer player = mc.player;
        if (!player.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;
        EntityTemperature playerData = player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        Gui gui = mc.gui;

        int centerX = (graphics.guiWidth() / 2) + (WIDGET_SIZE / 2) + (16 / 2) - 4;
        int y = graphics.guiHeight() - Math.max(gui.rightHeight, gui.leftHeight);

        float solarIntensity = EnvironmentHelpers.getSolarRadiationWeather(player.clientLevel, player.blockPosition().above(), playerData.solarShadeResult().shade());

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, y, 0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, solarIntensity);
        graphics.blit(TEXTURE, 0, 0, 8, 8, 80, 16, 16, 16, TEXTURE_SIZE_X, TEXTURE_SIZE_Y);
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static void renderWetness(GuiGraphics graphics, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!setupForSurvival(graphics, mc)) return;

        LocalPlayer player = mc.player;
        if (!player.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;
        EntityTemperature playerData = player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        Gui gui = mc.gui;

        int centerX = (graphics.guiWidth() / 2) - (WIDGET_SIZE / 2) - (16 / 2) - 4;
        int y = graphics.guiHeight() - Math.max(gui.rightHeight, gui.leftHeight);

        float wetness = playerData.wetness();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, y, 0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, wetness);
        graphics.blit(TEXTURE, 0, 0, 8, 8, 96, 0, 16, 16, TEXTURE_SIZE_X, TEXTURE_SIZE_Y);
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}
