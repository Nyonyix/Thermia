package com.nyonyix.thermia.data.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FluidTemperatureDataMap(float temperature, int searchCap)
{
    public static final Codec<FluidTemperatureDataMap> CODEC = RecordCodecBuilder.create(fluidTemperatureDataMapInstance -> fluidTemperatureDataMapInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(FluidTemperatureDataMap::temperature),
            Codec.INT.fieldOf("search_cap").forGetter(FluidTemperatureDataMap::searchCap)
    ).apply(fluidTemperatureDataMapInstance, FluidTemperatureDataMap::new));

    public static final StreamCodec<ByteBuf, FluidTemperatureDataMap> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, FluidTemperatureDataMap::temperature,
            ByteBufCodecs.INT, FluidTemperatureDataMap::searchCap,
            FluidTemperatureDataMap::new
    );

    public static FluidTemperatureDataMap createDefault() {return new FluidTemperatureDataMap(0f, 16);}

    public FluidTemperatureDataMap withTemperature(float temperature) {return new FluidTemperatureDataMap(temperature, this.searchCap);}

    public FluidTemperatureDataMap withSearchCap(int searchCap) {return new FluidTemperatureDataMap(this.temperature, searchCap);}
}
