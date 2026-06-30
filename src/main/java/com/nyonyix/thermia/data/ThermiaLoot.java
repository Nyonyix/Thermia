package com.nyonyix.thermia.data;

import com.mojang.serialization.MapCodec;
import com.nyonyix.thermia.Thermia;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ThermiaLoot
{
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM_SERIALISERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Thermia.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ThermiaPeltLootModifier>> PELT_LOOT = GLM_SERIALISERS.register("pelt_loot", () -> ThermiaPeltLootModifier.CODEC);
}
