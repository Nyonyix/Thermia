package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Thermometer(
        float temperature,
        float humidity
)
{
    public static final Codec<Thermometer> CODEC = RecordCodecBuilder.create(thermometerInstance -> thermometerInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(Thermometer::temperature),
            Codec.FLOAT.fieldOf("humidity").forGetter(Thermometer::humidity)
    ).apply(thermometerInstance, Thermometer::new));

    public static final StreamCodec<ByteBuf, Thermometer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, Thermometer::temperature,
            ByteBufCodecs.FLOAT, Thermometer::humidity,
            Thermometer::new
    );

    public static Thermometer createDefault() {return new Thermometer(0f, 0f);}

    public Thermometer withTemperature(float temperature) {return new Thermometer(temperature, this.humidity);}
    public Thermometer withHumidity(float humidity) {return new Thermometer(this.temperature, humidity);}
}
