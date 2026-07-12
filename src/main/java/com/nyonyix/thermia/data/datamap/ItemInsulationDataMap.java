package com.nyonyix.thermia.data.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ItemInsulationDataMap(
        float conductionProtection,
        float radiationProtection,
        float convectionProtection,
        float rainProtection
)
{
    public static final Codec<ItemInsulationDataMap> CODEC = RecordCodecBuilder.create(itemInsulationInstance -> itemInsulationInstance.group(
            Codec.FLOAT.fieldOf("conduction_protection").forGetter(ItemInsulationDataMap::conductionProtection),
            Codec.FLOAT.fieldOf("radiation_protection").forGetter(ItemInsulationDataMap::radiationProtection),
            Codec.FLOAT.fieldOf("convection_protection").forGetter(ItemInsulationDataMap::convectionProtection),
            Codec.FLOAT.fieldOf("rain_protection").forGetter(ItemInsulationDataMap::rainProtection)
    ).apply(itemInsulationInstance, ItemInsulationDataMap::new));

    public static final StreamCodec<ByteBuf, ItemInsulationDataMap> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ItemInsulationDataMap::conductionProtection,
            ByteBufCodecs.FLOAT, ItemInsulationDataMap::radiationProtection,
            ByteBufCodecs.FLOAT, ItemInsulationDataMap::convectionProtection,
            ByteBufCodecs.FLOAT, ItemInsulationDataMap::rainProtection,
            ItemInsulationDataMap::new
    );

    public static ItemInsulationDataMap createDefault() {return new ItemInsulationDataMap(1f, 1f, 1f, 1f);}
}
