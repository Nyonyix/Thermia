package com.nyonyix.thermia.effect;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.ThermiaDamageTypes;
import io.netty.util.AttributeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.management.Attribute;

public class HyperThermiaEffect extends MobEffect
{
    public HyperThermiaEffect() {super(MobEffectCategory.HARMFUL, 0xFF4500);}

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {return duration % 100 == 0;}

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier)
    {
        if (!entity.isAlive() || entity.isRemoved()) return false;

        entity.hurt(ThermiaDamageTypes.hyperDamageSource(entity.level().registryAccess()), amplifier);
        return true;
    }
}
