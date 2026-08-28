package com.nyonyix.thermia.data.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.item.cape.ThermiaCapeAnimal;
import com.nyonyix.thermia.item.cloth.ThermiaClothWearableMaterial;
import com.nyonyix.thermia.item.thick.ThermiaThickMaterial;
import net.dries007.tfc.TerraFirmaCraft;
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

    private static JsonArray damageCraftingRemainder()
    {
        JsonArray modifier = new JsonArray();
        JsonObject type = new JsonObject();

        type.addProperty("type", "tfc:damage_crafting_remainder");
        modifier.add(type);
        return modifier;
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
//        Path path = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("tfc").resolve("recipe").resolve(type).resolve(id.getPath() + ".json");

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, name);
        String recipeType = recipe.get("type").getAsString();
        String namespace = recipeType.startsWith("tfc:") ? TerraFirmaCraft.MOD_ID : Thermia.MODID;

        Path path = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(namespace).resolve("recipe").resolve(type).resolve(id.getPath() + ".json");

        return DataProvider.saveStable(output, recipe, path);
    }

    private void buildThickRecipes(BiConsumer<String, JsonObject> output)
    {
        List<List<String>> pattern = List.of(
                List.of("B", "P", " "),
                List.of("S", "L", "S"),
                List.of("W", "W", "W")
        );

        Map<String, String> baseKey = new HashMap();
        baseKey.put("B", "tfc:bone_needle");
        baseKey.put("S", "#c:strings");
        baseKey.put("W", "tfc:wool");
        baseKey.put("P", "minecraft:leather");

        baseKey.put("L", "minecraft:leather_helmet");
        JsonObject recipe = buildAdvanced(pattern, baseKey, damageCraftingRemainder(), "thermia:leather_lined_hat", 1, 0, 0);
        output.accept("leather_lined_hat", recipe);

        baseKey.put("L", "minecraft:leather_chestplate");
        recipe = buildAdvanced(pattern, baseKey, damageCraftingRemainder(), "thermia:leather_lined_coat", 1, 0, 0);
        output.accept("leather_lined_coat", recipe);

        baseKey.put("L", "minecraft:leather_leggings");
        recipe = buildAdvanced(pattern, baseKey, damageCraftingRemainder(), "thermia:leather_lined_pants", 1, 0, 0);
        output.accept("leather_lined_pants", recipe);

        baseKey.put("L", "minecraft:leather_boots");
        recipe = buildAdvanced(pattern, baseKey, damageCraftingRemainder(), "thermia:leather_lined_boots", 1, 0, 0);
        output.accept("leather_lined_boots", recipe);

        for (ThermiaThickMaterial material : ThermiaThickMaterial.values())
        {
            if (material == ThermiaThickMaterial.LEATHER) continue;

            String name = material.name().toLowerCase();
            Map<String, String> itemKey = new HashMap<>(baseKey);
            itemKey.put("P", "thermia:" + name + "_pelt");

            itemKey.put("L", "minecraft:leather_helmet");
            recipe = buildAdvanced(pattern, itemKey, damageCraftingRemainder(), "thermia:" + name + "_lined_hat", 1, 0, 0);
            output.accept(name + "_lined_hat", recipe);

            itemKey.put("L", "minecraft:leather_chestplate");
            recipe = buildAdvanced(pattern, itemKey, damageCraftingRemainder(), "thermia:" + name + "_lined_coat", 1, 0, 0);
            output.accept(name + "_lined_coat", recipe);

            itemKey.put("L", "minecraft:leather_leggings");
            recipe = buildAdvanced(pattern, itemKey, damageCraftingRemainder(), "thermia:" + name + "_lined_pants", 1, 0, 0);
            output.accept(name + "_lined_pants", recipe);

            itemKey.put("L", "minecraft:leather_boots");
            recipe = buildAdvanced(pattern, itemKey, damageCraftingRemainder(), "thermia:" + name + "_lined_boots", 1, 0, 0);
            output.accept(name + "_lined_boots", recipe);
        }
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

            JsonObject recipe = buildAdvanced(pattern, itemKey, damageCraftingRemainder(), "thermia:" + name + "_pelt_cape", 1, 0, 0);

            output.accept(name + "_cape", recipe);
        }
    }

    private void clothRecipes(BiConsumer<String, JsonObject> output)
    {
        List<List<String>> hatPattern = List.of(
                List.of("C", "S", "C"),
                List.of("C", "B", "C")
        );

        List<List<String>> shirtPattern = List.of(
                List.of("C", "B", "C"),
                List.of("S", "C", "S"),
                List.of("C", "C", "C")
        );

        List<List<String>> pantsPattern = List.of(
                List.of("C", "C", "C"),
                List.of("S", "B", "S"),
                List.of("C", " ", "C")
        );

        List<List<String>> shoesPattern = List.of(
                List.of("C", "B", "B"),
                List.of("C", "S", "C")
        );

        buildClothRecipe(output, "hat", hatPattern, 1, 1);
        buildClothRecipe(output, "shirt", shirtPattern, 0, 1);
        buildClothRecipe(output, "pants", pantsPattern, 1, 1);
        buildClothRecipe(output, "shoes", shoesPattern, 0, 1);
    }

    private void buildClothRecipe(BiConsumer<String, JsonObject> output, String item, List<List<String>> pattern, int row, int col)
    {
        Map<String, String> baseKey = new HashMap<>();
        baseKey.put("S", "#c:strings");
        baseKey.put("B", "tfc:bone_needle");

        for (ThermiaClothWearableMaterial material : ThermiaClothWearableMaterial.values())
        {
            String name = material.name().toLowerCase();
            Map<String, String> itemKey = new HashMap<>(baseKey);

            itemKey.put("C", "tfc:" + name + "_cloth");

            JsonObject recipe = buildAdvanced(pattern, itemKey, damageCraftingRemainder(), "thermia:" + name + "_" + item, 1, row, col);

            output.accept(name + "_cloth_" + item, recipe);
        }
    }

    private static void buildPeltRecipes(BiConsumer<String, JsonObject> output)
    {
        for (ThermiaCapeAnimal animal : ThermiaCapeAnimal.values())
        {
            String name = animal.name().toLowerCase();
            String result = "";
            List<String> ingredients = List.of("#c:tools/knife", "thermia:" + name + "_pelt");

            switch (animal)
            {
                case CROCODILE, BLACK_BEAR, GRIZZLY_BEAR, POLAR_BEAR, BISON, COUGAR, COW, DIREWOLF, LION, PANDA, SABERTOOTH, TIGER, YAK ->
                {
                    result = "tfc:large_raw_hide";
                }
                case GOAT ->
                {
                    result = "tfc:medium_raw_hide";
                }
                case WOLF ->
                {
                    result = "tfc:small_raw_hide";
                }
                case MUSK_OX ->
                {
                    result = "tfc:large_sheepskin_hide";
                }
                case ALPACA ->
                {
                    result = "tfc:medium_sheepskin_hide";
                }
                case SHEEP ->
                {
                    result = "tfc:small_sheepskin_hide";
                }
                case null, default ->
                {
                    continue;
                }
            }

            JsonObject recipe = buildAdvancedShapeless(ingredients, result, 1, damageCraftingRemainder());
            output.accept(name + "_pelt", recipe);
        }
    }

    private static void buildWideHatRecipes(BiConsumer<String, JsonObject> output)
    {
        List<List<String>> pattern = List.of(
                List.of("M", "M"),
                List.of("M", " ")
        );

        Map<String, String> itemKey = new HashMap<>();
        itemKey.put("M", "tfc:wool");
        JsonObject recipe = buildShaped(pattern, itemKey, "thermia:felt_wide_brim_hat", 1);
        output.accept("felt_wide_brim_hat", recipe);

        itemKey.put("M", "tfc:straw");
        recipe = buildShaped(pattern, itemKey, "thermia:straw_wide_brim_hat", 1);
        output.accept("straw_wide_brim_hat", recipe);

        itemKey.put("M", "minecraft:leather");
        recipe = buildShaped(pattern, itemKey, "thermia:leather_wide_brim_hat", 1);
        output.accept("leather_wide_brim_hat", recipe);
    }

    private static JsonObject buildShapeless(List<String> ingredients, String output, int count)
    {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");

        JsonArray ingredientArray = new JsonArray();
        for (String ingredient : ingredients)
        {
            ingredientArray.add(ingredientEntry(ingredient));
        }

        JsonObject resultStack = new JsonObject();
        resultStack.addProperty("id", output);
        resultStack.addProperty("count", count);
        recipe.add("result", resultStack);

        return recipe;
    }

    private static JsonObject buildShaped(List<List<String>> pattern, Map<String, String> itemKey, String output, int count)
    {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");

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

        return recipe;
    }

    private static JsonObject buildAdvancedShapeless(List<String> ingredients, String output, int count, JsonArray modifiers)
    {

        JsonObject recipe = new JsonObject();

        recipe.addProperty("type", "tfc:advanced_shapeless_crafting");
        recipe.add("primary_ingredient", ingredientEntry(ingredients.getFirst()));

        JsonArray ingredientArray = new JsonArray();
        for (String ingredient : ingredients)
        {
//            if (ingredients.getFirst().equals(ingredient)) continue;
            ingredientArray.add(ingredientEntry(ingredient));
        }
        recipe.add("ingredients", ingredientArray);

        JsonObject resultStack = new JsonObject();
        resultStack.addProperty("id", output);
        resultStack.addProperty("count", count);
        recipe.add("result", resultStack);

        JsonObject remainderProvider = new JsonObject();
        remainderProvider.add("modifiers", modifiers);
        recipe.add("remainder", remainderProvider);

        return recipe;
    }

    private static JsonObject buildAdvanced(List<List<String>> pattern, Map<String, String> itemKey, JsonArray modifiers, String output, int count, int toolRow, int toolCol)
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
            buildThickRecipes((name, json) -> futures.add(saveRecipe(output, "crafting", name, json)));
            buildWideHatRecipes((name, json) -> futures.add(saveRecipe(output, "crafting", name, json)));
            buildPeltRecipes((name, json) -> futures.add(saveRecipe(output, "crafting", name, json)));
            clothRecipes((name, json) -> futures.add(saveRecipe(output, "crafting", name, json)));

            return CompletableFuture.allOf((futures.toArray(CompletableFuture[]::new)));
        });
    }

    @Override
    public String getName()
    {
        return "Thermia Recipes";
    }
}
