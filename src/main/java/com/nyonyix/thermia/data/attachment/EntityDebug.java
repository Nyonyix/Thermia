package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.BlockSearchResult;
import com.nyonyix.thermia.data.SolarShadeResult;
import com.nyonyix.thermia.data.WindOcclusionResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.stream.Stream;

public record EntityDebug(
        SolarShadeResult solarShadeResult,
        BlockSearchResult blockSearchResult,
        WindOcclusionResult windOcclusionResult
)
{
    public static final Codec<EntityDebug> CODEC = RecordCodecBuilder.create(windOcclusionResultInstance -> windOcclusionResultInstance.group(
            SolarShadeResult.CODEC.fieldOf("solar_shade_result").forGetter(EntityDebug::solarShadeResult),
            BlockSearchResult.CODEC.fieldOf("block_search_result").forGetter(EntityDebug::blockSearchResult),
            WindOcclusionResult.CODEC.fieldOf("wind_occlusion_result").forGetter(EntityDebug::windOcclusionResult)
    ).apply(windOcclusionResultInstance, EntityDebug::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityDebug> STREAM_CODEC = StreamCodec.composite(
            SolarShadeResult.STREAM_CODEC, EntityDebug::solarShadeResult,
            BlockSearchResult.STREAM_CODEC, EntityDebug::blockSearchResult,
            WindOcclusionResult.STREAM_CODEC, EntityDebug::windOcclusionResult,
            EntityDebug::new
    );

    public static EntityDebug createDefault() {return new EntityDebug(SolarShadeResult.createDefault(), BlockSearchResult.createDefault(), WindOcclusionResult.createDefault());}

    public EntityDebug withSolarShadeResult(SolarShadeResult solarShadeResult) {return new EntityDebug(solarShadeResult, this.blockSearchResult, this.windOcclusionResult);}

    public EntityDebug withBlockSearchResult(BlockSearchResult blockSearchResult) {return new EntityDebug(this.solarShadeResult, blockSearchResult, this.windOcclusionResult);}

    public EntityDebug withWindOcclusionResult(WindOcclusionResult windOcclusionResult) {return new EntityDebug(this.solarShadeResult, this.blockSearchResult, windOcclusionResult);}
}
