package com.nyonyix.thermia.client.renderer.cloth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.item.cloth.ThermiaClothWearableMaterial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
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

public class ThermiaClothBootsRenderer implements ICurioRenderer
{
    private final ThermiaClothWearableMaterial material;
    private HumanoidArmorModel<LivingEntity> armourModel;

    public ThermiaClothBootsRenderer(ThermiaClothWearableMaterial material)
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

        if (armourModel == null)
        {
            armourModel = new HumanoidArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        }

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "textures/models/cloth/" + material.name().toLowerCase() + "_layer1.png");
        VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.entityCutoutNoCull(texture), stack.hasFoil());

        ((HumanoidModel<LivingEntity>) playerModel).copyPropertiesTo(armourModel);

        armourModel.setAllVisible(false);
        armourModel.rightLeg.visible = true;
        armourModel.leftLeg.visible = true;

        armourModel.renderToBuffer(poseStack, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
