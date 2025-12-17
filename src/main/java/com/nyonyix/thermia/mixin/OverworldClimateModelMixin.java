package com.nyonyix.thermia.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.nyonyix.thermia.ServerConfig;
import net.dries007.tfc.util.climate.OverworldClimateModel;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(OverworldClimateModel.class)
public class OverworldClimateModelMixin
{
    @ModifyReturnValue(method = "calculateDailyTemperature", at = @At("RETURN"), remap = false)
    private float increaseDailyVariation(float original) {return ServerConfig.ENABLE_HIGHER_TFC_TEMP.getAsBoolean() ? original * 3.0f + 4 : original;}
}
