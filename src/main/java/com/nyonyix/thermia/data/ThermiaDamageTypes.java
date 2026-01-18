package com.nyonyix.thermia.data;

import com.nyonyix.thermia.Thermia;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class ThermiaDamageTypes
{
    public static final ResourceKey<DamageType> HYPERTHERMIA = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "hyperthermia"));

    public static final ResourceKey<DamageType> HYPOTHERMIA = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "hypothermia"));

    public static DamageSource hyperDamageSource(RegistryAccess access) {return new DamageSource(access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(HYPERTHERMIA), null, null, null);}

    public static DamageSource hypoDamageSource(RegistryAccess access) {return new DamageSource(access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(HYPOTHERMIA), null, null, null);}
}