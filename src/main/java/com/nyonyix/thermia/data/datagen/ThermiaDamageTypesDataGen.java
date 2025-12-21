package com.nyonyix.thermia.data.datagen;

import com.nyonyix.thermia.data.ThermiaDamageTypes;
import io.netty.bootstrap.Bootstrap;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.item.enchantment.effects.DamageItem;

public class ThermiaDamageTypesDataGen
{
    public static void bootstrap(BootstrapContext<DamageType> contex)
    {
        contex.register(ThermiaDamageTypes.HYPERTHERMIA, new DamageType("hyperthermia", DamageScaling.NEVER, 0.0f, DamageEffects.HURT, DeathMessageType.DEFAULT));
        contex.register(ThermiaDamageTypes.HYPOTHERMIA, new DamageType("hypothermia", DamageScaling.NEVER, 0.0f, DamageEffects.HURT, DeathMessageType.DEFAULT));
    }
}
