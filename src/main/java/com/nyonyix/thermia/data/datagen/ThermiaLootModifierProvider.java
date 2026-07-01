package com.nyonyix.thermia.data.datagen;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.ThermiaPeltLootModifier;
import com.nyonyix.thermia.item.cape.ThermiaCapeAnimal;
import com.nyonyix.thermia.item.ThermiaItems;
import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ThermiaLootModifierProvider extends GlobalLootModifierProvider
{
    public ThermiaLootModifierProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(packOutput, lookupProvider, Thermia.MODID);
    }

    @Override
    protected void start()
    {
        for (ThermiaCapeAnimal animal : ThermiaCapeAnimal.values())
        {
            Item peltItem = ThermiaItems.PELTS.get(animal).get();
            String entityName = animal.name().toLowerCase();
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, entityName));

            add("pelt_from_" + entityName, new ThermiaPeltLootModifier(List.of(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(entityType).build()).build()), peltItem));
        }
    }
}
