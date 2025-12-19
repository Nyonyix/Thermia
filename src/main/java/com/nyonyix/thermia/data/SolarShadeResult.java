package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SolarShadeResult(float shade, BlockPos sunOcclusionPos)
{
    public static final Codec<SolarShadeResult> CODEC = RecordCodecBuilder.create(solarShadeResultinstance -> solarShadeResultinstance.group(
            Codec.FLOAT.fieldOf("shade").forGetter(SolarShadeResult::shade),
            BlockPos.CODEC.fieldOf("sun_occlusion_pos").forGetter(SolarShadeResult::sunOcclusionPos)
    ).apply(solarShadeResultinstance, SolarShadeResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SolarShadeResult> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SolarShadeResult::shade,
            BlockPos.STREAM_CODEC, SolarShadeResult::sunOcclusionPos,
            SolarShadeResult::new
    );

    public static SolarShadeResult createDefault() {return new SolarShadeResult(1.0f, BlockPos.ZERO);}

    public SolarShadeResult withShade(float shade) {return new SolarShadeResult(shade, this.sunOcclusionPos);}

    public SolarShadeResult withSunOcclusionPos(BlockPos pos) {return new SolarShadeResult(this.shade, pos);}
}
