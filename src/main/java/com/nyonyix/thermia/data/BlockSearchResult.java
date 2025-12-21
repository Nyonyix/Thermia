package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;

public record BlockSearchResult(
        BlockPos nearest,
        BlockPos searchOrigin,
        double nearestDistSq,
        ResourceKey<Level> levelID,
        Map<Block, Integer> counts,
        Map<Block, List<BlockPos>> allPositions
)
{
    public static final Codec<BlockSearchResult> CODEC = RecordCodecBuilder.create(blockSearchResultInstance -> blockSearchResultInstance.group(
            BlockPos.CODEC.optionalFieldOf("nearest", BlockPos.ZERO).forGetter(BlockSearchResult::nearest),
            BlockPos.CODEC.fieldOf("search_origin").forGetter(BlockSearchResult::searchOrigin),
            Codec.DOUBLE.fieldOf("nearest_dist_sq").forGetter(BlockSearchResult::nearestDistSq),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("level_id").forGetter(BlockSearchResult::levelID),
            Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), Codec.INT).fieldOf("counts").forGetter(BlockSearchResult::counts),
            Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), Codec.list(BlockPos.CODEC)).fieldOf("all_positions").forGetter(BlockSearchResult::allPositions)
    ).apply(blockSearchResultInstance, BlockSearchResult::new));

    public double nearestDistance() {return Math.sqrt(nearestDistSq);}

    public int getCount(Block block) {return counts.getOrDefault(block, 0);}

    public int getCount(ResourceLocation blockID) {return counts.getOrDefault(BuiltInRegistries.BLOCK.get(blockID), 0);}

    public List<BlockPos> getPositions(Block block) {return allPositions.getOrDefault(block, Collections.emptyList());}

    public List<BlockPos> getPositions(ResourceLocation blockId) {return allPositions.getOrDefault(BuiltInRegistries.BLOCK.get(blockId), Collections.emptyList());}

    public static BlockSearchResult createDefault()
    {
        return new BlockSearchResult(BlockPos.ZERO, BlockPos.ZERO, 0.0, Level.OVERWORLD, new HashMap<>(), new HashMap<>());
    }

    public BlockSearchResult withNearest(BlockPos nearest)
    {
        return new BlockSearchResult(nearest, this.searchOrigin, this.nearestDistSq, this.levelID, this.counts, this.allPositions);
    }

    public BlockSearchResult withSearchOrigin(BlockPos searchOrigin)
    {
        return new BlockSearchResult(this.nearest, searchOrigin, this.nearestDistSq, this.levelID, this.counts, this.allPositions);
    }

    public BlockSearchResult withNearestDistSq(double nearestDistSq)
    {
        return new BlockSearchResult(this.nearest, this.searchOrigin, nearestDistSq, this.levelID, this.counts, this.allPositions);
    }

    public BlockSearchResult withLevelID(ResourceKey<Level> levelID)
    {
        return new BlockSearchResult(this.nearest, this.searchOrigin, this.nearestDistSq, levelID, this.counts, this.allPositions);
    }

    public BlockSearchResult withCounts(Map<Block, Integer> counts)
    {
        return new BlockSearchResult(this.nearest, this.searchOrigin, this.nearestDistSq, this.levelID, counts, this.allPositions);
    }

    public BlockSearchResult withAllPositions(Map<Block, List<BlockPos>> allPositions)
    {
        return new BlockSearchResult(this.nearest, this.searchOrigin, this.nearestDistSq, this.levelID, this.counts, allPositions);
    }
}