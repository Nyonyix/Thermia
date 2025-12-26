package com.nyonyix.thermia.mixin;

import com.nyonyix.thermia.ServerConfig;
import net.dries007.tfc.common.player.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(value = PlayerInfo.class, remap = false)
public class TFCPlayerOverride
{
    @Inject(method = "getThirstContributionFromTemperature", at = @At("RETURN"), cancellable = true, remap = false)
    public void getThirstContributionFromTemperature(CallbackInfoReturnable<Float> cir) {cir.setReturnValue(ServerConfig.OVERRIDE_TFC_TEMP_THIRST.getAsBoolean() ? 0.0f : cir.getReturnValueF());}
}
