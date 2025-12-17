package com.nyonyix.thermia.client;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

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
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.isPaused()) return;

        long currentTick = minecraft.level.getGameTime();

        if (currentTick % 10 == 0)
        {
            Entity camera = minecraft.getCameraEntity();
            if (camera != null)
            {
                BlockPos pos = camera.blockPosition();
                if (minecraft.level.hasChunk(pos.getX() / 16, pos.getZ() / 16))
                {
                    ThermiaClientRenderCache.onClientTick();
                }
            }
        }
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
                EntityTemperature playerData = clientPlayer.getData(ThermiaAttachments.ENTITY_TEMPERATURE) != null ? clientPlayer.getData(ThermiaAttachments.ENTITY_TEMPERATURE) : EntityTemperature.createDefault();

                List<String> text = event.getLeft();
                text.add("");
                String colourDarkGreen = String.valueOf(ChatFormatting.DARK_GREEN);

                text.add(colourDarkGreen + "Thermia");
                text.add(String.format("Environment Temperature: %.2f", playerData.environmentTemperature()));
                text.add(String.format("Environment Humidity: %.2f", playerData.environmentHumidity()));
                text.add(String.format("Player internal Temp: %.2f", playerData.internalTemperature()));
            }
        }
    }
}
