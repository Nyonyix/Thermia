package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.Thermia;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record BlockTemperature(
        float temperature,
        int searchCap,
        ResourceLocation thermalData
)
{
    public static final Codec<BlockTemperature> CODEC = RecordCodecBuilder.create(blockTemperatureInstance -> blockTemperatureInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(BlockTemperature::temperature),
            Codec.INT.fieldOf("search_cap").forGetter(BlockTemperature::searchCap),
            ResourceLocation.CODEC.fieldOf("thermal_data").forGetter(BlockTemperature::thermalData)
    ).apply(blockTemperatureInstance, BlockTemperature::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockTemperature> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, BlockTemperature::temperature,
            ByteBufCodecs.INT, BlockTemperature::searchCap,
            ResourceLocation.STREAM_CODEC, BlockTemperature::thermalData,
            BlockTemperature::new
    );

    public static BlockTemperature createDefault() {return new BlockTemperature(256f, 32, ResourceLocation.fromNamespaceAndPath(Thermia.MODID, "default"));}

    public BlockTemperature withTemperature(float temperature) {return new BlockTemperature(temperature, this.searchCap, this.thermalData);}

    public BlockTemperature withSearchCap(int searchCap) {return new BlockTemperature(this.temperature, searchCap, this.thermalData);}

    public BlockTemperature withThermalData(ResourceLocation thermalData) {return new BlockTemperature(this.temperature, this.searchCap, thermalData);}
}
