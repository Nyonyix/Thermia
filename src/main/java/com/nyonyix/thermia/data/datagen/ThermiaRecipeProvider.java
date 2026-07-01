package com.nyonyix.thermia.data.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.item.cape.ThermiaCapeAnimal;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ThermiaRecipeProvider implements DataProvider
{
    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public ThermiaRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        this.packOutput = packOutput;
        this.lookupProvider = lookupProvider;
    }

    private static JsonObject wrap(String key, JsonObject values)
    {
        JsonObject wrapper = new JsonObject();
        wrapper.add(key, values);
        return wrapper;
    }

    private static JsonObject itemEntry(String itemId)
    {
        JsonObject entry = new JsonObject();
        entry.addProperty("item", itemId);
        return entry;
    }

    private static JsonObject ingredientEntry(String id)
    {
        JsonObject entry = new JsonObject();
        if (id.startsWith("#"))
        {
            entry.addProperty("tag", id.substring(1));
        }
        else
        {
            entry.addProperty("item", id);
        }

        return entry;
    }

    private static String stripPrefix(String id)
    {
        return id.startsWith("#") ? id.substring(1) : id;
    }

    private CompletableFuture<?> saveRecipe(CachedOutput output, String type, String name, JsonObject recipe)
    {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, name);
        Path path = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("tfc").resolve("recipe").resolve(type).resolve(id.getPath() + ".json");

        return DataProvider.saveStable(output, recipe, path);
    }

    private void buildCapeRecipes(BiConsumer<String, JsonObject> output)
    {
        List<List<String>> pattern = List.of(
                List.of("B", "L", " "),
                List.of("S", "L", "S"),
                List.of("S", "P", "S")
        );

        Map<String, String> baseKey = new HashMap<>();
        baseKey.put("B", "tfc:bone_needle");
        baseKey.put("L", "minecraft:leather");
        baseKey.put("S", "#c:strings");

        for (ThermiaCapeAnimal animal : ThermiaCapeAnimal.values())
        {
            String name = animal.name().toLowerCase();
            Map<String, String> itemKey = new HashMap<>(baseKey);
            itemKey.put("P", "thermia:" + name + "_pelt");

            JsonArray modifier = new JsonArray();
            JsonObject type = new JsonObject();

            type.addProperty("type", "tfc:damage_crafting_remainder");
            modifier.add(type);

            JsonObject recipe = buildAdvancedRecipe(pattern, itemKey, modifier, "thermia:" + name + "_pelt_cape", 1, 0, 0);

            output.accept(name + "_cape", recipe);
        }
    }

    private static JsonObject buildAdvancedRecipe(List<List<String>> pattern, Map<String, String> itemKey, JsonArray modifiers, String output, int count, int toolRow, int toolCol)
    {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "tfc:advanced_shaped_crafting");

        JsonArray patternArray = new JsonArray();
        for (List<String> row : pattern)
        {
            patternArray.add(String.join("", row));
        }
        recipe.add("pattern", patternArray);

        JsonObject keyObj = new JsonObject();
        for (var entry : itemKey.entrySet())
        {
            keyObj.add(entry.getKey(), ingredientEntry(entry.getValue()));
        }
        recipe.add("key", keyObj);

        JsonObject resultStack = new JsonObject();
        resultStack.addProperty("id", output);
        resultStack.addProperty("count", count);
        recipe.add("result", resultStack);

        String toolItemId = itemKey.get(pattern.get(toolRow).get(toolCol));
        JsonObject remainderStack = new JsonObject();
        remainderStack.addProperty("id", stripPrefix(toolItemId));

        JsonObject remainderProvider = new JsonObject();
        remainderProvider.add("stack", remainderStack);
        remainderProvider.add("modifiers", modifiers);
        recipe.add("remainder", remainderProvider);

        recipe.addProperty("input_row", toolRow);
        recipe.addProperty("input_column", toolCol);

        return recipe;

    }

    @Override
    public CompletableFuture<?> run(CachedOutput output)
    {

        return lookupProvider.thenCompose(provider -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();

            buildCapeRecipes((name, json) -> futures.add(saveRecipe(output, "crafting", name, json)));

            return CompletableFuture.allOf((futures.toArray(CompletableFuture[]::new)));
        });
    }

    @Override
    public String getName()
    {
        return "Thermia Recipes";
    }
}
