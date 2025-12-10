package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.*;

public record BlockSearchResult(
        BlockPos nearest,
        double nearestDistSq,
        Map<Block, Integer> counts,
        Map<Block, List<BlockPos>> allPositions
)
{
    public static final Codec<BlockSearchResult> CODEC = RecordCodecBuilder.create(blockSearchResultInstance -> blockSearchResultInstance.group(
            BlockPos.CODEC.fieldOf("nearest").forGetter(BlockSearchResult::nearest),
            Codec.DOUBLE.fieldOf("nearest_dist_sq").forGetter(BlockSearchResult::nearestDistSq),
            Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), Codec.INT).fieldOf("counts").forGetter(BlockSearchResult::counts),
            Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), Codec.list(BlockPos.CODEC)).fieldOf("all_positions").forGetter(BlockSearchResult::allPositions)
    ).apply(blockSearchResultInstance, BlockSearchResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockSearchResult> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BlockSearchResult::nearest,
            ByteBufCodecs.DOUBLE, BlockSearchResult::nearestDistSq,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.BLOCK), ByteBufCodecs.INT), BlockSearchResult::counts,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.BLOCK), ByteBufCodecs.collection(ArrayList::new, BlockPos.STREAM_CODEC)), BlockSearchResult::allPositions,
            BlockSearchResult::new
    );

    public double nearestDistance() {return Math.sqrt(nearestDistSq);}

    public int getCount(Block block) {return counts.getOrDefault(block, 0);}

    public int getCount(ResourceLocation blockID) {return counts.getOrDefault(BuiltInRegistries.BLOCK.get(blockID), 0);}

    public List<BlockPos> getPositions(Block block) {return allPositions.getOrDefault(block, Collections.emptyList());}

    public List<BlockPos> getPositions(ResourceLocation blockId) {return allPositions.getOrDefault(BuiltInRegistries.BLOCK.get(blockId), Collections.emptyList());}
}