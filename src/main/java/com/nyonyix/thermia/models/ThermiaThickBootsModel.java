package com.nyonyix.thermia.models;

import com.nyonyix.thermia.Thermia;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ThermiaThickBootsModel<T extends Entity>
{
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "thick_boots"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition leg_right = partdefinition.addOrReplaceChild("leg_right", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition boot_right = leg_right.addOrReplaceChild("boot_right", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-4.0F, 6.0F, -3.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 7).mirror().addBox(3.0F, 6.0F, -3.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 0).mirror().addBox(-3.0F, 12.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(14, 7).mirror().addBox(-3.0F, 6.0F, -4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(14, 7).mirror().addBox(-3.0F, 6.0F, 3.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leg_left = partdefinition.addOrReplaceChild("leg_left", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition boot_left = leg_left.addOrReplaceChild("boot_left", CubeListBuilder.create().texOffs(0, 7).addBox(3.0F, 6.0F, -3.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(14, 7).addBox(-3.0F, 6.0F, 3.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, 12.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(14, 7).addBox(-3.0F, 6.0F, -4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 7).addBox(-4.0F, 6.0F, -3.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}