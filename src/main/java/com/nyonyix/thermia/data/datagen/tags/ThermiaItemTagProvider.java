package com.nyonyix.thermia.data.datagen.tags;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.ThermiaTags;
import com.nyonyix.thermia.item.ThermiaItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ThermiaItemTagProvider extends ItemTagsProvider
{
    public ThermiaItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockLookup, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, blockLookup, Thermia.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        var appender = tag(ThermiaTags.Items.CURIOS_CAPE);
        ThermiaItems.CAPES.values().forEach(h -> appender.add(h.getKey()));
    }
}
