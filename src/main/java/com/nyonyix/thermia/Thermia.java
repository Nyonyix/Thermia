package com.nyonyix.thermia;

import com.nyonyix.thermia.ai.behaviours.ThermiaActivities;
import com.nyonyix.thermia.ai.memories.ThermiaMemoryModules;
import com.nyonyix.thermia.ai.sensors.ThermiaSensorTypes;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import com.nyonyix.thermia.effect.ThermiaEffects;
import com.nyonyix.thermia.item.ThermiaCapeAnimal;
import com.nyonyix.thermia.item.ThermiaItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.curios.api.CuriosApi;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Thermia.MODID)
public class Thermia {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "thermia";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "humidity" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "humidity" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "humidity" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Thermia(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(ThermiaDataMaps::registerDataMapTypes);

//        BLOCKS.register(modEventBus);
        ThermiaItems.register();
        ITEMS.register(modEventBus);

        CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder().title(Component.translatable("thermia.creativeTab")).icon(() -> new ItemStack(ThermiaItems.CAPES.get(ThermiaCapeAnimal.POLAR_BEAR))).displayItems(((itemDisplayParameters, output) -> Thermia.ITEMS.getEntries().forEach(holder -> output.accept(holder.get())))).build());
        CREATIVE_MODE_TABS.register(modEventBus);

        ThermiaAttachments.ATTACHMENTS.register(modEventBus);
        ThermiaEffects.MOB_EFFECTS.register(modEventBus);
        ThermiaMemoryModules.MEMORY_TYPES.register(modEventBus);
        ThermiaSensorTypes.SENSOR_TYPES.register(modEventBus);
        ThermiaActivities.ACTIVITIES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_CONFIG);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_CONFIG);
    }
}
