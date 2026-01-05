package com.nyonyix.thermia.effect;

import com.nyonyix.thermia.Thermia;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ThermiaEffects
{
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Thermia.MODID);

    public static final Supplier<MobEffect> HYPERTHERMIA = MOB_EFFECTS.register("hyperthermia", HyperThermiaEffect::new);

    public static final Supplier<MobEffect> HYPOTHERMIA = MOB_EFFECTS.register("hypothermia", HypoThermiaEffect::new);
}
