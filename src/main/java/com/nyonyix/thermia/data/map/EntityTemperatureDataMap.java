package com.nyonyix.thermia.data.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EntityTemperatureDataMap(
        float maxEntityTemperature,
        float minEntityTemperature,
        boolean isAnimal,
        boolean isTamable
)
{
    public static final Codec<EntityTemperatureDataMap> CODEC = RecordCodecBuilder.create(entityTemperatureDataMapInstance -> entityTemperatureDataMapInstance.group(
            Codec.FLOAT.fieldOf("max_entity_temperature").forGetter(EntityTemperatureDataMap::maxEntityTemperature),
            Codec.FLOAT.fieldOf("min_entity_temperature").forGetter(EntityTemperatureDataMap::minEntityTemperature),
            Codec.BOOL.fieldOf("is_animal").forGetter(EntityTemperatureDataMap::isAnimal),
            Codec.BOOL.fieldOf("is_tamable").forGetter(EntityTemperatureDataMap::isTamable)
    ).apply(entityTemperatureDataMapInstance, EntityTemperatureDataMap::new));

    public static final StreamCodec<ByteBuf, EntityTemperatureDataMap> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, EntityTemperatureDataMap::maxEntityTemperature,
            ByteBufCodecs.FLOAT, EntityTemperatureDataMap::minEntityTemperature,
            ByteBufCodecs.BOOL, EntityTemperatureDataMap::isAnimal,
            ByteBufCodecs.BOOL, EntityTemperatureDataMap::isTamable,
            EntityTemperatureDataMap::new
    );

    public EntityTemperatureDataMap
    {
        if (isTamable && !isAnimal) throw new IllegalArgumentException("\"IsTamable\" is true while \"isAnimal\" is false");
    }

    public EntityTemperatureDataMap createDefault() {return new EntityTemperatureDataMap(40, 10, false, false);}

}
