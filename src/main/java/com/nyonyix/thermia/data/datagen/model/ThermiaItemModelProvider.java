package com.nyonyix.thermia.data.datagen.model;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.item.cape.ThermiaCapeAnimal;
import com.nyonyix.thermia.item.wideBrimHat.ThermiaWideBrimHatMaterial;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ThermiaItemModelProvider extends ItemModelProvider
{
    public ThermiaItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper)
    {
        super(packOutput, Thermia.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        for (ThermiaCapeAnimal animal : ThermiaCapeAnimal.values())
        {
            String id = animal.name().toLowerCase();
            singleTexture(id + "_pelt", mcLoc("item/generated"), "layer0", modLoc("item/pelt/" + id + "_pelt"));
            singleTexture(id + "_pelt_cape", mcLoc("item/generated"), "layer0", modLoc("item/cape/" + id + "_pelt_cape"));
        }

        for (ThermiaWideBrimHatMaterial material : ThermiaWideBrimHatMaterial.values())
        {
            String id = material.name().toLowerCase();
            singleTexture(id + "_hat", mcLoc("item/generated"), "layer0", modLoc("item/hat/" + id + "wide_brim_hat"));
        }
    }
}
