package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.records.SyncedInteriorData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public record SyncedInteriorTemperatureAttachment(Map<BlockPos, SyncedInteriorData> activeInteriorTemperatures)
{
    public static final Codec<SyncedInteriorTemperatureAttachment> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING.xmap(
                    str -> BlockPos.of(Long.parseLong(str)),
                    pos -> String.valueOf(pos.asLong())
            ), SyncedInteriorData.CODEC).fieldOf("active_interior_temperatures").forGetter(SyncedInteriorTemperatureAttachment::activeInteriorTemperatures)
    ).apply(i, SyncedInteriorTemperatureAttachment::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncedInteriorTemperatureAttachment> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static SyncedInteriorTemperatureAttachment createDefault() {return new SyncedInteriorTemperatureAttachment(Map.of());}
}
