package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public record ClosestSource(BlockPos closestPos, ResourceLocation resourceLocation)
{
    public static final Codec<ClosestSource> CODEC = RecordCodecBuilder.create(closestSourceInstance -> closestSourceInstance.group(
        BlockPos.CODEC.fieldOf("closest_pos").forGetter(ClosestSource::closestPos),
        ResourceLocation.CODEC.fieldOf("resource_location").forGetter(ClosestSource::resourceLocation)
    ).apply(closestSourceInstance, ClosestSource::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClosestSource> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClosestSource::closestPos,
            ResourceLocation.STREAM_CODEC, ClosestSource::resourceLocation,
            ClosestSource::new);

    public static ClosestSource createDefault() {return new ClosestSource(new BlockPos(0, 319, 0), ResourceLocation.parse("minecraft:air"));}

    public ClosestSource(BlockPos closestPos, Block block) {this(closestPos, BuiltInRegistries.BLOCK.getKey(block));}

    public ClosestSource withClosestPos(BlockPos closestPos) {return new ClosestSource(closestPos, this.resourceLocation);}

    public ClosestSource withResourceLocation(ResourceLocation resourceLocation) {return new ClosestSource(this.closestPos, resourceLocation);}
}
