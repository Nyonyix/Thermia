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
        var capeTag = tag(ThermiaTags.Items.CURIOS_CAPE);
        ThermiaItems.CAPES.values().forEach(h -> capeTag.add(h.getKey()));

        var headTag = tag(ThermiaTags.Items.CURIOS_HEAD);
        ThermiaItems.WIDE_BRIM_HATS.values().forEach(h -> headTag.add(h.getKey()));
        ThermiaItems.THICK_HEAD.values().forEach(h -> headTag.add(h.getKey()));

        var bodyTag = tag(ThermiaTags.Items.CURIOS_BODY);
        ThermiaItems.THICK_TORSO.values().forEach(h -> bodyTag.add(h.getKey()));

        var legsTag = tag(ThermiaTags.Items.CURIOS_LEGS);
        ThermiaItems.THICK_LEGS.values().forEach(h -> legsTag.add(h.getKey()));

        var feetTag = tag(ThermiaTags.Items.CURIOS_FEET);
        ThermiaItems.THICK_BOOTS.values().forEach(h -> feetTag.add(h.getKey()));
    }
}
