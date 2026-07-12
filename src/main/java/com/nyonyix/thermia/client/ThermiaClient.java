package com.nyonyix.thermia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ClientConfig;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.client.renderer.*;
import com.nyonyix.thermia.client.renderer.cloth.ThermiaClothBootsRenderer;
import com.nyonyix.thermia.client.renderer.cloth.ThermiaClothHeadRenderer;
import com.nyonyix.thermia.client.renderer.cloth.ThermiaClothLegsRenderer;
import com.nyonyix.thermia.client.renderer.cloth.ThermiaClothTorsoRenderer;
import com.nyonyix.thermia.client.renderer.thick.ThermiaThickBootsRenderer;
import com.nyonyix.thermia.client.renderer.thick.ThermiaThickHeadRenderer;
import com.nyonyix.thermia.client.renderer.thick.ThermiaThickLegsRenderer;
import com.nyonyix.thermia.client.renderer.thick.ThermiaThickTorsoRenderer;
import com.nyonyix.thermia.data.manager.ItemInventoryManager;
import com.nyonyix.thermia.data.records.KoppenClimateHumidity;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.datamap.ItemInsulationDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import com.nyonyix.thermia.item.cape.ThermiaCapeAnimal;
import com.nyonyix.thermia.item.ThermiaItems;
import com.nyonyix.thermia.item.cloth.ThermiaClothWearableMaterial;
import com.nyonyix.thermia.item.thick.ThermiaThickMaterial;
import com.nyonyix.thermia.item.wideBrimHat.ThermiaWideBrimHatMaterial;
import com.nyonyix.thermia.models.*;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.client.ClimateRenderCache;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import java.util.List;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Thermia.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Thermia.MODID, value = Dist.CLIENT)
public class ThermiaClient {

    private static final Logger LOGGER = LogUtils.getLogger();

