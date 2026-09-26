package com.nyonyix.thermia.data.datagen.datamap;

import com.eerussianguy.firmalife.common.FLTags;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.nyonyix.thermia.data.datamap.BlockSealDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import com.nyonyix.thermia.data.records.Rule;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.CopperBlockSet;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockSealDataMapProvider extends DataMapProvider
{
    public BlockSealDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {super(packOutput, lookupProvider);}

    @Override
    protected void gather()
    {
        builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).replace(true)
                .add(BlockTags.FENCE_GATES, new BlockSealDataMap(List.of(
                        new Rule(List.of("open=true"),  0.1f),
                        new Rule(List.of("open=false"), 0.2f)),0.2f), false)

                .add(BlockTags.TRAPDOORS, new BlockSealDataMap(List.of(
                        new Rule(List.of("open=false"),  0.10f),
                        new Rule(List.of("open=true"),  0.99f)),0.99f), false)

                .add(BlockTags.DOORS, new BlockSealDataMap(List.of(
                        new Rule(List.of("open=true"),  0.10f),
                        new Rule(List.of("open=false"),  0.99f)),0.99f), false)

                .add(BlockTags.SLABS, new BlockSealDataMap(List.of(
                        new Rule(List.of("type=double"), 1f)),0.50f), false)

                .add(Tags.Blocks.FENCES, new BlockSealDataMap(List.of(
                        new Rule(List.of("east=false", "north=false", "south=false", "west=false"), 0.1f),
                        new Rule(List.of("east=true", "west=false"), 0.2f),
                        new Rule(List.of("east=false", "west=true"), 0.2f),
                        new Rule(List.of("south=true", "north=false"), 0.2f),
                        new Rule(List.of("south=false", "north=true"), 0.2f)), 0.1f), false)

                .add(Tags.Blocks.GLASS_PANES, new BlockSealDataMap(List.of(
                        new Rule(List.of("east=false", "north=false", "south=false", "west=false"), 0.0f),
                        new Rule(List.of("east=true", "west=false"), 0.5f),
                        new Rule(List.of("east=false", "west=true"), 0.5f),
                        new Rule(List.of("south=true", "north=false"), 0.5f),
                        new Rule(List.of("south=false", "north=true"), 0.5f)), 1f), false)

                .add(BlockTags.WALLS, new BlockSealDataMap(List.of(
                        new Rule(List.of("east=false", "north=false", "south=false", "west=false"), 0.5f),
                        new Rule(List.of("east=none", "west=low"), 0.75f),
                        new Rule(List.of("east=none", "west=tall"), 0.75f),
                        new Rule(List.of("east=low", "west=none"), 0.75f),
                        new Rule(List.of("east=tall", "west=none"), 0.75f),
                        new Rule(List.of("north=none", "south=low"), 0.75f),
                        new Rule(List.of("north=none", "south=tall"), 0.75f),
                        new Rule(List.of("north=low", "south=none"), 0.75f),
                        new Rule(List.of("north=tall", "south=none"), 0.75f)), 1f), false)

                .add(TFCTags.Blocks.AQUEDUCTS, new BlockSealDataMap(List.of(
                        new Rule(List.of("east=false", "north=false", "south=false", "west=false"), 1.0f),
                        new Rule(List.of("east=true", "west=false"), 0.75f),
                        new Rule(List.of("east=false", "west=true"), 0.75f),
                        new Rule(List.of("north=false", "south=true"), 0.75f),
                        new Rule(List.of("north=true", "south=false"), 0.75f)
                ), 1.0f), false)

                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.fromNamespaceAndPath("minecraft", "scaffolding")).orElseThrow(), new BlockSealDataMap(List.of(),0.3f), false)
                .add(BlockTags.STAIRS, new BlockSealDataMap(List.of(),0.75f), false);

        for (var metalEntry : TFCBlocks.METALS.entrySet())
        {
            TFCBlocks.Id<Block> grateId = metalEntry.getValue().get(Metal.BlockType.GRATE);
            if (grateId != null)
            {
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(grateId.holder(), new BlockSealDataMap(List.of(), 0.3f), false);
            }
        }

        String firmaId = "firmalife";
        if (ModList.get().isLoaded(firmaId))
        {
            ModLoadedCondition firmaCondition = new ModLoadedCondition(firmaId);

            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_BOTTOM.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(List.of(),0.80f), false, firmaCondition);
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.INSULATED_OVEN_BOTTOM.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(List.of(),1.0f), false, firmaCondition);
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_TOP.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(List.of(),0.80f), false, firmaCondition);
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.INSULATED_OVEN_TOP.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(List.of(),1.0f), false, firmaCondition);
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_CHIMNEY.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(List.of(),1.0f), false, firmaCondition);
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_HOPPER.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(List.of(),1.0f), false, firmaCondition);
            }

            builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP)
                    .add(FLTags.Blocks.GREENHOUSE_PANEL_ROOFS, new BlockSealDataMap(List.of(), 1.0f), false, firmaCondition)
                    .add(FLTags.Blocks.GREENHOUSE_PANEL_WALLS, new BlockSealDataMap(List.of(), 1.0f), false, firmaCondition);
        }

        String createId = "create";
        if (ModList.get().isLoaded(createId))
        {
            ModLoadedCondition createCondition = new ModLoadedCondition(createId);

            builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP)
                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.fromNamespaceAndPath(createId, "andesite_bars")).orElseThrow(), new BlockSealDataMap(List.of(
                            new Rule(List.of("east=false", "north=false", "south=false", "west=false"), 0.0f),
                            new Rule(List.of("east=false", "west=true"), 0.1f),
                            new Rule(List.of("east=true", "west=false"), 0.1f),
                            new Rule(List.of("north=true", "south=false"), 0.1f),
                            new Rule(List.of("north=false", "south=true"), 0.1f)), 0.2f), false, createCondition)

                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.fromNamespaceAndPath(createId, "brass_bars")).orElseThrow(), new BlockSealDataMap(List.of(
                            new Rule(List.of("east=false", "north=false", "south=false", "west=false"), 0.0f),
                            new Rule(List.of("east=false", "west=true"), 0.1f),
                            new Rule(List.of("east=true", "west=false"), 0.1f),
                            new Rule(List.of("north=true", "south=false"), 0.1f),
                            new Rule(List.of("north=false", "south=true"), 0.1f)), 0.2f), false, createCondition)

                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.fromNamespaceAndPath(createId, "copper_bars")).orElseThrow(), new BlockSealDataMap(List.of(
                            new Rule(List.of("east=false", "north=false", "south=false", "west=false"), 0.0f),
                            new Rule(List.of("east=false", "west=true"), 0.1f),
                            new Rule(List.of("east=true", "west=false"), 0.1f),
                            new Rule(List.of("north=true", "south=false"), 0.1f),
                            new Rule(List.of("north=false", "south=true"), 0.1f)), 0.2f), false, createCondition)

                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.fromNamespaceAndPath(createId, "andesite_scaffolding")).orElseThrow(), new BlockSealDataMap(List.of(), 0.3f), false, createCondition)
                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.fromNamespaceAndPath(createId, "brass_scaffolding")).orElseThrow(), new BlockSealDataMap(List.of(), 0.3f), false, createCondition)
                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.fromNamespaceAndPath(createId, "copper_scaffolding")).orElseThrow(), new BlockSealDataMap(List.of(), 0.3f), false, createCondition)
                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.fromNamespaceAndPath(createId, "copycat_step")).orElseThrow(), new BlockSealDataMap(List.of(), 0.25f), false, createCondition)
                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.fromNamespaceAndPath(createId, "copycat_panel")).orElseThrow(), new BlockSealDataMap(List.of(), 0.0f), false, createCondition);
        }
    }

    @Override
    public @NotNull String getName() {return "Block_Seal_Data_Map";}
}
