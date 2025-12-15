package com.nyonyix.thermia.mixin;

import com.nyonyix.thermia.ServerConfig;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(RegionGenerator.class)
public class RegionGeneratorMixin
{
    @ModifyConstant(method = "<init>", constant = @Constant(doubleValue = 30), remap = false)
    private double modifyMaxTemp(double original) {return ServerConfig.ENABLE_HIGHER_AVG_TEMP.getAsBoolean() ? 40.0 : original;}
}
