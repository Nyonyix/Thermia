package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ChunkHumidity(float humidity)
{
    public static final Codec<ChunkHumidity> CODEC = RecordCodecBuilder.create(chunkHumidityInstance -> chunkHumidityInstance.group(
            Codec.FLOAT.fieldOf("chunk_humidity").forGetter(ChunkHumidity::humidity)
    ).apply(chunkHumidityInstance, ChunkHumidity::new));

    public static ChunkHumidity createDefault() {return new ChunkHumidity(0.5f);}

    public ChunkHumidity withHumidity(float humidity) {return new ChunkHumidity(humidity);}
}
