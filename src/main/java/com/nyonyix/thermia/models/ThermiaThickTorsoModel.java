package com.nyonyix.thermia.models;

import com.nyonyix.thermia.Thermia;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ThermiaThickTorsoModel<T extends Entity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "thick_torso"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, 0.0F));

        PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -14.0F, -4.0F, 10.0F, 14.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-6.0F, -16.0F, -4.0F, 12.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 0.0F));

        PartDefinition arm_right = partdefinition.addOrReplaceChild("arm_right", CubeListBuilder.create(), PartPose.offset(-5.0F, 12.0F, 0.0F));

        PartDefinition rightArm = arm_right.addOrReplaceChild("right_arm_sleeve", CubeListBuilder.create().texOffs(48, 20).addBox(-4.0F, -2.0F, 2.0F, 4.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 52).addBox(-4.0F, -3.0F, -3.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(48, 36).addBox(-4.0F, -2.0F, -4.0F, 4.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(16, 32).addBox(-5.0F, -2.0F, -3.0F, 2.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(32, 32).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition arm_left = partdefinition.addOrReplaceChild("arm_left", CubeListBuilder.create(), PartPose.offset(5.0F, 12.0F, 0.0F));

        PartDefinition leftArm = arm_left.addOrReplaceChild("left_arm_sleeve", CubeListBuilder.create().texOffs(0, 32).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 52).addBox(0.0F, -3.0F, -3.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(52, 0).addBox(0.0F, -2.0F, -4.0F, 4.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(36, 0).addBox(3.0F, -2.0F, -3.0F, 2.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(40, 52).addBox(0.0F, -2.0F, 2.0F, 4.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 70, 70);
	}
}