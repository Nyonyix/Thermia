package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.Thermia;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public record BlockTemperature(
        float temperature,
        float invSqrRange,
        int searchCap,
        boolean hasTFCHeat,
        Block mappedBlock
)
{
    public static final Codec<BlockTemperature> CODEC = RecordCodecBuilder.create(blockTemperatureInstance -> blockTemperatureInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(BlockTemperature::temperature),
            Codec.FLOAT.fieldOf("inv_sqr_range").forGetter(BlockTemperature::invSqrRange),
            Codec.INT.fieldOf("search_cap").forGetter(BlockTemperature::searchCap),
            Codec.BOOL.fieldOf("has_tfc_heat").forGetter(BlockTemperature::hasTFCHeat),
            Block.CODEC.fieldOf("mapped_block").forGetter(BlockTemperature::mappedBlock)
    ).apply(blockTemperatureInstance, BlockTemperature::new));

    public static BlockTemperature createDefault() {return new BlockTemperature(256f, 0, 32, false, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("minecraft", "air")));}

    public BlockTemperature withTemperature(float temperature) {return new BlockTemperature(temperature, this.invSqrRange, this.searchCap, this.hasTFCHeat, this.mappedBlock);}

    public BlockTemperature withInvSqrRange(float invSqrRange) {return new BlockTemperature(this.temperature, invSqrRange, this.searchCap, this.hasTFCHeat, this.mappedBlock);}

    public BlockTemperature withSearchCap(int searchCap) {return new BlockTemperature(this.temperature, this.invSqrRange, searchCap, this.hasTFCHeat, this.mappedBlock);}

    public BlockTemperature withHasTFCHeat(boolean hasTFCHeat) {return new BlockTemperature(this.temperature, this.invSqrRange, this.searchCap, hasTFCHeat, this.mappedBlock);}

    public BlockTemperature withMappedBlock(Block mappedBlock) {return new BlockTemperature(this.temperature, this.invSqrRange, this.searchCap, this.hasTFCHeat, mappedBlock);}
}
