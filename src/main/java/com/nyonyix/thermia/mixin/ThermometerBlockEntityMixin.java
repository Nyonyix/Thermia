package com.nyonyix.thermia.mixin;

import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.common.blockentities.ThermometerBlockEntity;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ThermometerBlockEntity.class, remap = false)
public class ThermometerBlockEntityMixin
{
    @Inject(method = "serverTick", at = @At("TAIL"), remap = false)
    private static void thermiaThermometerServerTick(Level level, BlockPos pos, BlockState state, ThermometerBlockEntity thermometer, CallbackInfo ci)
    {
        if (level.getGameTime() % 40L == 0L && !state.getValue(TFCBlockStateProperties.THERMOMETER_ATTACHED))
        {
            thermometer.setData(ThermiaAttachments.THERMOMETER, EnvironmentHelpers.getThermometer(level, pos));
        }
    }
}
