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
        float maxInternalTemperature,
        float minInternalTemperature,
        boolean toRemove,
        BlockSearchResult blockSearchResult
)
{
    public static final Codec<EntityTemperature> CODEC = RecordCodecBuilder.create(entityTemperatureInstance -> entityTemperatureInstance.group(
       Codec.FLOAT.fieldOf("internal_temperature").forGetter(EntityTemperature::internalTemperature),
       Codec.FLOAT.fieldOf("environment_temperature").forGetter(EntityTemperature::environmentTemperature),
       Codec.FLOAT.fieldOf("environment_humidity").forGetter(EntityTemperature::environmentHumidity),
       Codec.FLOAT.fieldOf("wetness").forGetter(EntityTemperature::wetness),
       Codec.FLOAT.fieldOf("max_internal_temperature").forGetter(EntityTemperature::maxInternalTemperature),
       Codec.FLOAT.fieldOf("min_internal_temperature").forGetter(EntityTemperature::minInternalTemperature),
       Codec.BOOL.fieldOf("to_remove").forGetter(EntityTemperature::toRemove),
       BlockSearchResult.CODEC.fieldOf("block_search_result").forGetter(EntityTemperature::blockSearchResult)
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
                buf.writeBoolean(temp.toRemove);
            }, (buf) -> new EntityTemperature(
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readBoolean(),
                    BlockSearchResult.createDefault()
            )
    );

    public static EntityTemperature createDefault() {return new EntityTemperature(20f, 13f, 0.5f, 0f, 40, 0, false, BlockSearchResult.createDefault());}

    public EntityTemperature withInternalTemperature(float internalTemperature) {return new EntityTemperature(internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.toRemove, this.blockSearchResult);}

    public EntityTemperature withEnvironmentTemperature(float environmentTemperature) {return new EntityTemperature(this.internalTemperature, environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.toRemove, this.blockSearchResult);}

    public EntityTemperature withEnvironmentHumidity(float environmentHumidity) {return new EntityTemperature(this.internalTemperature, this.environmentTemperature, environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, this.toRemove, this.blockSearchResult);}

    public EntityTemperature withWetness(float wetness) {return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, wetness, this.maxInternalTemperature, this.minInternalTemperature, this.toRemove, this.blockSearchResult);}

    public EntityTemperature withMaxInternalTemperature(float maxInternalTemperature) {return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, maxInternalTemperature, this.minInternalTemperature, this.toRemove, this.blockSearchResult);}

    public EntityTemperature withMinInternalTemperature(float minInternalTemperature) {return  new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, minInternalTemperature, this.toRemove, this.blockSearchResult);}

    public EntityTemperature withToRemove(boolean toRemove) {return new EntityTemperature(this.internalTemperature, this.environmentTemperature, this.environmentHumidity, this.wetness, this.maxInternalTemperature, this.minInternalTemperature, toRemove, this.blockSearchResult);}
}
