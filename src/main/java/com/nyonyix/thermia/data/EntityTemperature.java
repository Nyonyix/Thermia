package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.util.ClosestSource;

public record EntityTemperature(
        float internalTemperature,
        float environmentTemperature,
        float environmentHumidity,
        float environmentWetBulb,
        float wetness,
        int maxInternalTemperature,
        int minInternalTemperature,
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
       Codec.INT.fieldOf("max_internal_temperature").forGetter(EntityTemperature::maxInternalTemperature),
       Codec.INT.fieldOf("min_internal_temperature").forGetter(EntityTemperature::minInternalTemperature),
       Codec.BOOL.fieldOf("is_wet").forGetter(EntityTemperature::isWet),
       Codec.BOOL.fieldOf("is_underground").forGetter(EntityTemperature::isUnderground),
       ClosestSource.CODEC.fieldOf("closest_source").forGetter(EntityTemperature::closestSource)
    ).apply(entityTemperatureInstance, EntityTemperature::new));

    public static EntityTemperature createDefault() {return new EntityTemperature(37f, 13f, 0.5f, 10f, 0f, 40, 0, false, false, ClosestSource.createDefault());}

    public EntityTemperature withInternalTemperature(float internalTemperature) { return new EntityTemperature(internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withEnvironmentTemperature(float environmentTemperature) { return new EntityTemperature(this.internalTemperature, environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withEnvironmentHumidity(float environmentHumidity) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, environmentHumidity, this.environmentWetBulb, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withEnvironmentWetBulb(float environmentWetBulb) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, environmentWetBulb, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withWetness(float wetness) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, wetness, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withMaxInternalTemperature(int maxInternalTemperature) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withMinInternalTemperature(int minInternalTemperature) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.maxInternalTemperature, minInternalTemperature, this.isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withIsWet(boolean isWet) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, isWet, this.isUnderground, this.closestSource);}

    public EntityTemperature withIsUnderground(boolean isUnderground) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, isUnderground, this.closestSource);}

    public EntityTemperature withClosestSource(ClosestSource closestSource) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.environmentWetBulb, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, closestSource);}
}
