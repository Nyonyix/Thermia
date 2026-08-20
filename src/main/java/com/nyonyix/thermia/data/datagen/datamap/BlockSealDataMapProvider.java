package com.nyonyix.thermia.data.datagen.datamap;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.nyonyix.thermia.data.datamap.BlockSealDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockSealDataMapProvider extends DataMapProvider
{
    public BlockSealDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {super(packOutput, lookupProvider);}

    @Override
    protected void gather()
    {
        builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).replace(true)
                .add(BlockTags.FENCE_GATES, new BlockSealDataMap(Map.of("open=true", 0.10f, "open=false", 0.30f),0.30f), false)
                .add(BlockTags.TRAPDOORS, new BlockSealDataMap(Map.of("open=false", 0.10f, "open=true", 0.95f),0.75f), false)
                .add(BlockTags.DOORS, new BlockSealDataMap(Map.of("open=true", 0.10f, "open=false", 0.95f),0.75f), false)
                .add(BlockTags.SLABS, new BlockSealDataMap(Map.of("type=double", 1f),0.50f), false)
                .add(BlockTags.STAIRS, new BlockSealDataMap(Map.of(),0.75f), false);
//                .add(BlockTags.STAIRS, new BlockSealDataMap(Map.of(),0.75f), false);

        if (ModList.get().isLoaded("firmalife"))
        {
            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_BOTTOM.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(Map.of(),0.80f), false, new ModLoadedCondition("firmalife"));
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.INSULATED_OVEN_BOTTOM.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(Map.of(),1.0f), false, new ModLoadedCondition("firmalife"));
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_TOP.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(Map.of(),0.80f), false, new ModLoadedCondition("firmalife"));
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.INSULATED_OVEN_TOP.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(Map.of(),1.0f), false, new ModLoadedCondition("firmalife"));
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_CHIMNEY.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(Map.of(),1.0f), false, new ModLoadedCondition("firmalife"));
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_HOPPER.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockSealDataMap(Map.of(),1.0f), false, new ModLoadedCondition("firmalife"));
            }
        }
    }

    @Override
    public @NotNull String getName() {return "Block_Seal_Data_Map";}
}
