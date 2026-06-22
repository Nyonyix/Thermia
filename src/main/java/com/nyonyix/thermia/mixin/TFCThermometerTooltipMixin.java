package com.nyonyix.thermia.mixin;

import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.attachment.Thermometer;
import net.dries007.tfc.common.blockentities.ThermometerBlockEntity;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.tooltip.BlockEntityTooltip;
import net.dries007.tfc.util.tooltip.BlockEntityTooltips;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockEntityTooltips.class, remap = false)
public class TFCThermometerTooltipMixin
{
    @Shadow
    @Mutable
    public static BlockEntityTooltip THERMOMETER;

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void tooltipWrap(CallbackInfo ci)
    {
        final BlockEntityTooltip original = THERMOMETER;

        THERMOMETER = ((level, blockState, blockPos, blockEntity, consumer) -> {
            original.display(level, blockState, blockPos, blockEntity, consumer);
            if (!(blockEntity instanceof ThermometerBlockEntity)) return;
            if (blockState.getValue(TFCBlockStateProperties.THERMOMETER_ATTACHED)) return;

            Thermometer data = blockEntity.getData(ThermiaAttachments.THERMOMETER);
            var style = TFCConfig.CLIENT.climateTooltipStyle.get();
            consumer.accept(Component.translatable("thermia.tooltip.thermometer.thermia_temp"));
            consumer.accept(style.formatRange(data.temperature()));
            consumer.accept(Component.translatable("thermia.tooltip.thermometer.humidity"));
            consumer.accept(Component.literal(String.format("%.0f%%", data.humidity() * 100f)));
        });
    }
}
