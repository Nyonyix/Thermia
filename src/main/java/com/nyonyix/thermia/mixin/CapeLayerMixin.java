package com.nyonyix.thermia.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nyonyix.thermia.ClientConfig;
import com.nyonyix.thermia.data.manager.ItemInventoryManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public class CapeLayerMixin
{
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelIfCuriosCape(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci)
    {
        if (!ClientConfig.ENABLE_CAPE.getAsBoolean()) return;
        if (ItemInventoryManager.hasCape(livingEntity)) ci.cancel();
    }
}
