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

		PartDefinition root_item = partdefinition.addOrReplaceChild("root_item", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition helmet = root_item.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(28, 10).addBox(-4.0F, -10.0F, -5.0F, 8.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 10).addBox(-6.0F, -10.0F, 3.0F, 12.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition WestSmall = helmet.addOrReplaceChild("WestSmall", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, -5.0F, -5.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 38).addBox(-1.0F, 3.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -5.0F, 0.0F));

		PartDefinition EastSmall = helmet.addOrReplaceChild("EastSmall", CubeListBuilder.create().texOffs(20, 22).addBox(-1.0F, -5.0F, -5.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(32, 0).addBox(-1.0F, 3.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -5.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 48, 48);
	}
}