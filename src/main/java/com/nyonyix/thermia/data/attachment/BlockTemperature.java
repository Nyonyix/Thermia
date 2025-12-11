package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.Thermia;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record BlockTemperature(
        float temperature,
        int searchCap
)
{
    public static final Codec<BlockTemperature> CODEC = RecordCodecBuilder.create(blockTemperatureInstance -> blockTemperatureInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(BlockTemperature::temperature),
            Codec.INT.fieldOf("search_cap").forGetter(BlockTemperature::searchCap)
    ).apply(blockTemperatureInstance, BlockTemperature::new));

    public static final StreamCodec<ByteBuf, BlockTemperature> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, BlockTemperature::temperature,
            ByteBufCodecs.INT, BlockTemperature::searchCap,
            BlockTemperature::new
    );

    public static BlockTemperature createDefault() {return new BlockTemperature(256f, 32);}

    public BlockTemperature withTemperature(float temperature) {return new BlockTemperature(temperature, this.searchCap);}

    public BlockTemperature withSearchCap(int searchCap) {return new BlockTemperature(this.temperature, searchCap);}
}
