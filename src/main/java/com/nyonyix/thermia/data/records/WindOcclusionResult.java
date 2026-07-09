package com.nyonyix.thermia.data.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WindOcclusionResult(BlockPos occludingBlock, float occlusionMultiplier)
{
    public static final Codec<WindOcclusionResult> CODEC = RecordCodecBuilder.create(windOcclusionResultInstance -> windOcclusionResultInstance.group(
            BlockPos.CODEC.fieldOf("occluding_block").forGetter(WindOcclusionResult::occludingBlock),
            Codec.FLOAT.fieldOf("occlusion_multiplier").forGetter(WindOcclusionResult::occlusionMultiplier)
    ).apply(windOcclusionResultInstance, WindOcclusionResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WindOcclusionResult> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, WindOcclusionResult::occludingBlock,
            ByteBufCodecs.FLOAT, WindOcclusionResult::occlusionMultiplier,
            WindOcclusionResult::new
    );

    public static WindOcclusionResult createDefault() {return new WindOcclusionResult(BlockPos.ZERO, 1.0f);}

    public WindOcclusionResult withOccludingBlock(BlockPos pos) {return new WindOcclusionResult(pos, this.occlusionMultiplier);}

    public WindOcclusionResult withOcclusionMultiplier(float multi) {return new WindOcclusionResult(this.occludingBlock, multi);}
}
