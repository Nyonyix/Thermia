package com.nyonyix.thermia.data.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ThermiaPatchouliProvider implements DataProvider
{
    private final PackOutput output;
    private final String modID;

    private CompletableFuture<?> createEntry(CachedOutput cache, String id, String name, String desc, String icon, int sortNum)
    {
        JsonObject category = new JsonObject();

        category.addProperty("name", name);
        category.addProperty("description", desc);
        category.addProperty("icon", icon);
        category.addProperty("sortnum", sortNum);

        Path categoryPath = output.getOutputFolder().resolve("assets/tfc/patchouli_books/field_guide/en_us/entires/" + id + ".json");

        return DataProvider.saveStable(cache, category, categoryPath);
    }

    private CompletableFuture<?> generateEntries(CachedOutput cache)
    {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(createEntry(cache, "mechanics", "patchouli.thermia.entry.mechanics", "patchouli.thermia.entry.mechanics.desc", "thermia:textures/logo.png", 0));
        futures.add(createEntry(cache, "heat_sources", "patchouli.thermia.entry.heat_sources", "patchouli.thermia.entry.heat_sources.desc", "thermia:textures/mob_effects/hyperthermia.png", 1));
        futures.add(createEntry(cache, "cold_sources", "patchouli.thermia.entry.cold_sources", "patchouli.thermia.entry.cold_sources.desc", "thermia:textures/mob_effects/hypothermia.png", 2));
        futures.add(createEntry(cache, "insulation", "patchouli.thermia.entry.insulation", "patchouli.thermia.entry.insulation.desc", "minecraft:leather", 3));
        futures.add(createEntry(cache, "animals", "patchouli.thermia.entry.animals", "patchouli.thermia.entry.animals.desc", "tfc:pig", 4));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<?> generateCategory(CachedOutput cache)
    {
        JsonObject category = new JsonObject();

        category.addProperty("name", "patchouli.thermia.category.thermia");
        category.addProperty("description", "patchouli.thermia.category.thermia.desc");
        category.addProperty("icon", "thermia:textures/logo.png");

        Path categoryPath = output.getOutputFolder().resolve("assets/tfc//patchouli_books/field_guide/en_us/category/thermia.json");
    }

    public ThermiaPatchouliProvider(PackOutput output, String modId)
    {
        this.output = output;
        this.modID = modId;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {return CompletableFuture.allOf(generateEntries(cache));}

    @Override
    public String getName() {return "thermia_patchouli_provider";}
}
