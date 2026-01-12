package com.nyonyix.thermia.data.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record BlockTemperatureDataMap(
        float temperature,
        int searchCap,
        boolean hasTFCHeat,
        boolean isRadiative,
        boolean isHomeBlock,
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
            Codec.BOOL.fieldOf("is_home_block").forGetter(BlockTemperatureDataMap::isHomeBlock),
            STATE_BOOLS_CODEC.optionalFieldOf("state_bools", Map.of()).forGetter(BlockTemperatureDataMap::stateBools),
            STATE_INTS_CODEC.optionalFieldOf("state_ints", Map.of()).forGetter(BlockTemperatureDataMap::stateInts),
            STATE_DOUBLES_CODEC.optionalFieldOf("state_doubles", Map.of()).forGetter(BlockTemperatureDataMap::stateDoubles)
    ).apply(blockTemperatureDataMapInstance, BlockTemperatureDataMap::new));

    public static BlockTemperatureDataMap createDefault() {return new BlockTemperatureDataMap(256f, 32, false, true, false, Map.of(), Map.of(), Map.of());}
}