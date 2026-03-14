package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.compress.compressors.lz77support.LZ77Compressor;
import org.spongepowered.asm.util.PrettyPrinter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record Interior(
        InteriorBlocks interiorBlocks,
        Set<BlockPos> internalAirBlocks,
        BlockPos homePos,
        boolean isValid,
        float internalHumidity,
        float externalHumidity,
        float internalTemperature,
        float externalTemperature
)
{
//    private static final Codec<Set<BlockPos>> INTERNAL_AIR_BLOCKS_CODEC = BlockPos.CODEC.listOf().xmap(list -> new HashSet<>(list), set -> new ArrayList<>(set));
    private static final Codec<Set<BlockPos>> INTERNAL_AIR_BLOCKS_CODEC = Codec.LONG.listOf().xmap(
            longs -> longs.stream().map(BlockPos::of).collect(Collectors.toCollection(HashSet::new)),
        set -> set.stream().map(BlockPos::asLong).collect(Collectors.toList())
    );

    public static final Codec<Interior> CODEC = RecordCodecBuilder.create(interiorInstance -> interiorInstance.group(
            InteriorBlocks.CODEC.fieldOf("interior_Blocks").forGetter(Interior::interiorBlocks),
            INTERNAL_AIR_BLOCKS_CODEC.fieldOf("internal_air_blocks").forGetter(Interior::internalAirBlocks),
            BlockPos.CODEC.fieldOf("home_pos").forGetter(Interior::homePos),
            Codec.BOOL.fieldOf("is_valid").forGetter(Interior::isValid),
            Codec.FLOAT.fieldOf("internal_humidity").forGetter(Interior::internalHumidity),
            Codec.FLOAT.fieldOf("external_humidity").forGetter(Interior::externalHumidity),
            Codec.FLOAT.fieldOf("internal_temperature").forGetter(Interior::internalTemperature),
            Codec.FLOAT.fieldOf("external_temperature").forGetter(Interior::externalTemperature)
    ).apply(interiorInstance, Interior::new));

    public static Interior createDefault() {return new Interior(InteriorBlocks.createDefault(), Set.of(), BlockPos.ZERO, false, 0f, 0f, 0f, 0f);}

    public Interior withIsValid(boolean isValid) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withInternalHumidity(float internalHumidity) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.isValid, internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withInternalTemperature(float internalTemperature) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, this.externalHumidity, internalTemperature, this.externalTemperature);}

    public Interior withExternalHumidity(float externalHumidity) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, externalHumidity, this.internalTemperature, this.externalTemperature);}

    public Interior withExternalTemperature(float externalTemperature) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, externalTemperature);}

}
