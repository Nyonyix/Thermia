package com.nyonyix.thermia.data.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ItemInsulationDataMap(
        float insulationModifier
)
{
    public static final Codec<ItemInsulationDataMap> CODEC = RecordCodecBuilder.create(itemInsulationInstance -> itemInsulationInstance.group(
            Codec.FLOAT.fieldOf("insulation_modifier").forGetter(ItemInsulationDataMap::insulationModifier)
    ).apply(itemInsulationInstance, ItemInsulationDataMap::new));

    public static final StreamCodec<ByteBuf, ItemInsulationDataMap> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ItemInsulationDataMap::insulationModifier,
            ItemInsulationDataMap::new
    );

    public static ItemInsulationDataMap createDefault() {return new ItemInsulationDataMap(1f);}

    public ItemInsulationDataMap withInsulationModifier(float insulationModifier) {return new ItemInsulationDataMap(insulationModifier);}
}
