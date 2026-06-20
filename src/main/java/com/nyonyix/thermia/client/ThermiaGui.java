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
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

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

    private static int clampX(int screenW, int widgetW, int offset)
    {
        return Mth.clamp(offset, 0, screenW - widgetW);
    }

    private static int clampY(int screenH, int widgetH, int offset)
    {
        return Mth.clamp(offset, 0, screenH - widgetH);
    }

    private static boolean canRender(GuiGraphics graphics)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (!mc.player.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;
        MultiPlayerGameMode gm = mc.gameMode;
        if (gm == null || gm.getPlayerMode() == GameType.CREATIVE) return false;
        if (!ClientConfig.GLOBAL_UI_TOGGLE.getAsBoolean()) return false;
        return IngameOverlays.setup(graphics, mc);
    }

    private static boolean canRenderEffect(GuiGraphics graphics)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (!mc.player.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;
        MultiPlayerGameMode gm = mc.gameMode;
        if (gm == null || !gm.canHurtPlayer()) return false;
        return IngameOverlays.setup(graphics, mc);
    }

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
        if (!ClientConfig.TEMPERATURE_UI_TOGGLE.getAsBoolean()) return;
        if (!canRender(graphics)) return;

        float scale = (float) ClientConfig.TEMPERATURE_UI_SCALE.getAsDouble();
        float globalScale = (float) ClientConfig.GLOBAL_UI_SCALE.getAsDouble();
        int iconW = (int) ((WIDGET_SIZE * scale) * globalScale);
        int widgetW = iconW * 2;
        int anchorX = (graphics.guiWidth() / 2) - (iconW / 2);
        int anchorY = graphics.guiHeight() - iconW;

        int rawX = anchorX + ClientConfig.TEMPERATURE_UI_OFFSET_X.getAsInt() + ClientConfig.GLOBAL_UI_OFFSET_X.getAsInt();
        int rawY = anchorY + ClientConfig.TEMPERATURE_UI_OFFSET_Y.getAsInt() + ClientConfig.GLOBAL_UI_OFFSET_Y.getAsInt();

        int x = clampX(graphics.guiWidth(), widgetW, rawX);
        int y = clampY(graphics.guiHeight(), iconW, rawY);

        EntityTemperature playerTemp = Minecraft.getInstance().player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        float playerTemperature = playerTemp.internalTemperature();
        float environmentTemperature = playerTemp.environmentTemperature();
        float maxTemperature = playerTemp.maxInternalTemperature();
        float minTemperature = playerTemp.minInternalTemperature();
        float playerTemperatureNormalised = Mth.clamp((playerTemperature - minTemperature) / (maxTemperature - minTemperature), 0f, 1f);
        float environmentNormalised = Mth.clamp((environmentTemperature - minTemperature) / ((maxTemperature * 1.1f) - (minTemperature * 0.9f)), 0f, 1f);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0f);

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
        graphics.blit(ICON_TEXTURE, -iconW/ 2, 0, iconW, iconW,0, 16, 8, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);

        RenderSystem.setShaderColor(envR, envG, envB, 1.0f);
        graphics.blit(ICON_TEXTURE,iconW / 2, 0, iconW, iconW, 8, 16, 8, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static void renderSolarIntensity(GuiGraphics graphics, DeltaTracker delta)
    {
        if (!ClientConfig.SOLAR_UI_TOGGLE.getAsBoolean()) return;
        if (!canRender(graphics)) return;

        float scale = (float) ClientConfig.SOLAR_UI_SCALE.getAsDouble();
        float globalScale = (float) ClientConfig.GLOBAL_UI_SCALE.getAsDouble();
        int widgetSize = ClientConfig.SOLAR_UI_SUB_TOGGLE.getAsBoolean() ? (int) (((WIDGET_SIZE * scale) * globalScale) * SUB_WIDGET_SIZE): (int) ((WIDGET_SIZE * scale) * globalScale) ;;
        int anchorX = (graphics.guiWidth() / 2) - (widgetSize / 2);
        int anchorY = graphics.guiHeight() - widgetSize;

        int rawX = anchorX + ClientConfig.SOLAR_UI_OFFSET_X.getAsInt() + ClientConfig.GLOBAL_UI_OFFSET_X.getAsInt();
        int rawY = anchorY + ClientConfig.SOLAR_UI_OFFSET_Y.getAsInt() + ClientConfig.GLOBAL_UI_OFFSET_Y.getAsInt();

        int x = clampX(graphics.guiWidth(), widgetSize, rawX);
        int y = clampY(graphics.guiHeight(), widgetSize, rawY);

        Player player = Minecraft.getInstance().player;
        EntityTemperature playerData = player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

        float solarIntensity = EnvironmentHelpers.getSolarRadiationWeather(player.level(), player.blockPosition().above(), playerData.solarShadeResult().shade());

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, solarIntensity);
        graphics.blit(ICON_TEXTURE, 0, 0, widgetSize, widgetSize, 32, 16, 16, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static void renderWetness(GuiGraphics graphics, DeltaTracker delta)
    {
        if (!ClientConfig.WETNESS_UI_TOGGLE.getAsBoolean()) return;
        if (!canRender(graphics)) return;

        float scale = (float) ClientConfig.WETNESS_UI_SCALE.getAsDouble();
        float globalScale = (float) ClientConfig.GLOBAL_UI_SCALE.getAsDouble();
        int widgetSize = ClientConfig.WETNESS_UI_SUB_TOGGLE.getAsBoolean() ? (int) (((WIDGET_SIZE * scale) * globalScale) * SUB_WIDGET_SIZE): (int) ((WIDGET_SIZE * scale) * globalScale) ;;
        int anchorX = (graphics.guiWidth() / 2) - (widgetSize / 2);;
        int anchorY = graphics.guiHeight() - widgetSize;

        int rawX = anchorX + ClientConfig.WETNESS_UI_OFFSET_X.getAsInt() + ClientConfig.GLOBAL_UI_OFFSET_X.getAsInt();
        int rawY = anchorY + ClientConfig.WETNESS_UI_OFFSET_Y.getAsInt() + ClientConfig.GLOBAL_UI_OFFSET_Y.getAsInt();

        int x = clampX(graphics.guiWidth(), widgetSize, rawX);
        int y = clampY(graphics.guiHeight(), widgetSize, rawY);

        Player player = Minecraft.getInstance().player;
        EntityTemperature playerData = player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

        float wetness = playerData.wetness();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, wetness);
        graphics.blit(ICON_TEXTURE, 0, 0, widgetSize, widgetSize, 16, 16, 16, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static void renderWind(GuiGraphics graphics, DeltaTracker delta)
    {
        if (!ClientConfig.WIND_UI_TOGGLE.getAsBoolean()) return;
        if (!canRender(graphics)) return;

        float scale = (float) ClientConfig.WIND_UI_SCALE.getAsDouble();
        float globalScale = (float) ClientConfig.GLOBAL_UI_SCALE.getAsDouble();
        int widgetSize = ClientConfig.WIND_UI_SUB_TOGGLE.getAsBoolean() ? (int) (((WIDGET_SIZE * scale) * globalScale) * SUB_WIDGET_SIZE): (int) ((WIDGET_SIZE * scale) * globalScale) ;;
        int anchorX = (graphics.guiWidth() / 2) - (widgetSize / 2);;
        int anchorY = graphics.guiHeight() - widgetSize;

        int rawX = anchorX + ClientConfig.WIND_UI_OFFSET_X.getAsInt() + ClientConfig.GLOBAL_UI_OFFSET_X.getAsInt();
        int rawY = anchorY + ClientConfig.WIND_UI_OFFSET_Y.getAsInt() + ClientConfig.GLOBAL_UI_OFFSET_Y.getAsInt();

        int x = clampX(graphics.guiWidth(), widgetSize, rawX);
        int y = clampY(graphics.guiHeight(), widgetSize, rawY);

        Player player = Minecraft.getInstance().player;
        EntityTemperature playerData = player.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

        float windDirection = EnvironmentHelpers.getWindDirection(player.level(), player.blockPosition().above());
        float windSpeed = EnvironmentHelpers.getWindSpeed(player.level(), player.blockPosition().above(), playerData.windOcclusionResult().occlusionMultiplier());
        float playerYaw = (player.getYRot() + 180f) % 360f;
        float playerYawRad = (float) Math.toRadians(playerYaw);
        float relativeWindDirection = windDirection - playerYawRad;
        float arrowRotation = (float) Math.toDegrees(relativeWindDirection) + 90f;
        float pivot = widgetSize / 2f;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0f);

        poseStack.translate(pivot, pivot, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(arrowRotation));
        poseStack.translate(-pivot, -pivot, 0);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (windSpeed < 1)
        {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            poseStack.popPose();
            return;
        }
        else if (windSpeed < 5) graphics.blit(ICON_TEXTURE, 0, 0, widgetSize, widgetSize, 0, 0, 16, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);
        else if (windSpeed < 15) graphics.blit(ICON_TEXTURE, 0, 0, widgetSize, widgetSize, 16, 0, 16, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);
        else graphics.blit(ICON_TEXTURE, 0, 0, widgetSize, widgetSize, 32, 0, 16, 16, ICON_TEXTURE_SIZE_X, ICON_TEXTURE_SIZE_Y);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static void renderHyperthermiaEffect(GuiGraphics graphics, DeltaTracker delta)
    {
        if (!ClientConfig.ENABLE_HYPER_RENDER.getAsBoolean()) return;
        if (!canRenderEffect(graphics)) return;

        Player player = Minecraft.getInstance().player;
        if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ThermiaEffects.HYPERTHERMIA.get()))) return;

        float effectScale = EntityTemperatureManager.getTemperatureEffectScale(player);
        float intensity = (float) ClientConfig.HYPER_EFFECT_INTENSITY.getAsDouble();
        float alpha = effectScale * 1.5f * intensity;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 0.25f, 0f, alpha);

        graphics.blit(HEAT_OVERLAY_TEXTURE, 0, 0, 0, 0, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight());

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    private static void renderHypothermiaEffect(GuiGraphics graphics, DeltaTracker delta)
    {
        if (!ClientConfig.ENABLE_HYPO_RENDER.getAsBoolean()) return;
        if (!canRenderEffect(graphics)) return;

        Player player = Minecraft.getInstance().player;
        if (!player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ThermiaEffects.HYPOTHERMIA.get()))) return;

        float effectScale = EntityTemperatureManager.getTemperatureEffectScale(player);
        float intensity = (float) ClientConfig.HYPO_EFFECT_INTENSITY.getAsDouble();
        float alpha = effectScale * 1.5f * intensity;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        graphics.blit(COLD_OVERLAY_TEXTURE, 0, 0, 0, 0, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight());

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

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
