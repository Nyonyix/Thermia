package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ChunkClimate(
        float humidity
)
{
    public static final Codec<ChunkClimate> CODEC = RecordCodecBuilder.create(chunkClimateInstance -> chunkClimateInstance.group(
            Codec.FLOAT.fieldOf("humidity").forGetter(ChunkClimate::humidity)
    ).apply(chunkClimateInstance, ChunkClimate::new));

    public static final StreamCodec<ByteBuf, ChunkClimate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ChunkClimate::humidity,
            ChunkClimate::new
    );

    public static ChunkClimate createDefault() {return new ChunkClimate(0.5f);}

    public ChunkClimate withHumidity(float humidity) {return new ChunkClimate(humidity);}
}
