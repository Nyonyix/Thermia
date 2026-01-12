package com.nyonyix.thermia.data.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record EntityTemperatureDataMap(
        float maxEntityTemperature,
        float minEntityTemperature,
        boolean isMob,
        boolean isTamed,
        ResourceLocation homeBlock
)
{
    public static final Codec<EntityTemperatureDataMap> CODEC = RecordCodecBuilder.create(entityTemperatureDataMapInstance -> entityTemperatureDataMapInstance.group(
            Codec.FLOAT.fieldOf("max_entity_temperature").forGetter(EntityTemperatureDataMap::maxEntityTemperature),
            Codec.FLOAT.fieldOf("min_entity_temperature").forGetter(EntityTemperatureDataMap::minEntityTemperature),
            Codec.BOOL.fieldOf("is_mob").forGetter(EntityTemperatureDataMap::isMob),
            Codec.BOOL.fieldOf("is_tamed").forGetter(EntityTemperatureDataMap::isTamed),
            ResourceLocation.CODEC.optionalFieldOf("home_block", ResourceLocation.withDefaultNamespace("air")).forGetter(EntityTemperatureDataMap::homeBlock)
    ).apply(entityTemperatureDataMapInstance, EntityTemperatureDataMap::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityTemperatureDataMap> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, EntityTemperatureDataMap::maxEntityTemperature,
            ByteBufCodecs.FLOAT, EntityTemperatureDataMap::minEntityTemperature,
            ByteBufCodecs.BOOL, EntityTemperatureDataMap::isMob,
            ByteBufCodecs.BOOL, EntityTemperatureDataMap::isTamed,
            ResourceLocation.STREAM_CODEC, EntityTemperatureDataMap::homeBlock,
            EntityTemperatureDataMap::new
    );

    public static EntityTemperatureDataMap createDefault() {return new EntityTemperatureDataMap(40, 10, false, false, ResourceLocation.withDefaultNamespace("air"));}
}
