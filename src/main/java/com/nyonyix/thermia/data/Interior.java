package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.compress.compressors.lz77support.LZ77Compressor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record Interior(
        Map<BlockPos, Block> edgeBlocks,
        Map<BlockPos, Block> heatSourceBlocks,
        Map<BlockPos, Fluid> heatSourceFluids,
        Map<BlockPos, Block> heatSinkBlocks,
        Map<BlockPos, Fluid> heatSinkFluids,
        Set<BlockPos> internalAirBlocks,
        BlockPos homePos,
        boolean isValid,
        float internalHumidity,
        float externalHumidity,
        float internalTemperature,
        float externalTemperature
)
{
    private static final Codec<Set<BlockPos>> INTERNAL_AIR_BLOCKS_CODEC = BlockPos.CODEC.listOf().xmap(list -> new HashSet<>(list), set -> new ArrayList<>(set));

    private static final Codec<BlockPos> BLOCKPOS_STRING_CODEC = Codec.STRING.xmap(str -> BlockPos.of(Long.parseLong(str)), pos -> String.valueOf(pos.asLong()));

    public static final Codec<Interior> CODEC = RecordCodecBuilder.create(interiorInstance -> interiorInstance.group(
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("edge_blocks").forGetter(Interior::edgeBlocks),
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("heat_source_blocks").forGetter(Interior::heatSourceBlocks),
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, BuiltInRegistries.FLUID.byNameCodec()).fieldOf("heat_source_fluids").forGetter(Interior::heatSourceFluids),
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("heat_sink_blocks").forGetter(Interior::heatSinkBlocks),
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, BuiltInRegistries.FLUID.byNameCodec()).fieldOf("heat_sink_fluids").forGetter(Interior::heatSinkFluids),
            INTERNAL_AIR_BLOCKS_CODEC.fieldOf("internal_air_blocks").forGetter(Interior::internalAirBlocks),
            BlockPos.CODEC.fieldOf("home_pos").forGetter(Interior::homePos),
            Codec.BOOL.fieldOf("is_valid").forGetter(Interior::isValid),
            Codec.FLOAT.fieldOf("internal_humidity").forGetter(Interior::internalHumidity),
            Codec.FLOAT.fieldOf("external_humidity").forGetter(Interior::externalHumidity),
            Codec.FLOAT.fieldOf("internal_temperature").forGetter(Interior::internalTemperature),
            Codec.FLOAT.fieldOf("external_temperature").forGetter(Interior::externalTemperature)
    ).apply(interiorInstance, Interior::new));

    public static Interior createDefault() {return new Interior(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Set.of(), BlockPos.ZERO, false, 0f, 0f, 0f, 0f);}

    public Interior withEdgeBlocks(Map<BlockPos, Block> edgeBlocks) {return new Interior(edgeBlocks, this.heatSourceBlocks, this.heatSourceFluids, this.heatSinkBlocks, this.heatSinkFluids, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withHeatSourceBlocks(Map<BlockPos, Block> heatSourceBlocks) {return new Interior(this.edgeBlocks, heatSourceBlocks, this.heatSourceFluids, this.heatSinkBlocks, this.heatSinkFluids, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withHeatSourceFluids(Map<BlockPos, Fluid> heatSourceFluids) {return new Interior(this.edgeBlocks, this.heatSourceBlocks, heatSourceFluids, this.heatSinkBlocks, this.heatSinkFluids, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withHeatSinkBlocks(Map<BlockPos, Block> heatSinkBlocks) {return new Interior(this.edgeBlocks, this.heatSourceBlocks, this.heatSourceFluids, heatSinkBlocks, this.heatSinkFluids, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withHeatSinkFluids(Map<BlockPos, Fluid> heatSinkFluids) {return new Interior(this.edgeBlocks, this.heatSourceBlocks, this.heatSourceFluids, this.heatSinkBlocks, heatSinkFluids, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withInternalAirBlocks(Set<BlockPos> internalAirBlocks) {return new Interior(this.edgeBlocks, this.heatSourceBlocks, this.heatSourceFluids, this.heatSinkBlocks, this.heatSinkFluids, internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withHomePos(BlockPos homePos) {return new Interior(this.edgeBlocks, this.heatSourceBlocks, this.heatSourceFluids, this.heatSinkBlocks, this.heatSinkFluids, this.internalAirBlocks, homePos, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withIsValid(boolean isValid) {return new Interior(this.edgeBlocks, this.heatSourceBlocks, this.heatSourceFluids, this.heatSinkBlocks, this.heatSinkFluids, this.internalAirBlocks, this.homePos, isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withInternalHumidity(float internalHumidity) {return new Interior(this.edgeBlocks, this.heatSourceBlocks, this.heatSourceFluids, this.heatSinkBlocks, this.heatSinkFluids, this.internalAirBlocks, this.homePos, this.isValid, internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withExternalHumidity(float externalHumidity) {return new Interior(this.edgeBlocks, this.heatSourceBlocks, this.heatSourceFluids, this.heatSinkBlocks, this.heatSinkFluids, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withInternalTemperature(float internalTemperature) {return new Interior(this.edgeBlocks, this.heatSourceBlocks, this.heatSourceFluids, this.heatSinkBlocks, this.heatSinkFluids, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, this.externalHumidity, internalTemperature, this.externalTemperature);}

    public Interior withExternalTemperature(float externalTemperature) {return new Interior(this.edgeBlocks, this.heatSourceBlocks, this.heatSourceFluids, this.heatSinkBlocks, this.heatSinkFluids, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, externalTemperature);}
}
