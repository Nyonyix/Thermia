package com.nyonyix.thermia.client.renderer.thick;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.item.thick.ThermiaThickMaterial;
import com.nyonyix.thermia.models.ThermiaThickHeadModel;
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

public class ThermiaThickHeadRenderer implements ICurioRenderer
{
    private final ThermiaThickMaterial material;
    private ModelPart headModel;

    public ThermiaThickHeadRenderer(ThermiaThickMaterial material)
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

        if (headModel == null)
        {
            headModel = Minecraft.getInstance().getEntityModels().bakeLayer(ThermiaThickHeadModel.LAYER_LOCATION);
        }

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "textures/models/thick/" + material.name().toLowerCase() + "_thick_head.png");
        VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.entityCutoutNoCull(texture), stack.hasFoil());

        poseStack.pushPose();
        playerModel.head.translateAndRotate(poseStack);
        poseStack.translate(0.0, -1.5, 0.0);
        headModel.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
