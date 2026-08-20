package com.nyonyix.thermia.data.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;

public record BlockSealDataMap(Map<String, Float> statePorosity, float defaultPorosity)
{
    private static final Codec<Map<String, Float>> STATE_POROSITY_CODEC = Codec.unboundedMap(Codec.STRING, Codec.FLOAT);

    public static final Codec<BlockSealDataMap> CODEC = RecordCodecBuilder.create(blockPorosityDataMapInstance -> blockPorosityDataMapInstance.group(
            STATE_POROSITY_CODEC.optionalFieldOf("state_porosity", Map.of()).forGetter(BlockSealDataMap::statePorosity),
            Codec.FLOAT.fieldOf("default_porosity").forGetter(BlockSealDataMap::defaultPorosity)
    ).apply(blockPorosityDataMapInstance, BlockSealDataMap::new));

    public static final StreamCodec<ByteBuf, BlockSealDataMap> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.FLOAT), BlockSealDataMap::statePorosity,
            ByteBufCodecs.FLOAT, BlockSealDataMap::defaultPorosity,
            BlockSealDataMap::new);

    public static BlockSealDataMap createDefault() {return new BlockSealDataMap(Map.of(), 1f);}

    public float resolveForState(BlockState state)
    {
        float porosity = this.defaultPorosity;
        int matchCount = 0;
        float multiplier = 0.1f;

        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet())
        {
            String key = entry.getKey().getName() + "=" + entry.getValue().toString();

            if (this.statePorosity.containsKey(key))
            {
                porosity = Math.min(porosity, this.statePorosity.get(key));
                matchCount++;
            }
        }

        if (matchCount > 1) porosity *= (1f + (matchCount - 1) * multiplier);

        return Math.min(porosity, 1f);
    }
}
