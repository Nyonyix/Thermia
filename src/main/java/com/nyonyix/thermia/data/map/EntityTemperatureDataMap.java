package com.nyonyix.thermia.data.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EntityTemperatureDataMap(
        float maxEntityTemperature,
        float minEntityTemperature,
        boolean isMob,
        boolean isTamed
)
{
    public static final Codec<EntityTemperatureDataMap> CODEC = RecordCodecBuilder.create(entityTemperatureDataMapInstance -> entityTemperatureDataMapInstance.group(
            Codec.FLOAT.fieldOf("max_entity_temperature").forGetter(EntityTemperatureDataMap::maxEntityTemperature),
            Codec.FLOAT.fieldOf("min_entity_temperature").forGetter(EntityTemperatureDataMap::minEntityTemperature),
            Codec.BOOL.fieldOf("is_mob").forGetter(EntityTemperatureDataMap::isMob),
            Codec.BOOL.fieldOf("is_tamed").forGetter(EntityTemperatureDataMap::isTamed)
    ).apply(entityTemperatureDataMapInstance, EntityTemperatureDataMap::new));

    public static final StreamCodec<ByteBuf, EntityTemperatureDataMap> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, EntityTemperatureDataMap::maxEntityTemperature,
            ByteBufCodecs.FLOAT, EntityTemperatureDataMap::minEntityTemperature,
            ByteBufCodecs.BOOL, EntityTemperatureDataMap::isMob,
            ByteBufCodecs.BOOL, EntityTemperatureDataMap::isTamed,
            EntityTemperatureDataMap::new
    );

    public static EntityTemperatureDataMap createDefault() {return new EntityTemperatureDataMap(40, 10, false, false);}

    public EntityTemperatureDataMap withMaxEntityTemperature(float maxEntityTemperature) {return new EntityTemperatureDataMap(maxEntityTemperature, this.minEntityTemperature, this.isMob, this.isTamed);}

    public EntityTemperatureDataMap withMinEntityTemperature(float minEntityTemperature) {return new EntityTemperatureDataMap(this.maxEntityTemperature, minEntityTemperature, this.isMob, this.isTamed);}

    public EntityTemperatureDataMap withIsMob(boolean isMob) {return new EntityTemperatureDataMap(this.maxEntityTemperature, this.minEntityTemperature, isMob, this.isTamed);}

    public EntityTemperatureDataMap withIsTamed(boolean isTamed) {return new EntityTemperatureDataMap(this.maxEntityTemperature, this.minEntityTemperature, this.isMob, isTamed);}
}
