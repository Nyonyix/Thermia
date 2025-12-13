package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ItemInsulation(
        float insulationModifier
)
{
    public static final Codec<ItemInsulation> CODEC = RecordCodecBuilder.create(itemInsulationInstance -> itemInsulationInstance.group(
            Codec.FLOAT.fieldOf("insulation_modifier").forGetter(ItemInsulation::insulationModifier)
    ).apply(itemInsulationInstance, ItemInsulation::new));

    public static final StreamCodec<ByteBuf, ItemInsulation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ItemInsulation::insulationModifier,
            ItemInsulation::new
    );

    public static ItemInsulation createDefault() {return new ItemInsulation(1f);}

    public ItemInsulation withInsulationModifier(float insulationModifier) {return new ItemInsulation(insulationModifier);}
}
