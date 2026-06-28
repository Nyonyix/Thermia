package com.nyonyix.thermia.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.nyonyix.thermia.ClientConfig;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.item.ThermiaCapeAnimal;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import javax.swing.*;

public class ThermiaCapeRenderer implements ICurioRenderer
{
    private final ThermiaCapeAnimal animal;

    public ThermiaCapeRenderer(ThermiaCapeAnimal animal)
    {
        this.animal = animal;
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
    SlotContext slotContext,
    PoseStack poseStack,
    RenderLayerParent<T, M> renderLayerParent,
    MultiBufferSource bufferSource,
    int light,
    float limbSwing,
    float limbSwingAmount,
    float partialTicks,
    float ageInTicks,
    float netHeadYaw,
    float headPitch)
    {
        LivingEntity entity = slotContext.entity();
        if (entity.isInvisible()) return;
        if (!ClientConfig.ENABLE_CAPE.getAsBoolean()) return;

        if (!(entity instanceof AbstractClientPlayer player)) return;

        EntityModel<?> model = renderLayerParent.getModel();
        if (!(model instanceof PlayerModel<?> playerModel)) return;

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "textures/cape/" + animal.name().toLowerCase() + "_pelt_cape.png");
        VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.entityCutoutNoCull(texture), stack.hasFoil());

        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 0.125f);

        double dX = Mth.lerp((double) partialTicks, player.xCloakO, player.xCloak) - Mth.lerp((double) partialTicks, player.xo, player.getX());
        double dY = Mth.lerp((double) partialTicks, player.yCloakO, player.yCloak) - Mth.lerp((double) partialTicks, player.yo, player.getY());
        double dZ = Mth.lerp((double) partialTicks, player.zCloakO, player.zCloak) - Mth.lerp((double) partialTicks, player.zo, player.getZ());

        float bodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        double sin = (double) Mth.sin(bodyYaw * ((float) Math.PI / 180.0f));
        double cos = (double) (-Mth.cos(bodyYaw * (float) Math.PI / 180.0f));

        float vertical = Mth.clamp((float) dY * 10.0f, -6.0f, 32.0f);
        float forward = (float) (dX * sin + dZ * cos) * 100.0f;
        forward = Mth.clamp(forward, 0.0f, 150.0f);
        float lateral = (float) (dX * cos - dZ * sin) * 100.0f;
        lateral = Mth.clamp(lateral, -20.0f, 20.0f);

        float bob = Mth.lerp(partialTicks, player.oBob, player.bob);
        vertical += Mth.sin(Mth.lerp(partialTicks, player.walkDistO, player.walkDist) * 6.0f) * 32.0f * bob;
        if (player.isCrouching())
        {
            vertical += 25.0f;
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(6.0f + forward / 2.0f + vertical));
        poseStack.mulPose(Axis.ZP.rotationDegrees(lateral / 2.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - lateral / 2.0f));

        playerModel.cloak.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
