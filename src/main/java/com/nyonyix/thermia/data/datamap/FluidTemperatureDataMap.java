package com.nyonyix.thermia.data.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FluidTemperatureDataMap(float temperature, int searchCap, boolean isRadiative)
{
    public static final Codec<FluidTemperatureDataMap> CODEC = RecordCodecBuilder.create(fluidTemperatureDataMapInstance -> fluidTemperatureDataMapInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(FluidTemperatureDataMap::temperature),
            Codec.INT.fieldOf("search_cap").forGetter(FluidTemperatureDataMap::searchCap),
            Codec.BOOL.fieldOf("is_radiative").forGetter(FluidTemperatureDataMap::isRadiative)
    ).apply(fluidTemperatureDataMapInstance, FluidTemperatureDataMap::new));

    public static final StreamCodec<ByteBuf, FluidTemperatureDataMap> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, FluidTemperatureDataMap::temperature,
            ByteBufCodecs.INT, FluidTemperatureDataMap::searchCap,
            ByteBufCodecs.BOOL, FluidTemperatureDataMap::isRadiative,
            FluidTemperatureDataMap::new
    );

    public static FluidTemperatureDataMap createDefault() {return new FluidTemperatureDataMap(0f, 16, true);}
}
