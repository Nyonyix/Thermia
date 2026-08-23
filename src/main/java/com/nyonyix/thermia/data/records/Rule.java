package com.nyonyix.thermia.data.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record Rule(List<String> when, float blockSeal)
{
    public static final Codec<Rule> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.list(Codec.STRING).fieldOf("when").forGetter(Rule::when),
            Codec.FLOAT.fieldOf("blockSeal").forGetter(Rule::blockSeal)
    ).apply(i, Rule::new));

    public static final StreamCodec<ByteBuf, Rule> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
