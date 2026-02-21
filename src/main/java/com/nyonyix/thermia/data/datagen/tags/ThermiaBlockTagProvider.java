package com.nyonyix.thermia.data.datagen.tags;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.ThermiaTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ThermiaBlockTagProvider extends BlockTagsProvider
{
    public ThermiaBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {super(output, lookupProvider, Thermia.MODID, existingFileHelper);}

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        tag(ThermiaTags.Blocks.INTERIOR_TRIGGERS)
                .addOptional(ResourceLocation.parse("tfc:thatch_bed"))
                .addTag(BlockTags.BEDS);
    }
}
