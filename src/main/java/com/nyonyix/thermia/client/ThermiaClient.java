package com.nyonyix.thermia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.KoppenClimateHumidity;
import com.nyonyix.thermia.data.attachment.ChunkHumidity;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateModel;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.codehaus.plexus.util.dag.Vertex;
import org.joml.Matrix4f;

import java.util.Calendar;
import java.util.List;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Thermia.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Thermia.MODID, value = Dist.CLIENT)
public class ThermiaClient {
    public ThermiaClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        Thermia.LOGGER.info("HELLO FROM CLIENT SETUP");
        Thermia.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
    }

    @SubscribeEvent
    public static void onRenderGameOverlayText(CustomizeGuiOverlayEvent.DebugText event)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Player clientPlayer = minecraft.player;

        if (clientPlayer != null)
        {
            BlockPos pos = BlockPos.containing(clientPlayer.position());
            if (minecraft.level.hasChunk(pos.getX() / 16, pos.getZ() / 16))
            {
                RandomSource random = minecraft.level.getRandom();
                EntityTemperature playerData = clientPlayer.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

                List<String> text = event.getLeft();
                text.add("");
                String colourDarkGreen = String.valueOf(ChatFormatting.DARK_GREEN);

                text.add(colourDarkGreen + "Thermia");
                text.add("Entity:");
                text.add(String.format("    Environment Temperature: %.2f", playerData.environmentTemperature()));
                text.add(String.format("    Environment Humidity: %.2f", playerData.environmentHumidity()));
                text.add(String.format("    Player internal Temp: %.2f", playerData.internalTemperature()));

                text.add("Chunk:");
                LevelChunk chunk = clientPlayer.level().getChunkAt(pos);
                Level level = clientPlayer.level();
                ClimateModel model = Climate.get(level);
                text.add(String.format("    Climate: %s", KoppenClimateHumidity.KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.get(KoppenClimateClassification.classify(model.getAverageTemperature(level, pos), model.getRainfall(level, pos), model.getRainfallVariance(level, pos), SolarCalculator.getInNorthernHemisphere(pos, level))).climateToString()));
                text.add(String.format("    Solar Intensity: %.2f", EnvironmentHelpers.getSolarRadiationWeather(level, pos, playerData.sunOcclusionPos().shade())));
            }
        }
    }

    public static void onRenderLevel(RenderLevelStageEvent event)
    {
        if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        Vec3 cameraPos = event.getCamera().getPosition();

        for (Entity entity : minecraft.level.entitiesForRendering())
        {
            if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return;

            EntityTemperature tempData = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
            BlockPos occlusionPos = tempData.sunOcclusionPos().sunOcclusionPos();

            if (occlusionPos.equals(BlockPos.ZERO)) continue;

            renderSunOcclusionLine(poseStack, bufferSource, cameraPos, entity.position(), Vec3.atCenterOf(occlusionPos));
        }
    }

    private static void renderSunOcclusionLine(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, Vec3 entityPos, Vec3 occlusionPos)
    {
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        float startX = (float) entityPos.x;
        float startY = (float) entityPos.y + 0.5f;
        float startZ = (float) entityPos.z;

        float endX = (float) occlusionPos.x;
        float endY = (float) occlusionPos.y;
        float endZ = (float) occlusionPos.z;

        float r = 1.0f;
        float g = 1.0f;
        float b = 0.0f;
        float a = 0.8f;

        buffer.addVertex(matrix, startX, startY, startZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        buffer.addVertex(matrix, endX, endY, endZ).setColor(r, g, b, a).setNormal(0, 1, 0);

        poseStack.popPose();
    }
}
