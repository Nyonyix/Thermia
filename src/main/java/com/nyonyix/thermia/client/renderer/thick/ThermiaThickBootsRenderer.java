package com.nyonyix.thermia.client.renderer.thick;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.item.thick.ThermiaThickMaterial;
import com.nyonyix.thermia.models.ThermiaThickBootsModel;
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

public class ThermiaThickBootsRenderer implements ICurioRenderer
{
    private final ThermiaThickMaterial material;
    private ModelPart baked;
    private ModelPart leftBoot;
    private ModelPart rightBoot;

    public ThermiaThickBootsRenderer(ThermiaThickMaterial material)
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
            baked = Minecraft.getInstance().getEntityModels().bakeLayer(ThermiaThickBootsModel.LAYER_LOCATION);
            leftBoot = baked.getChild("leg_left").getChild("left_foot");
            rightBoot = baked.getChild("leg_right").getChild("right_foot");
        }

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "textures/models/thick/" + material.name().toLowerCase() + "_thick_boots.png");
        VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.entityCutoutNoCull(texture), stack.hasFoil());

        poseStack.pushPose();
        playerModel.rightLeg.translateAndRotate(poseStack);
        poseStack.translate(0.1, 0.725, 0);
        rightBoot.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        poseStack.pushPose();
        playerModel.leftLeg.translateAndRotate(poseStack);
        poseStack.translate(-0.1, 0.725, 0);
        leftBoot.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
