package com.nyonyix.thermia.models;

import com.nyonyix.thermia.Thermia;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ThermiaThickHeadModel<T extends Entity>{
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "thick_head"), "main");


    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root_item = partdefinition.addOrReplaceChild("root_item", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition helmet = root_item.addOrReplaceChild("helmet", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head_details = helmet.addOrReplaceChild("head_details", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_ear_r1 = head_details.addOrReplaceChild("right_ear_r1", CubeListBuilder.create().texOffs(36, 0).addBox(-1.0F, -6.0F, -1.25F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 6.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition left_ear_r1 = head_details.addOrReplaceChild("left_ear_r1", CubeListBuilder.create().texOffs(36, 9).addBox(-1.0F, -6.0F, 0.25F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -6.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition crown_r1 = head_details.addOrReplaceChild("crown_r1", CubeListBuilder.create().texOffs(0, 39).addBox(0.5F, -3.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -6.0F, 0.0F, 0.0F, 0.0F, -0.0436F));

        PartDefinition head = helmet.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
                .texOffs(0, 16).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 48, 48);
	}
}