package com.nyonyix.thermia.client.renderer.thick;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.item.thick.ThermiaThickMaterial;
import com.nyonyix.thermia.models.ThermiaThickTorsoModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class ThermiaThickTorsoRenderer implements ICurioRenderer
{
    private final ThermiaThickMaterial material;
    private ModelPart baked;
    private ModelPart torsoModel;
    private ModelPart leftArm;
    private ModelPart rightArm;

    public ThermiaThickTorsoRenderer(ThermiaThickMaterial material)
    {
        this.material = material;
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

        EntityModel<?> model = renderLayerParent.getModel();
        if (!(model instanceof PlayerModel<?> playerModel)) return;

        if (baked == null)
        {
            baked = Minecraft.getInstance().getEntityModels().bakeLayer(ThermiaThickTorsoModel.LAYER_LOCATION);
            torsoModel = baked.getChild("body");
            rightArm = baked.getChild("right_arm").getChild("right_arm_sleeve");
            leftArm = baked.getChild("left_arm").getChild("left_arm_sleeve");
        }

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "textures/models/thick/" + material.name().toLowerCase() + "_thick_torso.png");
        VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.entityCutoutNoCull(texture), stack.hasFoil());

        poseStack.pushPose();
        playerModel.body.translateAndRotate(poseStack);
        poseStack.translate(0.0, -0.7, 0.0);
        torsoModel.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        poseStack.pushPose();
        playerModel.rightArm.translateAndRotate(poseStack);
        poseStack.translate(0.35, 0.66, 0.0);
        rightArm.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        poseStack.pushPose();
        playerModel.leftArm.translateAndRotate(poseStack);
        poseStack.translate(-0.35, 0.66, 0.0);
        leftArm.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
