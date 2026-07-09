package com.nyonyix.thermia.data.datagen.lang;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.item.ThermiaItems;
import com.nyonyix.thermia.item.cape.ThermiaCapeAnimal;
import com.nyonyix.thermia.item.thick.ThermiaThickMaterial;
import com.nyonyix.thermia.item.wideBrimHat.ThermiaWideBrimHatMaterial;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ThermiaLanguageProvider extends LanguageProvider
{
    public ThermiaLanguageProvider(PackOutput packOutput)
    {
        super(packOutput, Thermia.MODID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        add("thermia.jade.comfort.comfortable", "Comfortable");
        add("thermia.jade.comfort.warm", "Warm");
        add("thermia.jade.comfort.hot", "Hyperthermia");
        add("thermia.jade.comfort.cool", "Cool");
        add("thermia.jade.comfort.cold", "Hypothermia");
        add("config.jade.plugin_thermia.entity_temperature_tooltip", "Entity Temperature");

        add("effect.thermia.hyperthermia", "Hyperthermia");
        add("effect.thermia.hypothermia", "Hypothermia");
        add("death.attack.hyperthermia", "%1$s's brain boiled");
        add("death.attack.hyperthermia.player", "%1$s was too hot for %2$s to handle");
        add("death.attack.hypothermia", "%1$s became an icicle");
        add("death.attack.hypothermia.player", "%1$s was too cool for %2$s");

        add("thermia.configuration.search_and_range", "Search and Range Options");
        add("thermia.configuration.entity_temperature", "Entity Temperature Options");
        add("thermia.configuration.environment_temperature", "Environment Temperature Options");
        add("thermia.configuration.server_misc", "Misc Options");
        add("thermia.configuration.ui", "UI Options");
        add("thermia.configuration.effect", "Effect Options");
        add("thermia.configuration.client_misc", "Misc Options");
        add("thermia.configuration.ai_and_memory", "AI and AI Memory");
        add("thermia.configuration.solar", "Solar Widget");
        add("thermia.configuration.wetness", "Wetness Widget");
        add("thermia.configuration.wind", "Wind Widget");
        add("thermia.configuration.temperature", "Temperature Widget");
        add("thermia.configuration.hypo", "Hypothermia");
        add("thermia.configuration.hyper", "Hyperthermia");

        add("thermia.configuration.searchRange", "Heat source search range");
        add("thermia.configuration.isMobSearchRange", "Mob heat source search range");
        add("thermia.configuration.maxFluidDepthCheck", "Max Fluid Depth Check");
        add("thermia.configuration.maxBlocksAbove", "Maximum blocks above");
        add("thermia.configuration.maxRadiantHeating", "Maximum radiant heat");
        add("thermia.configuration.maxInventoryHeating", "Maximum inventory heat");
        add("thermia.configuration.temperatureSegmentsPercent", "Temperature segment percent");
        add("thermia.configuration.maxTemperatureEffectLevel", "Maximum number of effect levels");
        add("thermia.configuration.entityTemperatureChangeMulti", "Temperature change rate multiplier");
        add("thermia.configuration.playerSweatHydrationLossMulti", "Player sweat hydration loss multiplier");
        add("thermia.configuration.maxSolarHeating", "Maximum heating from sun");
        add("thermia.configuration.evapCoolingMulti", "Evaporative cooling multiplier");
        add("thermia.configuration.solarRadiationMulti", "Solar radiation multiplier");
        add("thermia.configuration.dryingMulti", "Drying rate multiplier");
        add("thermia.configuration.overrideTFCTempThirst", "Override TFC player temperature thirst");
        add("thermia.configuration.inventoryHeatMulti", "Inventory heat multiplier");
        add("thermia.configuration.maxInteriorVolume", "Maximum Interior Volume");
        add("thermia.configuration.maxOpeningDepth", "Maximum Interior Wall Depth");
        add("thermia.configuration.maxOpeningSize", "Maximum Opening Area");

        add("thermia.configuration.uiScale", "UI Scale");
        add("thermia.configuration.uiXOffset", "UI X Offset");
        add("thermia.configuration.uiYOffset", "UI Y Offset");
        add("thermia.configuration.enableHyperRender", "Toggle Hyperthermia Render");
        add("thermia.configuration.enableHypoRender", "Toggle Hypothermia Render");
        add("thermia.configuration.hyperEffectIntensity", "Hyperthermia effect intensity");
        add("thermia.configuration.hypoEffectIntensity", "Hypothermia effect intensity");
        add("thermia.configuration.enableDebug", "Enable Debug");
        add("thermia.configuration.temperatureUiOffsetX", "Temperature Widget X Offset");
        add("thermia.configuration.solarUISubToggle", "Solar Sub-Widget Toggle");
        add("thermia.configuration.solarUiToggle", "Solar Widget Toggle");
        add("thermia.configuration.wetnessUiScale", "Wetness Widget Scale");
        add("thermia.configuration.globalUiScale", "Global Ui Scale");
        add("thermia.configuration.wetnessUiOffsetX", "Wetness Widget X Offset");
        add("thermia.configuration.wetnessUiOffsetY", "Wetness Widget Y Offset");
        add("thermia.configuration.globalUiOffsetY", "Global Widget Y Offset");
        add("thermia.configuration.globalUiOffsetX", "Global Widget X Offset");
        add("thermia.configuration.windUiOffsetX", "Wind Widget X Offset");
        add("thermia.configuration.temperatureUiToggle", "Temperature Widget Toggle");
        add("thermia.configuration.globalUiToggle", "Global Widget Toggle");
        add("thermia.configuration.windUiOffsetY", "Wind Widget Y Offset");
        add("thermia.configuration.windUiToggle", "Wind Widget Toggle");
        add("thermia.configuration.windUiScale", "Wind Widget Scale");
        add("thermia.configuration.solarUiScale", "Solar Widget Scale");
        add("thermia.configuration.temperatureUiScale", "Temperature Widget Scale");
        add("thermia.configuration.solarUiOffsetX", "Solar Widget X Offset");
        add("thermia.configuration.temperatureUiOffsetY", "Temperature Widget Y Offset");
        add("thermia.configuration.solarUiOffsetY", "Solar Widget Y Offset");
        add("thermia.configuration.wetnessUiToggle", "Wetness Widget Toggle");
        add("thermia.configuration.wetnessUISubToggle", "Wetness Sub-Widget Toggle");
        add("thermia.configuration.windUISubToggle", "Wind Sub-Widget Toggle");

        add("thermia.tooltip.thermometer.humidity", "Reading Humidity");
        add("thermia.creativeTab", "Thermia");
        add("thermia.tooltip.thermometer.thermia_temp", "Reading Thermia Temperature");
        add("tooltip.thermia.insulation", "Insulation Value: %1$f");

        for (Map.Entry<ThermiaWideBrimHatMaterial, DeferredHolder<Item, Item>> entry : ThermiaItems.WIDE_BRIM_HATS.entrySet())
        {
            String material = Arrays.stream(entry.getKey().name().toLowerCase().split("_")).map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1)).collect(Collectors.joining(" "));
            String text = String.format("%s Wide Brim Hat", material);

            addItem(entry.getValue(), text);
        }

        for (Map.Entry<ThermiaCapeAnimal, DeferredHolder<Item, Item>> entry : ThermiaItems.PELTS.entrySet())
        {
            String animal = Arrays.stream(entry.getKey().name().toLowerCase().split("_")).map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1)).collect(Collectors.joining(" "));
            String text = String.format("%s Pelt", animal);

            addItem(entry.getValue(), text);
        }

        for (Map.Entry<ThermiaCapeAnimal, DeferredHolder<Item, Item>> entry : ThermiaItems.CAPES.entrySet())
        {
            String animal = Arrays.stream(entry.getKey().name().toLowerCase().split("_")).map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1)).collect(Collectors.joining(" "));
            String text = String.format("%s Cape", animal);

            addItem(entry.getValue(), text);
        }

        for (Map.Entry<ThermiaThickMaterial, DeferredHolder<Item, Item>> entry : ThermiaItems.THICK_HEAD.entrySet())
        {
            String material = Arrays.stream(entry.getKey().name().toLowerCase().split("_")).map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1)).collect(Collectors.joining(" "));
            String text = String.format("%s Insulated Hat", material);

            addItem(entry.getValue(), text);
        }

        for (Map.Entry<ThermiaThickMaterial, DeferredHolder<Item, Item>> entry : ThermiaItems.THICK_TORSO.entrySet())
        {
            String material = Arrays.stream(entry.getKey().name().toLowerCase().split("_")).map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1)).collect(Collectors.joining(" "));
            String text = String.format("%s Insulated Coat", material);

            addItem(entry.getValue(), text);
        }

        for (Map.Entry<ThermiaThickMaterial, DeferredHolder<Item, Item>> entry : ThermiaItems.THICK_LEGS.entrySet())
        {
            String material = Arrays.stream(entry.getKey().name().toLowerCase().split("_")).map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1)).collect(Collectors.joining(" "));
            String text = String.format("%s Insulated Pants", material);

            addItem(entry.getValue(), text);
        }

        for (Map.Entry<ThermiaThickMaterial, DeferredHolder<Item, Item>> entry : ThermiaItems.THICK_BOOTS.entrySet())
        {
            String material = Arrays.stream(entry.getKey().name().toLowerCase().split("_")).map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1)).collect(Collectors.joining(" "));
            String text = String.format("%s Insulated Boots", material);

            addItem(entry.getValue(), text);
        }
    }
}
