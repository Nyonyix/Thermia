package com.nyonyix.thermia.data.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

public record BlockTemperatureDataMap(
        float temperature,
        int searchCap,
        boolean hasTFCHeat,
        boolean isRadiative,
        boolean isHomeBlock,
        Map<String, Float> stateTemps
)
{
    private static final Codec<Map<String, Float>> STATE_TEMPS = Codec.unboundedMap(Codec.STRING, Codec.FLOAT);

    public static final Codec<BlockTemperatureDataMap> CODEC = RecordCodecBuilder.create(blockTemperatureDataMapInstance -> blockTemperatureDataMapInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(BlockTemperatureDataMap::temperature),
            Codec.INT.fieldOf("search_cap").forGetter(BlockTemperatureDataMap::searchCap),
            Codec.BOOL.fieldOf("has_tfc_heat").forGetter(BlockTemperatureDataMap::hasTFCHeat),
            Codec.BOOL.fieldOf("is_radiative").forGetter(BlockTemperatureDataMap::isRadiative),
            Codec.BOOL.fieldOf("is_home_block").forGetter(BlockTemperatureDataMap::isHomeBlock),
            STATE_TEMPS.optionalFieldOf("state_temps", Map.of()).forGetter(BlockTemperatureDataMap::stateTemps)
    ).apply(blockTemperatureDataMapInstance, BlockTemperatureDataMap::new));

    public static BlockTemperatureDataMap createDefault() {return new BlockTemperatureDataMap(256f, 32, false, true, false, Map.of());}

    public float resolveForState(BlockState state)
    {
        float temperature = this.temperature;

        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet())
        {
            String key = entry.getKey().getName() + "=" + entry.getValue().toString();

            if (this.stateTemps.containsKey(key)) temperature = Math.max(temperature, this.stateTemps.get(key));
        }

        return temperature;
    }
}