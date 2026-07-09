package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.records.BlockSearchResult;
import com.nyonyix.thermia.data.records.SolarShadeResult;
import com.nyonyix.thermia.data.records.WindOcclusionResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record EntityTemperature(
        float internalTemperature,
        float environmentTemperature,
        float environmentHumidity,
        float wetness,
        float maxInternalTemperature,
        float minInternalTemperature,
        float acclimatization,
        boolean toRemove,
        BlockSearchResult blockSearchResult,
        SolarShadeResult solarShadeResult,
        WindOcclusionResult windOcclusionResult
)
{
    public static final Codec<EntityTemperature> CODEC = RecordCodecBuilder.create(entityTemperatureInstance -> entityTemperatureInstance.group(
        Codec.FLOAT.fieldOf("internal_temperature").forGetter(EntityTemperature::internalTemperature),
        Codec.FLOAT.fieldOf("environment_temperature").forGetter(EntityTemperature::environmentTemperature),
        Codec.FLOAT.fieldOf("environment_humidity").forGetter(EntityTemperature::environmentHumidity),
        Codec.FLOAT.fieldOf("wetness").forGetter(EntityTemperature::wetness),
        Codec.FLOAT.fieldOf("max_internal_temperature").forGetter(EntityTemperature::maxInternalTemperature),
        Codec.FLOAT.fieldOf("min_internal_temperature").forGetter(EntityTemperature::minInternalTemperature),
        Codec.FLOAT.fieldOf("acclimatization").forGetter(EntityTemperature::acclimatization),
        Codec.BOOL.fieldOf("to_remove").forGetter(EntityTemperature::toRemove),
        BlockSearchResult.CODEC.fieldOf("block_search_result").forGetter(EntityTemperature::blockSearchResult),
        SolarShadeResult.CODEC.fieldOf("solar_shade_result").forGetter(EntityTemperature::solarShadeResult),
        WindOcclusionResult.CODEC.fieldOf("wind_occlusion_result").forGetter(EntityTemperature::windOcclusionResult)
    ).apply(entityTemperatureInstance, EntityTemperature::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityTemperature> STREAM_CODEC = StreamCodec.of(
            (buf, temp) ->
            {
                buf.writeFloat(temp.internalTemperature);
                buf.writeFloat(temp.environmentTemperature);
                buf.writeFloat(temp.environmentHumidity);
                buf.writeFloat(temp.wetness);
                buf.writeFloat(temp.maxInternalTemperature);
                buf.writeFloat(temp.minInternalTemperature);
                buf.writeFloat(temp.acclimatization);
                buf.writeBoolean(temp.toRemove);
                SolarShadeResult.STREAM_CODEC.encode(buf, temp.solarShadeResult);
                WindOcclusionResult.STREAM_CODEC.encode(buf, temp.windOcclusionResult);
            }, (buf) -> new EntityTemperature(
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readBoolean(),
                    BlockSearchResult.createDefault(),
                    SolarShadeResult.STREAM_CODEC.decode(buf),
                    WindOcclusionResult.STREAM_CODEC.decode(buf)
            )
    );

    public static EntityTemperature createDefault() {return new EntityTemperature(20f, 13f, 0.5f, 0f, 40, 0, 0,false, BlockSearchResult.createDefault(), SolarShadeResult.createDefault(), WindOcclusionResult.createDefault());}

    public EntityTemperature withInternalTemperature(float internalTemperature) {return new EntityTemperature(internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.acclimatization, this.toRemove, this.blockSearchResult, this.solarShadeResult, this.windOcclusionResult);}

    public EntityTemperature withEnvironmentTemperature(float environmentTemperature) {return new EntityTemperature(this.internalTemperature, environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.acclimatization, this.toRemove, this.blockSearchResult, this.solarShadeResult, this.windOcclusionResult);}

    public EntityTemperature withEnvironmentHumidity(float environmentHumidity) {return new EntityTemperature(this.internalTemperature, this.environmentTemperature, environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.acclimatization, this.toRemove, this.blockSearchResult, this.solarShadeResult, this.windOcclusionResult);}

    public EntityTemperature withWetness(float wetness) {return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, wetness, this.maxInternalTemperature, this.minInternalTemperature, this.acclimatization, this.toRemove, this.blockSearchResult, this.solarShadeResult, this.windOcclusionResult);}

    public EntityTemperature withMaxInternalTemperature(float maxInternalTemperature) {return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, maxInternalTemperature, this.minInternalTemperature, this.acclimatization, this.toRemove, this.blockSearchResult, this.solarShadeResult, this.windOcclusionResult);}

    public EntityTemperature withMinInternalTemperature(float minInternalTemperature) {return  new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, minInternalTemperature, this.acclimatization, this.toRemove, this.blockSearchResult, this.solarShadeResult, this.windOcclusionResult);}

    public EntityTemperature withAcclimatization(float acclimatization) {return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, acclimatization, this.toRemove, this.blockSearchResult, this.solarShadeResult, this.windOcclusionResult);}

    public EntityTemperature withToRemove(boolean toRemove) {return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.acclimatization, toRemove, this.blockSearchResult, this.solarShadeResult, this.windOcclusionResult);}

    public EntityTemperature withBlockSearchResult(BlockSearchResult blockSearchResult) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.acclimatization, this.toRemove, blockSearchResult, this.solarShadeResult, this.windOcclusionResult);}

    public EntityTemperature withSolarShadeResult(SolarShadeResult solarShadeResult) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.acclimatization, this.toRemove, this.blockSearchResult, solarShadeResult, this.windOcclusionResult);}

    public EntityTemperature withWindOcclusionResult(WindOcclusionResult windOcclusionResult) { return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.acclimatization, this.toRemove, this.blockSearchResult, this.solarShadeResult, windOcclusionResult);}
}
