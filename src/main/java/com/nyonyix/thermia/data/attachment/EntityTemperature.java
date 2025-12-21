package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.BlockSearchResult;
import com.nyonyix.thermia.data.ClosestSource;
import com.nyonyix.thermia.data.SolarShadeResult;
import com.nyonyix.thermia.util.BlockSearch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record EntityTemperature(
        float internalTemperature,
        float environmentTemperature,
        float environmentHumidity,
        float wetness,
        float sunAngle,
        float maxInternalTemperature,
        float minInternalTemperature,
        boolean isWet,
        boolean isUnderground,
        boolean toRemove,
        BlockSearchResult blockSearchResult,
        SolarShadeResult sunOcclusionPos
)
{
    public static final Codec<EntityTemperature> CODEC = RecordCodecBuilder.create(entityTemperatureInstance -> entityTemperatureInstance.group(
       Codec.FLOAT.fieldOf("internal_temperature").forGetter(EntityTemperature::internalTemperature),
       Codec.FLOAT.fieldOf("environment_temperature").forGetter(EntityTemperature::environmentTemperature),
       Codec.FLOAT.fieldOf("environment_humidity").forGetter(EntityTemperature::environmentHumidity),
       Codec.FLOAT.fieldOf("wetness").forGetter(EntityTemperature::wetness),
       Codec.FLOAT.fieldOf("sun_angle").forGetter(EntityTemperature::sunAngle),
       Codec.FLOAT.fieldOf("max_internal_temperature").forGetter(EntityTemperature::maxInternalTemperature),
       Codec.FLOAT.fieldOf("min_internal_temperature").forGetter(EntityTemperature::minInternalTemperature),
       Codec.BOOL.fieldOf("is_wet").forGetter(EntityTemperature::isWet),
       Codec.BOOL.fieldOf("is_underground").forGetter(EntityTemperature::isUnderground),
       Codec.BOOL.fieldOf("to_remove").forGetter(EntityTemperature::toRemove),
       BlockSearchResult.CODEC.fieldOf("block_search_result").forGetter(EntityTemperature::blockSearchResult),
       SolarShadeResult.CODEC.fieldOf("sun_occlusion_pos").forGetter(EntityTemperature::sunOcclusionPos)
    ).apply(entityTemperatureInstance, EntityTemperature::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityTemperature> STREAM_CODEC = StreamCodec.of(
            (buf, temp) ->
            {
                buf.writeFloat(temp.internalTemperature);
                buf.writeFloat(temp.environmentTemperature);
                buf.writeFloat(temp.environmentHumidity);
                buf.writeFloat(temp.wetness);
                buf.writeFloat(temp.sunAngle);
                buf.writeFloat(temp.maxInternalTemperature);
                buf.writeFloat(temp.minInternalTemperature);
                buf.writeBoolean(temp.isWet);
                buf.writeBoolean(temp.isUnderground);
                buf.writeBoolean(temp.toRemove);
                SolarShadeResult.STREAM_CODEC.encode(buf, temp.sunOcclusionPos);
            }, (buf) -> new EntityTemperature(
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    BlockSearchResult.createDefault(),
                    SolarShadeResult.STREAM_CODEC.decode(buf)
            )
    );

    public static EntityTemperature createDefault() {return new EntityTemperature(20f, 13f, 0.5f, 0f, 45f, 40, 0, false, false, false, BlockSearchResult.createDefault(), SolarShadeResult.createDefault());}

    public EntityTemperature withInternalTemperature(float internalTemperature) { return new EntityTemperature(internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.toRemove, this.blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withEnvironmentTemperature(float environmentTemperature) { return new EntityTemperature(this.internalTemperature, environmentTemperature, this.environmentHumidity, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.toRemove, this.blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withEnvironmentHumidity(float environmentHumidity) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, environmentHumidity, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.toRemove, this.blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withWetness(float wetness) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.toRemove, this.blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withSunAngle(float sunAngle) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.toRemove, this.blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withMaxInternalTemperature(float maxInternalTemperature) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.sunAngle, maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.toRemove, this.blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withMinInternalTemperature(float minInternalTemperature) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.sunAngle, this.maxInternalTemperature, minInternalTemperature, this.isWet, this.isUnderground, this.toRemove, this.blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withIsWet(boolean isWet) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, isWet, this.isUnderground, this.toRemove, this.blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withIsUnderground(boolean isUnderground) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, isUnderground, this.toRemove, this.blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withToRemove(boolean toRemove) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, toRemove, this.blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withBlockSearchResult(BlockSearchResult blockSearchResult) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.toRemove, blockSearchResult, this.sunOcclusionPos);}

    public EntityTemperature withSunOcclusionPos(SolarShadeResult sunOcclusionPos) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.sunAngle, this.maxInternalTemperature, this.minInternalTemperature, this.isWet, this.isUnderground, this.toRemove, blockSearchResult, sunOcclusionPos);}
}
