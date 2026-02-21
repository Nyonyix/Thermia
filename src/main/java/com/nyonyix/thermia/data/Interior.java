package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record Interior(
        Map<BlockPos, BlockState> edgeBlocks,
        Map<BlockPos, BlockState> heatSources,
        Set<BlockPos> internalAirBlocks,
        int edgeAirCount,
        boolean isValid
)
{
    private static final Codec<Set<BlockPos>> INTERNAL_AIR_BLOCKS_CODEC = BlockPos.CODEC.listOf().xmap(list -> new HashSet<>(list), set -> new ArrayList<>(set));

    public static final Codec<Interior> CODEC = RecordCodecBuilder.create(interiorInstance -> interiorInstance.group(
            Codec.unboundedMap(BlockPos.CODEC, BlockState.CODEC).fieldOf("edge_blocks").forGetter(Interior::edgeBlocks),
            Codec.unboundedMap(BlockPos.CODEC, BlockState.CODEC).fieldOf("heat_sources").forGetter(Interior::heatSources),
            INTERNAL_AIR_BLOCKS_CODEC.fieldOf("internal_air_blocks").forGetter(Interior::internalAirBlocks),
            Codec.INT.fieldOf("edge_air_count").forGetter(Interior::edgeAirCount),
            Codec.BOOL.fieldOf("is_valid").forGetter(Interior::isValid)
    ).apply(interiorInstance, Interior::new));

    public static Interior createDefault() {return new Interior(Map.of(), Map.of(), Set.of(), 0, false);}

    public Interior withEdgeBlocks(Map<BlockPos, BlockState> edgeBlocks) {return new Interior(edgeBlocks, this.heatSources, this.internalAirBlocks, this.edgeAirCount, this.isValid);}

    public Interior withHeatSources(Map<BlockPos, BlockState> heatSources) {return new Interior(this.edgeBlocks, heatSources, this.internalAirBlocks, this.edgeAirCount, this.isValid);}

    public Interior withInternalAirBlocks(Set<BlockPos> internalAirBlocks) {return new Interior(this.edgeBlocks, this.heatSources, internalAirBlocks, this.edgeAirCount, this.isValid);}

    public Interior withEdgeAirCount(int edgeAirCount) {return new Interior(this.edgeBlocks, this.heatSources, this.internalAirBlocks, edgeAirCount, this.isValid);}

    public Interior withIsValid(boolean isValid) {return new Interior(this.edgeBlocks, this.heatSources, this.internalAirBlocks, this.edgeAirCount, isValid);}
}
