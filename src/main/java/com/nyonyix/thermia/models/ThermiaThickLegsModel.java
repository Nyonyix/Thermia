package com.nyonyix.thermia.models;

import com.nyonyix.thermia.Thermia;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ThermiaThickLegsModel<T extends Entity>{
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "thick_legs"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition belt = body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(0, 0).addBox(-5.6F, -5.0F, -3.5F, 11.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition leg_right = partdefinition.addOrReplaceChild("leg_right", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition leggings_right = leg_right.addOrReplaceChild("leggings_right", CubeListBuilder.create().texOffs(12, 12).mirror().addBox(-2.45F, -0.5F, 2.25F, 5.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 12).mirror().addBox(-3.2F, -0.5F, -2.5F, 1.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 12).mirror().addBox(2.3F, -0.5F, -2.5F, 1.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(12, 12).mirror().addBox(-2.45F, -0.5F, -3.25F, 5.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leg_left = partdefinition.addOrReplaceChild("leg_left", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition leggings_left = leg_left.addOrReplaceChild("leggings_left", CubeListBuilder.create().texOffs(12, 12).addBox(-2.3F, -0.5F, -3.25F, 5.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(-3.3F, -0.5F, -2.5F, 1.0F, 10.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(12, 12).addBox(-2.3F, -0.5F, 2.25F, 5.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(2.2F, -0.5F, -2.5F, 1.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
	}
}