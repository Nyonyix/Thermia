package com.nyonyix.thermia.registry.builtins;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.api.sources.ThermiaSources;
import com.nyonyix.thermia.data.manager.ItemInventoryManager;
import net.minecraft.resources.ResourceLocation;

public class ThermiaSourceBuiltins
{
    private static ResourceLocation id(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(Thermia.MODID, path);
    }

    public static void register()
    {
        ThermiaSources.RADIANT.register(id("radiant"), ctx -> ctx.temperature().blockSearchResult().getRadiance(ctx.level(), ctx.entity()));
        ThermiaSources.CONDUCTION.register(id("conduction"), ctx -> ctx.temperature().blockSearchResult().getContact(ctx.level(), ctx.entity(), ctx.dataMap().isMob()));
        ThermiaSources.IMMERSION.register(id("immersion"), ctx -> ctx.temperature().blockSearchResult().getImmersion(ctx.level(), ctx.entity()));
        ThermiaSources.INVENTORY.register(id("inventory"), ctx -> ItemInventoryManager.getInventoryTemperature(ctx.entity()));
    }
}
