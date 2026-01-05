package com.nyonyix.thermia.effect;

import com.nyonyix.thermia.data.ThermiaDamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class HypoThermiaEffect extends MobEffect
{
    public HypoThermiaEffect() {super(MobEffectCategory.HARMFUL, 0x6DBCFF);}

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {return duration % 100 == 0;}

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier)
    {
        if (!entity.isAlive() || entity.isRemoved()) return false;

        entity.hurt(ThermiaDamageTypes.hypoDamageSource(entity.level().registryAccess()), amplifier);
        return true;
    }
}
