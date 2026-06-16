package com.nyonyix.thermia.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nyonyix.thermia.ClientConfig;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.manager.EntityTemperatureManager;
import com.nyonyix.thermia.effect.ThermiaEffects;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.client.IngameOverlays;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Locale;

import static net.dries007.tfc.client.IngameOverlays.setup;

@Mod(value = Thermia.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Thermia.MODID, value = Dist.CLIENT)
public class ThermiaGui
{
    public static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "textures/gui/icons.png");
    public static final int ICON_TEXTURE_SIZE_X = 48;
    public static final int ICON_TEXTURE_SIZE_Y = 32;
    public static final ResourceLocation HEAT_OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "textures/gui/heat_overlay.png");
    public static final int HEAT_OVERLAY_SIZE_X = 512;
    public static final int HEAT_OVERLAY_SIZE_Y = 512;
    public static final ResourceLocation COLD_OVERLAY_TEXTURE = ResourceLocation.parse("minecraft:textures/misc/powder_snow_outline.png");
    public static final int COLD_OVERLAY_SIZE_X = 256;
    public static final int COLD_OVERLAY_SIZE_Y = 256;
    private static final int WIDGET_SIZE = 8;
    private static final float SUB_WIDGET_SIZE = 0.75f;

    private static boolean setupForSurvival(GuiGraphics gui, Minecraft minecraft) {
        MultiPlayerGameMode gm = Minecraft.getInstance().gameMode;
        return gm != null && gm.canHurtPlayer() && setup(gui, minecraft);
    }

    private static boolean setupForNotSpectator(GuiGraphics gui, Minecraft minecraft) {
        MultiPlayerGameMode gm = Minecraft.getInstance().gameMode;
        return gm != null && gm.getPlayerMode() != GameType.SPECTATOR && setup(gui, minecraft);
    }

    private static float getConfigUIScale() {return (float) ClientConfig.UI_SCALE.getAsDouble();}

    private static int getConfigUIXOffset() {return ClientConfig.UI_X_OFFSET.getAsInt();}

    private static int getConfigUIYOffset() {return ClientConfig.UI_Y_OFFSET.getAsInt();}

    private static ResourceLocation getTFCResourceLocation(IngameOverlays overlay) {return Helpers.resourceLocation(overlay.name().toLowerCase(Locale.ROOT));}

    private static int rgbToHex(float r, float g, float b, float a)
    {
        int alpha = (int) (Mth.clamp(a, 0f, 1f) * 255);
        int red = (int) (Mth.clamp(r, 0f, 1) * 255);
        int green = (int) (Mth.clamp(g, 0f, 1) * 255);
        int blue = (int) (Mth.clamp(b, 0f, 1) * 255);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static void renderPlayerTemp(GuiGraphics graphics, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!setupForNotSpectator(graphics, mc)) return;

        LocalPlayer player = mc.player;
        if (!player.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;
        EntityTemperature playerTemp = player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        Gui gui = mc.gui;

        float uiScale = getConfigUIScale();
        int uiXOffset = getConfigUIXOffset();
        int uiYOffset = getConfigUIYOffset();
        int scaledWidgetSize = (int) (WIDGET_SIZE * uiScale);

        int centerX = (graphics.guiWidth() / 2) - scaledWidgetSize / 2;
        int y = graphics.guiHeight() - gui.leftHeight - (scaledWidgetSize / 2) + 5;

        float playerTemperature = playerTemp.internalTemperature();
        float environmentTemperature = playerTemp.environmentTemperature();
        float maxTemperature = playerTemp.maxInternalTemperature();
        float minTemperature = playerTemp.minInternalTemperature();
        float playerTemperatureNormalised = Mth.clamp((playerTemperature - minTemperature) / (maxTemperature - minTemperature), 0f, 1f);
        float environmentNormalised = Mth.clamp((environmentTemperature - minTemperature) / ((maxTemperature * 1.1f) - (minTemperature * 0.9f)), 0f, 1f);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX + uiXOffset, y + uiYOffset, 0f);

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

        RenderSystem.setShaderColor(playerR, playerG, playerB, 1.0f);
        graphics.blit(ICON_TEXTURE, -scaledWidgetSize / 2, 0, scaledWidgetSize, scaledWidgetSize,0, 16, 8, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);

        RenderSystem.setShaderColor(envR, envG, envB, 1.0f);
        graphics.blit(ICON_TEXTURE,scaledWidgetSize / 2, 0, scaledWidgetSize, scaledWidgetSize, 8, 16, 8, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static void renderSolarIntensity(GuiGraphics graphics, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!setupForNotSpectator(graphics, mc)) return;

        LocalPlayer player = mc.player;
        if (!player.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;
        EntityTemperature playerData = player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        Gui gui = mc.gui;

        float uiScale = getConfigUIScale();
        float solarWidgetSize = WIDGET_SIZE * SUB_WIDGET_SIZE;
        int uiXOffset = getConfigUIXOffset();
        int uiYOffset = getConfigUIYOffset();
        int scaledWidgetSize = (int) (solarWidgetSize * uiScale);

        int centerX = (graphics.guiWidth() / 2) - scaledWidgetSize / 2 - 6;
        int y = graphics.guiHeight() - gui.leftHeight - (scaledWidgetSize / 2) - 3;

        float solarIntensity = EnvironmentHelpers.getSolarRadiationWeather(player.clientLevel, player.blockPosition().above(), playerData.solarShadeResult().shade());

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX + uiXOffset, y + uiYOffset, 0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, solarIntensity);
        graphics.blit(ICON_TEXTURE, 0, 0, scaledWidgetSize, scaledWidgetSize, 32, 16, 16, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static void renderWetness(GuiGraphics graphics, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!setupForNotSpectator(graphics, mc)) return;

        LocalPlayer player = mc.player;
        if (!player.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;
        EntityTemperature playerData = player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        Gui gui = mc.gui;

        float uiScale = getConfigUIScale();
        float wetnessWidgetSize = WIDGET_SIZE * SUB_WIDGET_SIZE;
        int uiXOffset = getConfigUIXOffset();
        int uiYOffset = getConfigUIYOffset();
        int scaledWidgetSize = (int) (wetnessWidgetSize * uiScale);

        int centerX = (graphics.guiWidth() / 2) - scaledWidgetSize / 2 + 6;
        int y = graphics.guiHeight() - gui.leftHeight - (scaledWidgetSize / 2) - 3;

        float wetness = playerData.wetness();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX + uiXOffset, y + uiYOffset, 0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, wetness);
        graphics.blit(ICON_TEXTURE, 0, 0, scaledWidgetSize, scaledWidgetSize, 16, 16, 16, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static void renderWind(GuiGraphics graphics, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!setupForNotSpectator(graphics, mc)) return;

        LocalPlayer player = mc.player;
        if (!player.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;
        EntityTemperature playerData = player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        Gui gui = mc.gui;

        float uiScale = getConfigUIScale();
        float windWidgetSize = WIDGET_SIZE * SUB_WIDGET_SIZE;
        int uiXOffset = getConfigUIXOffset();
        int uiYOffset = getConfigUIYOffset();
        int scaledWidgetSize = (int) (windWidgetSize * uiScale);

        int centerX = (graphics.guiWidth() / 2) - scaledWidgetSize / 2;
        int y = graphics.guiHeight() - gui.leftHeight - (scaledWidgetSize / 2) - 3;

        float windDirection = EnvironmentHelpers.getWindDirection(player.clientLevel, player.blockPosition().above());
        float windSpeed = EnvironmentHelpers.getWindSpeed(player.clientLevel, player.blockPosition().above(), playerData.windOcclusionResult().occlusionMultiplier());
        float playerYaw = (player.getYRot() + 180f) % 360f;
        float playerYawRad = (float) Math.toRadians(playerYaw);
        float relativeWindDirection = windDirection - playerYawRad;
        float arrowRotation = (float) Math.toDegrees(relativeWindDirection) + 90f;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX + uiXOffset, y + uiYOffset, 0f);

        poseStack.translate(3, 3, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(arrowRotation));
        poseStack.translate(-3, -3, 0);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (windSpeed < 1)
        {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            poseStack.popPose();
            return;
        }
        else if (windSpeed < 5) graphics.blit(ICON_TEXTURE, 0, 0, scaledWidgetSize, scaledWidgetSize, 0, 0, 16, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);
        else if (windSpeed < 15) graphics.blit(ICON_TEXTURE, 0, 0, scaledWidgetSize, scaledWidgetSize, 16, 0, 16, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);
        else graphics.blit(ICON_TEXTURE, 0, 0, scaledWidgetSize, scaledWidgetSize, 32, 0, 16, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static void renderHyperthermiaEffect(GuiGraphics graphics, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!setupForSurvival(graphics, mc)) return;

        if (!ClientConfig.ENABLE_HYPER_RENDER.getAsBoolean()) return;

        LocalPlayer player = mc.player;
        if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ThermiaEffects.HYPERTHERMIA.get()))) return;

        float effectScale  = EntityTemperatureManager.getTemperatureEffectScale(player);
        float configIntensity = (float) ClientConfig.HYPER_EFFECT_INTENSITY.getAsDouble();

        float r = 1f;
        float g = 0.25f;
        float b = 0f;
        float alpha = effectScale * 0.5f;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(r * configIntensity, g * configIntensity, b * configIntensity, alpha * configIntensity);

        int colourHex = (int) (rgbToHex(r, g, b, alpha) * configIntensity);

        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), colourHex);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void renderHypothermiaEffect(GuiGraphics graphics, DeltaTracker delta)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!setupForSurvival(graphics, mc)) return;

        if (!ClientConfig.ENABLE_HYPO_RENDER.getAsBoolean()) return;

        LocalPlayer player = mc.player;
        if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ThermiaEffects.HYPOTHERMIA.get()))) return;

        float effectScale = EntityTemperatureManager.getTemperatureEffectScale(player);
        float configIntensity = (float) ClientConfig.HYPO_EFFECT_INTENSITY.getAsDouble();

        float r = 1f;
        float g = 1f;
        float b = 1f;
        float alpha = effectScale * 0.5f;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r * configIntensity, g * configIntensity, b * configIntensity, alpha * configIntensity);

        int colourHex = (int) (rgbToHex(r, g, b, alpha) * configIntensity);

        graphics.blit(COLD_OVERLAY_TEXTURE, 0, 0, 0, 0, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight());
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), colourHex);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void registerGuis(RegisterGuiLayersEvent event)
    {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "hyperthermia_effects"), ThermiaGui::renderHyperthermiaEffect);
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "hypothermia_effects"), ThermiaGui::renderHypothermiaEffect);

        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "temp"), ThermiaGui::renderPlayerTemp);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "solar_intensity"), ThermiaGui::renderSolarIntensity);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "wetness"), ThermiaGui::renderWetness);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "wind"), ThermiaGui::renderWind);
    }
}
