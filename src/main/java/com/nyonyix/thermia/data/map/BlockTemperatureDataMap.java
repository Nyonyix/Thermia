package com.nyonyix.thermia.data.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public record BlockTemperatureDataMap(
        float temperature,
        int searchCap,
        boolean hasTFCHeat,
        boolean isRadiative,
        Map<String, Boolean> stateBools,
        Map<String, Integer> stateInts,
        Map<String, Double> stateDoubles
)
{
    private static final Codec<Map<String, Boolean>> STATE_BOOLS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.BOOL);
    private static final Codec<Map<String, Integer>> STATE_INTS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);
    private static final Codec<Map<String, Double>> STATE_DOUBLES_CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE);

    public static final Codec<BlockTemperatureDataMap> CODEC = RecordCodecBuilder.create(blockTemperatureDataMapInstance -> blockTemperatureDataMapInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(BlockTemperatureDataMap::temperature),
            Codec.INT.fieldOf("search_cap").forGetter(BlockTemperatureDataMap::searchCap),
            Codec.BOOL.fieldOf("has_tfc_heat").forGetter(BlockTemperatureDataMap::hasTFCHeat),
            Codec.BOOL.fieldOf("is_radiative").forGetter(BlockTemperatureDataMap::isRadiative),
            STATE_BOOLS_CODEC.optionalFieldOf("state_bools", Map.of()).forGetter(BlockTemperatureDataMap::stateBools),
            STATE_INTS_CODEC.optionalFieldOf("state_ints", Map.of()).forGetter(BlockTemperatureDataMap::stateInts),
            STATE_DOUBLES_CODEC.optionalFieldOf("state_doubles", Map.of()).forGetter(BlockTemperatureDataMap::stateDoubles)
    ).apply(blockTemperatureDataMapInstance, BlockTemperatureDataMap::new));

    public static BlockTemperatureDataMap createDefault() {return new BlockTemperatureDataMap(256f, 32, false, true, Map.of(), Map.of(), Map.of());}

    public BlockTemperatureDataMap withTemperature(float temperature) {return new BlockTemperatureDataMap(temperature, this.searchCap, this.hasTFCHeat, this.isRadiative, this.stateBools, this.stateInts, this.stateDoubles);}

    public BlockTemperatureDataMap withSearchCap(int searchCap) {return new BlockTemperatureDataMap(this.temperature, searchCap, this.hasTFCHeat, this.isRadiative, this.stateBools, this.stateInts, this.stateDoubles);}

    public BlockTemperatureDataMap withHasTFCHeat(boolean hasTFCHeat) {return new BlockTemperatureDataMap(this.temperature, this.searchCap, hasTFCHeat, this.isRadiative, this.stateBools, this.stateInts, this.stateDoubles);}

    public BlockTemperatureDataMap withIsRadiative(boolean isRadiative) {return new BlockTemperatureDataMap(this.temperature, this.searchCap, this.hasTFCHeat, isRadiative, this.stateBools, this.stateInts, this.stateDoubles);}

    public BlockTemperatureDataMap withStateBools(Map<String, Boolean> stateBools) {return new BlockTemperatureDataMap(this.temperature, this.searchCap, this.hasTFCHeat, this.isRadiative, stateBools, this.stateInts, this.stateDoubles);}

    public BlockTemperatureDataMap withStateInts(Map<String, Integer> stateInts) {return new BlockTemperatureDataMap(this.temperature, this.searchCap, this.hasTFCHeat, this.isRadiative, this.stateBools, stateInts, this.stateDoubles);}

    public BlockTemperatureDataMap withStateDoubles(Map<String, Double> stateDoubles) {return new BlockTemperatureDataMap(this.temperature, this.searchCap, this.hasTFCHeat, this.isRadiative, this.stateBools, this.stateInts, stateDoubles);}
}