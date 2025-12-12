package com.nyonyix.thermia.data.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BlockTemperatureDataMap(
    float temperature,
    int searchCap,
    boolean hasTFCHeat
)
{
    public static final Codec<BlockTemperatureDataMap> CODEC = RecordCodecBuilder.create(blockTemperatureDataMapInstance -> blockTemperatureDataMapInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(BlockTemperatureDataMap::temperature),
            Codec.INT.fieldOf("search_cap").forGetter(BlockTemperatureDataMap::searchCap),
            Codec.BOOL.fieldOf("has_tfc_heat").forGetter(BlockTemperatureDataMap::hasTFCHeat)
    ).apply(blockTemperatureDataMapInstance, BlockTemperatureDataMap::new));

    public static final StreamCodec<ByteBuf, BlockTemperatureDataMap> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, BlockTemperatureDataMap::temperature,
            ByteBufCodecs.INT, BlockTemperatureDataMap::searchCap,
            ByteBufCodecs.BOOL, BlockTemperatureDataMap::hasTFCHeat,
            BlockTemperatureDataMap::new
    );

    public BlockTemperatureDataMap
    {
        if (hasTFCHeat || temperature != 0) new IllegalArgumentException("Block data map provided temperature but is marked \"hasTFCHeat\"");
    }

    public static BlockTemperatureDataMap createDefault() {return new BlockTemperatureDataMap(256f, 32, false);}

    public BlockTemperatureDataMap withTemperature(float temperature) {return new BlockTemperatureDataMap(temperature, this.searchCap, this.hasTFCHeat);}

    public BlockTemperatureDataMap withSearchCap(int searchCap) {return new BlockTemperatureDataMap(this.temperature, searchCap, this.hasTFCHeat);}

    public BlockTemperatureDataMap withHasTFCHeat(boolean hasTFCHeat) {return new BlockTemperatureDataMap(this.temperature, this.searchCap, hasTFCHeat);}
}
