package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.records.ClientInterior;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public record ClientInteriorAttachment(Map<BlockPos, ClientInterior> activeClientInteriors)
{
    public static final Codec<ClientInteriorAttachment> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING.xmap(
                    str -> BlockPos.of(Long.parseLong(str)),
                    pos -> String.valueOf(pos.asLong())
            ), ClientInterior.CODEC).fieldOf("client_interior").forGetter(ClientInteriorAttachment::activeClientInteriors)
    ).apply(i, ClientInteriorAttachment::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientInteriorAttachment> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static ClientInteriorAttachment createDefault() {return new ClientInteriorAttachment(Map.of());}
}
