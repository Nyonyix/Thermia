package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ChunkClimate(
        float humidity,
        float wetBulb
)
{
    public static final Codec<ChunkClimate> CODEC = RecordCodecBuilder.create(chunkClimateInstance -> chunkClimateInstance.group(
            Codec.FLOAT.fieldOf("humidity").forGetter(ChunkClimate::humidity),
            Codec.FLOAT.fieldOf("wet_bulb").forGetter(ChunkClimate::wetBulb)
    ).apply(chunkClimateInstance, ChunkClimate::new));

    public static final StreamCodec<ByteBuf, ChunkClimate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ChunkClimate::humidity,
            ByteBufCodecs.FLOAT, ChunkClimate::wetBulb,
            ChunkClimate::new
    );

    public static ChunkClimate createDefault() {return new ChunkClimate(0.5f, 20f);}

    public ChunkClimate withHumidity(float humidity) {return new ChunkClimate(humidity, this.wetBulb);}

    public ChunkClimate withWetBulb(float wetBulb) {return new ChunkClimate(this.humidity, wetBulb);}
}
