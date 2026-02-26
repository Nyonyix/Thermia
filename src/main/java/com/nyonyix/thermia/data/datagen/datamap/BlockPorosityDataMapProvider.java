package com.nyonyix.thermia.data.datagen.datamap;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.oven.OvenType;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.datamap.BlockPorosityDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import com.therighthon.afc.common.blocks.AFCWood;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.wood.TFCTrapDoorBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockPorosityDataMapProvider extends DataMapProvider
{
    public BlockPorosityDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {super(packOutput, lookupProvider);}

    @Override
    protected void gather()
    {
        builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).replace(true)
                .add(BlockTags.FENCES, new BlockPorosityDataMap(Map.of("east=true", 0.50f, "north=true", 0.50f, "south=true", 0.50f, "west=true", 0.50f),0.30f), false)
                .add(BlockTags.FENCE_GATES, new BlockPorosityDataMap(Map.of("open=true", 0.10f, "open=false", 0.30f),0.30f), false)
                .add(BlockTags.TRAPDOORS, new BlockPorosityDataMap(Map.of("open=true", 0.10f, "open=false", 0.80f),0.75f), false)
                .add(BlockTags.DOORS, new BlockPorosityDataMap(Map.of("open=true", 0.10f, "open=false", 0.80f),1f), false)
                .add(BlockTags.WALLS, new BlockPorosityDataMap(Map.of("east=low", 0.70f, "north=low", 0.70f, "south=low", 0.70f, "west=low", 0.70f, "east=tall", 1f, "north=tall", 1f, "south=tall", 1f, "west=tall", 1f, "up=true", 0.70f),0.70f), false)
                .add(BlockTags.SLABS, new BlockPorosityDataMap(Map.of("type=double", 1f),0.50f), false)
                .add(BlockTags.STAIRS, new BlockPorosityDataMap(Map.of(),0.75f), false)
                .add(Tags.Blocks.CHESTS, new BlockPorosityDataMap(Map.of(),0.90f), false)

                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:bellows")).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.80f), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:firepit")).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.30f), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:grill")).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.40f), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:pot")).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.60f), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:stove")).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.80f), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:stove_pot")).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.80f), false);

        for (Wood wood : Wood.values())
        {
            String bookshelfID = String.format("tfc:wood/bookshelf/%s", wood.getSerializedName());
            String encasedAxleID = String.format("tfc:wood/encased_axle/%s", wood.getSerializedName());
            String clutchID = String.format("tfc:wood/clutch/%s", wood.getSerializedName());
            String gearBoxID = String.format("tfc:wood/gear_box/%s", wood.getSerializedName());
            String leavesID = String.format("tfc:wood/leaves/%s", wood.getSerializedName());

            builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP)
                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(bookshelfID)).orElseThrow(), new BlockPorosityDataMap(Map.of(),1.0f), false)
                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(encasedAxleID)).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.95f), false)
                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(clutchID)).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.95f), false)
                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(gearBoxID)).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.95f), false)
                    .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(leavesID)).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.95f), false);
        }

        if (ModList.get().isLoaded("afc"))
        {
            for (AFCWood wood : AFCWood.values())
            {
                String bookshelfID = String.format("afc:wood/bookshelf/%s", wood.getSerializedName());
                String encasedAxleID = String.format("afc:wood/encased_axle/%s", wood.getSerializedName());
                String clutchID = String.format("afc:wood/clutch/%s", wood.getSerializedName());
                String gearBoxID = String.format("afc:wood/gear_box/%s", wood.getSerializedName());
                String leavesID = String.format("afc:wood/leaves/%s", wood.getSerializedName());

                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP)
                        .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(bookshelfID)).orElseThrow(), new BlockPorosityDataMap(Map.of(),1.0f), false)
                        .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(encasedAxleID)).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.95f), false)
                        .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(clutchID)).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.95f), false)
                        .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(gearBoxID)).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.95f), false)
                        .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(leavesID)).orElseThrow(), new BlockPorosityDataMap(Map.of(),0.95f), false);
            }
        }

        if (ModList.get().isLoaded("firmalife"))
        {
            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_BOTTOM.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockPorosityDataMap(Map.of(),0.80f), false);
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.INSULATED_OVEN_BOTTOM.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockPorosityDataMap(Map.of(),1.0f), false);
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_TOP.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockPorosityDataMap(Map.of(),0.80f), false);
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.INSULATED_OVEN_TOP.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockPorosityDataMap(Map.of(),1.0f), false);
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_CHIMNEY.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockPorosityDataMap(Map.of(),1.0f), false);
            }

            for (TFCBlocks.Id<Block> ID : FLBlocks.CURED_OVEN_HOPPER.values())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(ID.get());
                builder(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP).add(blockHolder, new BlockPorosityDataMap(Map.of(),1.0f), false);
            }
        }
    }

    @Override
    public @NotNull String getName() {return "Block_Porosity_Data_Map";}
}