    public ThermiaClient(ModContainer container, IEventBus modEventBus) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(ThermiaGui::registerGuis);
    }

    @SubscribeEvent
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(ThermiaHatModel.LAYER_LOCATION, ThermiaHatModel::createBodyLayer);
        event.registerLayerDefinition(ThermiaThickHeadModel.LAYER_LOCATION, ThermiaThickHeadModel::createBodyLayer);
        event.registerLayerDefinition(ThermiaThickTorsoModel.LAYER_LOCATION, ThermiaThickTorsoModel::createBodyLayer);
        event.registerLayerDefinition(ThermiaThickLegsModel.LAYER_LOCATION, ThermiaThickLegsModel::createBodyLayer);
        event.registerLayerDefinition(ThermiaThickBootsModel.LAYER_LOCATION, ThermiaThickBootsModel::createBodyLayer);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event)
    {
        for (var entry : ThermiaItems.CAPES.entrySet())
        {
            ThermiaCapeAnimal animal = entry.getKey();
            CuriosRendererRegistry.register(entry.getValue().get(), () -> new ThermiaCapeRenderer(animal));
        }

        for (var entry : ThermiaItems.WIDE_BRIM_HATS.entrySet())
        {
            ThermiaWideBrimHatMaterial material = entry.getKey();
            CuriosRendererRegistry.register(entry.getValue().get(), () -> new ThermiaWideBrimHatRenderer(material));
        }

        for (var entry : ThermiaItems.THICK_HEAD.entrySet())
        {
            ThermiaThickMaterial material = entry.getKey();
            CuriosRendererRegistry.register(entry.getValue().get(), () -> new ThermiaThickHeadRenderer(material));
        }

        for (var entry : ThermiaItems.THICK_TORSO.entrySet())
        {
            ThermiaThickMaterial material = entry.getKey();
            CuriosRendererRegistry.register(entry.getValue().get(), () -> new ThermiaThickTorsoRenderer(material));
        }

        for (var entry : ThermiaItems.THICK_LEGS.entrySet())
        {
            ThermiaThickMaterial material = entry.getKey();
            CuriosRendererRegistry.register(entry.getValue().get(), () -> new ThermiaThickLegsRenderer(material));
        }

        for (var entry : ThermiaItems.THICK_BOOTS.entrySet())
        {
            ThermiaThickMaterial material = entry.getKey();
            CuriosRendererRegistry.register(entry.getValue().get(), () -> new ThermiaThickBootsRenderer(material));
        }

        for (var entry : ThermiaItems.CLOTH_HEAD.entrySet())
        {
            ThermiaClothWearableMaterial material = entry.getKey();
            CuriosRendererRegistry.register(entry.getValue().get(), () -> new ThermiaClothHeadRenderer(material));
        }

        for (var entry : ThermiaItems.CLOTH_TORSO.entrySet())
        {
            ThermiaClothWearableMaterial material = entry.getKey();
            CuriosRendererRegistry.register(entry.getValue().get(), () -> new ThermiaClothTorsoRenderer(material));
        }

        for (var entry : ThermiaItems.CLOTH_LEGS.entrySet())
        {
            ThermiaClothWearableMaterial material = entry.getKey();
            CuriosRendererRegistry.register(entry.getValue().get(), () -> new ThermiaClothLegsRenderer(material));
        }

        for (var entry : ThermiaItems.CLOTH_BOOTS.entrySet())
        {
            ThermiaClothWearableMaterial material = entry.getKey();
            CuriosRendererRegistry.register(entry.getValue().get(), () -> new ThermiaClothBootsRenderer(material));
        }
    }

    @SubscribeEvent
    public static void onColorHandler(RegisterColorHandlersEvent.Item event)
    {
        for (var entry : ThermiaItems.CLOTH_HEAD.entrySet())
        {
            ThermiaClothWearableMaterial material = entry.getKey();

            if (material.isDyeable())
            {
                event.register((itemStack, i) -> {
                    DyedItemColor c = itemStack.get(DataComponents.DYED_COLOR);
                    return c != null ? FastColor.ARGB32.opaque(c.rgb()) : FastColor.ARGB32.opaque(material.getDefaultColour());
                }, entry.getValue().get());
            }
        }

        for (var entry : ThermiaItems.CLOTH_TORSO.entrySet())
        {
            ThermiaClothWearableMaterial material = entry.getKey();

            if (material.isDyeable())
            {
                event.register((itemStack, i) -> {
                    DyedItemColor c = itemStack.get(DataComponents.DYED_COLOR);
                    return c != null ? FastColor.ARGB32.opaque(c.rgb()) : FastColor.ARGB32.opaque(material.getDefaultColour());
                }, entry.getValue().get());
            }
        }

        for (var entry : ThermiaItems.CLOTH_LEGS.entrySet())
        {
            ThermiaClothWearableMaterial material = entry.getKey();

            if (material.isDyeable())
            {
                event.register((itemStack, i) -> {
                    DyedItemColor c = itemStack.get(DataComponents.DYED_COLOR);
                    return c != null ? FastColor.ARGB32.opaque(c.rgb()) : FastColor.ARGB32.opaque(material.getDefaultColour());
                }, entry.getValue().get());
            }
        }

        for (var entry : ThermiaItems.CLOTH_BOOTS.entrySet())
        {
            ThermiaClothWearableMaterial material = entry.getKey();

            if (material.isDyeable())
            {
                event.register((itemStack, i) -> {
                    DyedItemColor c = itemStack.get(DataComponents.DYED_COLOR);
                    return c != null ? FastColor.ARGB32.opaque(c.rgb()) : FastColor.ARGB32.opaque(material.getDefaultColour());
                }, entry.getValue().get());
            }
        }
    }


    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event)
    {
        Item item = event.getItemStack().getItem();
        ItemInsulationDataMap insulationData = BuiltInRegistries.ITEM.wrapAsHolder(item).getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);

        if (insulationData != null)
        {
            if (Screen.hasShiftDown())
            {
                float conductionProtection = insulationData.conductionProtection();
                float radiationProtection = insulationData.radiationProtection();
                float convectionProtection = insulationData.convectionProtection();

                Component conductionText = Component.translatable("tooltip.thermia.conduction", String.format("%.2f", conductionProtection)).withColor(0xff622e);
                Component radiationText = Component.translatable("tooltip.thermia.radiation", String.format("%.2f", radiationProtection)).withColor(0xf1ff70);
                Component convectionText = Component.translatable("tooltip.thermia.convection", String.format("%.2f", convectionProtection)).withColor(0x6188ff);

                event.getToolTip().add(Component.empty());
                event.getToolTip().add(conductionText);
                event.getToolTip().add(radiationText);
                event.getToolTip().add(convectionText);
            }
            else
            {
                Component shiftTooltip = Component.translatable("tooltip.thermia.holdShift");
                event.getToolTip().add(Component.empty());
                event.getToolTip().add(shiftTooltip);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlayText(CustomizeGuiOverlayEvent.DebugText event)
    {
        if (!ClientConfig.ENABLE_DEBUG.getAsBoolean()) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player clientPlayer = minecraft.player;

        if (clientPlayer != null)
        {
            BlockPos pos = BlockPos.containing(clientPlayer.position());
            if (minecraft.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4))
            {
                EntityTemperature playerData = clientPlayer.getData(ThermiaAttachments.ENTITY_TEMPERATURE);

                List<String> text = event.getLeft();
                text.add("");
                String colourDarkGreen = String.valueOf(ChatFormatting.DARK_GREEN);

                text.add(colourDarkGreen + "Thermia");
                text.add("Entity:");
                text.add(String.format("Environment Temperature: %.2f", playerData.environmentTemperature()));
                text.add(String.format("Raw Environment Temperature: %.2f", playerData.rawEnvironmentTemperature()));
                text.add(String.format("Environment Humidity: %.2f", playerData.environmentHumidity()));
                text.add(String.format("Player Internal Temperature: %.2f", playerData.internalTemperature()));
                text.add(String.format("Wetness: %.2f", playerData.wetness()));

                Level level = clientPlayer.level();
                ClimateRenderCache climate = ClimateRenderCache.INSTANCE;

                float conductionProtection = ItemInventoryManager.getInventoryConductionProtection(clientPlayer);
                float radiationProtection = ItemInventoryManager.getInventoryRadiationProtection(clientPlayer);
                float convectionProtection = ItemInventoryManager.getInventoryConvectionProtection(clientPlayer);

                float solarRadiation = EnvironmentHelpers.getSolarRadiationWeather(level, pos, playerData.solarShadeResult().shade());
                float solarRadiationWithProtection = solarRadiation * (1f - radiationProtection);

                float windSpeed = EnvironmentHelpers.getWindSpeed(level, pos, playerData.windOcclusionResult().occlusionMultiplier());
                float windSpeedWithProtection  = windSpeed * (1f - convectionProtection);

                float effectiveTemp = EnvironmentHelpers.calcEffectiveTemperature(level, pos, climate.getInstantTemperature(), playerData.environmentHumidity(), playerData.solarShadeResult().shade(), playerData.wetness(), playerData.windOcclusionResult().occlusionMultiplier(), convectionProtection, radiationProtection);

                text.add("Environment:");
                text.add(String.format("Climate: %s", KoppenClimateHumidity.KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.get(KoppenClimateClassification.classify(climate.getAverageTemperature(), climate.getAverageRainfall(), climate.getRainVariance(), SolarCalculator.getInNorthernHemisphere(pos, level))).climateToString()));
                text.add(String.format("Solar Intensity: %.2f, Raw Solar Heating: %.2f, With Radiation Protection: %.2f", solarRadiation, solarRadiation * (float) ServerConfig.MAX_SOLAR_HEATING.getAsDouble(), solarRadiationWithProtection));
                text.add(String.format("Drying Rate: %.2f", EnvironmentHelpers.calcDryingRate(level, pos, effectiveTemp, playerData.environmentHumidity(), playerData.solarShadeResult().shade(), playerData.windOcclusionResult().occlusionMultiplier())));
                text.add(String.format("Evaporative Cooling: %.2f", EnvironmentHelpers.calcEvaporativeCooling(windSpeedWithProtection, playerData.environmentHumidity(), playerData.wetness())));
                text.add(String.format("Wet Bulb: %.2f, Globe: %.2f, WetBulbGlobe(WBGT): %.2f", EnvironmentHelpers.calcWetBulbTemperature(climate.getInstantTemperature(), playerData.environmentHumidity()), EnvironmentHelpers.calcGlobeTemperature(climate.getInstantTemperature(), solarRadiation), EnvironmentHelpers.calcWetBulbGlobeTemperature(level, pos, climate.getInstantTemperature(), playerData.environmentHumidity(), solarRadiation)));
                text.add(String.format("Cold: %.2f, Mild: %.2f, Effective: %.2f", EnvironmentHelpers.calcForCold(climate.getInstantTemperature(), windSpeedWithProtection, solarRadiationWithProtection, playerData.environmentHumidity()),  EnvironmentHelpers.calcForMild(climate.getInstantTemperature(), windSpeedWithProtection, solarRadiationWithProtection, playerData.environmentHumidity()), effectiveTemp));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event)
    {
        if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (!ClientConfig.ENABLE_DEBUG.getAsBoolean()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        Vec3 cameraPos = event.getCamera().getPosition();

        for (Entity entity : minecraft.level.entitiesForRendering())
        {
            if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) continue;

            EntityTemperature entityTemperature = entity.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
            BlockPos sunOcclusionPos = entityTemperature.solarShadeResult().sunOcclusionPos();
            BlockPos windOcclusionPos = entityTemperature.windOcclusionResult().occludingBlock();
            Vec3 entityPos = new Vec3(entity.position().x, entity.position().y + 1.0, entity.position().z);


            if (!windOcclusionPos.equals(BlockPos.ZERO)) renderDebugOcclusionLine(poseStack, bufferSource, cameraPos, entityPos, Vec3.atCenterOf(windOcclusionPos), "wind");

            if (!sunOcclusionPos.equals(BlockPos.ZERO)) renderDebugOcclusionLine(poseStack, bufferSource, cameraPos, entityPos, Vec3.atCenterOf(sunOcclusionPos), "sun");
        }

        bufferSource.endBatch();
    }

    private static void renderDebugOcclusionLine(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, Vec3 entityPos, Vec3 occlusionPos, String type)
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

        float r = 0.0f;
        float g = 0.0f;
        float b = 0.0f;
        float a = 0.0f;

        if (type.equals("sun"))
        {
            r = 1.0f;
            g = 1.0f;
            b = 0.0f;
            a = 0.8f;

            if (entityPos.distanceTo(occlusionPos) >= 32.0)
            {
                r = 0.7f;
                g = 0.7f;
                b = 0.0f;
                a = 0.8f;
            }
        }
        else if (type.equals("wind"))
        {
            r = 0.0f;
            g = 0.5f;
            b = 1.0f;
            a = 0.8f;
        }

        buffer.addVertex(matrix, startX, startY, startZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        buffer.addVertex(matrix, endX, endY, endZ).setColor(r, g, b, a).setNormal(0, 1, 0);

        poseStack.popPose();
    }
}
