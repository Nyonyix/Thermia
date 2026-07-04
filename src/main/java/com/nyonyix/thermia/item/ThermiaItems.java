package com.nyonyix.thermia.item;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.item.cape.ThermiaCapeAnimal;
import com.nyonyix.thermia.item.cape.ThermiaCapeItem;
import com.nyonyix.thermia.item.thick.*;
import com.nyonyix.thermia.item.wideBrimHat.ThermiaWideBrimHatItem;
import com.nyonyix.thermia.item.wideBrimHat.ThermiaWideBrimHatMaterial;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.Map;

public class ThermiaItems
{
    public static final Map<ThermiaCapeAnimal, DeferredHolder<Item, Item>> CAPES = new HashMap<>();
    public static final Map<ThermiaCapeAnimal, DeferredHolder<Item, Item>> PELTS = new HashMap<>();
    public static final Map<ThermiaWideBrimHatMaterial, DeferredHolder<Item, Item>> WIDE_BRIM_HATS = new HashMap<>();
    public static final Map<ThermiaThickMaterial, DeferredHolder<Item, Item>> THICK_HEAD = new HashMap<>();
    public static final Map<ThermiaThickMaterial, DeferredHolder<Item, Item>> THICK_TORSO = new HashMap<>();
    public static final Map<ThermiaThickMaterial, DeferredHolder<Item, Item>> THICK_LEGS = new HashMap<>();
    public static final Map<ThermiaThickMaterial, DeferredHolder<Item, Item>> THICK_BOOTS = new HashMap<>();

    public static void register()
    {
        for (ThermiaCapeAnimal animal : ThermiaCapeAnimal.values())
        {
            String id = animal.name().toLowerCase();
            CAPES.put(animal, Thermia.ITEMS.register(id + "_pelt_cape", () -> new ThermiaCapeItem(animal, new Item.Properties())));
            PELTS.put(animal, Thermia.ITEMS.register(id + "_pelt", () -> new ThermiaPeltItem(animal, new Item.Properties())));
        }

        for (ThermiaWideBrimHatMaterial material : ThermiaWideBrimHatMaterial.values())
        {
            String id = material.name().toLowerCase();
            WIDE_BRIM_HATS.put(material, Thermia.ITEMS.register(id + "_wide_brim_hat", () -> new ThermiaWideBrimHatItem(material, new Item.Properties())));
        }

        for (ThermiaThickMaterial material : ThermiaThickMaterial.values())
        {
            String id = material.name().toLowerCase();
            THICK_HEAD.put(material, Thermia.ITEMS.register(id + "_lined_cap", () -> new ThermiaThickHeadItem(material, new Item.Properties())));
            THICK_TORSO.put(material, Thermia.ITEMS.register(id + "_lined_tunic", () -> new ThermiaThickTorsoItem(material, new Item.Properties())));
            THICK_LEGS.put(material, Thermia.ITEMS.register(id + "_lined_pants", () -> new ThermiaThickLegsItem(material, new Item.Properties())));
            THICK_BOOTS.put(material, Thermia.ITEMS.register(id + "_lined_boots", () -> new ThermiaThickBootsItem(material, new Item.Properties())));
        }
    }
}
