package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.ClosestSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record EntityTemperature(
        float internalTemperature,
        float environmentTemperature,
        float environmentHumidity,
        float environmentWetBulb,
        float wetness,
        float sunAngle,
        float maxInternalTemperature,
        float minInternalTemperature,
        boolean isWet,
        boolean isUnderground,
        ClosestSource closestSource
)
{
    public static final Codec<EntityTemperature> CODEC = RecordCodecBuilder.create(entityTemperatureInstance -> entityTemperatureInstance.group(
       Codec.FLOAT.fieldOf("internal_temperature").forGetter(EntityTemperature::internalTemperature),
       Codec.FLOAT.fieldOf("environment_temperature").forGetter(EntityTemperature::environmentTemperature),
       Codec.FLOAT.fieldOf("environment_humidity").forGetter(EntityTemperature::environmentHumidity),
       Codec.FLOAT.fieldOf("environment_wet_bulb").forGetter(EntityTemperature::environmentWetBulb),
       Codec.FLOAT.fieldOf("wetness").forGetter(EntityTemperature::wetness),
       Codec.FLOAT.fieldOf("sun_angle").forGetter(EntityTemperature::sunAngle),
       Codec.FLOAT.fieldOf("max_internal_temperature").forGetter(EntityTemperature::maxInternalTemperature),
       Codec.FLOAT.fieldOf("min_internal_temperature").forGetter(EntityTemperature::minInternalTemperature),
       Codec.BOOL.fieldOf("is_wet").forGetter(EntityTemperature::isWet),
       Codec.BOOL.fieldOf("is_underground").forGetter(EntityTemperature::isUnderground),
       ClosestSource.CODEC.fieldOf("closest_source").forGetter(EntityTemperature::closestSource)
    ).apply(entityTemperatureInstance, EntityTemperature::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityTemperature> STREAM_CODEC = StreamCodec.of(
            (buf, temp) ->
            {
                buf.writeFloat(temp.internalTemperature);
                buf.writeFloat(temp.environmentTemperature);
                buf.writeFloat(temp.environmentHumidity);
                buf.writeFloat(temp.environmentWetBulb);
                buf.writeFloat(temp.wetness);
                buf.writeFloat(temp.sunAngle);
                buf.writeFloat(temp.maxInternalTemperature);
                buf.writeFloat(temp.minInternalTemperature);
                buf.writeBoolean(temp.isWet);
                buf.writeBoolean(temp.isUnderground);
                ClosestSource.STREAM_CODEC.encode(buf, temp.closestSource);
            }, (buf) -> new EntityTemperature(
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    ClosestSource.STREAM_CODEC.decode(buf)
            )
    );

    public static EntityTemperature createDefault() {return new EntityTemperature(20f, 13f, 0.5f, 10f, 0f, 45f, 40, 0, false, false, ClosestSource.createDefault());}

    public EntityTemperature withInternalTemperature(float internalTemperature) { return new EntityTemperature(internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withEnvironmentTemperature(float environmentTemperature) { return new EntityTemperature(this.internalTemperature, environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withEnvironmentHumidity(float environmentHumidity) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, environmentHumidity, this.environmentWetBulb, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withEnvironmentWetBulb(float environmentWetBulb) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, environmentWetBulb, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withWetness(float wetness) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withSunAngle(float sunAngle) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withMaxInternalTemperature(float maxInternalTemperature) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.sunAngle, maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withMinInternalTemperature(float minInternalTemperature) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.sunAngle, this.maxInternalTemperature, minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withIsWet(boolean isWet) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withIsUnderground(boolean isUnderground) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, isUnderground, this.closestSource);}

    public EntityTemperature withClosestSource(ClosestSource closestSource) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, closestSource);}
}
