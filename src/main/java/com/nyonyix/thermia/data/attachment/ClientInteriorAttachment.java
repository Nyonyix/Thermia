package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.records.SyncedInterior;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public record ClientInteriorAttachment(Map<BlockPos, SyncedInterior> activeInteriors)
{
    public static final Codec<ClientInteriorAttachment> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING.xmap(
                    str -> BlockPos.of(Long.parseLong(str)),
                    pos -> String.valueOf(pos.asLong())
            ), SyncedInterior.CODEC).fieldOf("active_interiors").forGetter(ClientInteriorAttachment::activeInteriors)
    ).apply(i, ClientInteriorAttachment::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientInteriorAttachment> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
