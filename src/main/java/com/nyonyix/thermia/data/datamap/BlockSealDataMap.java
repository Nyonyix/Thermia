package com.nyonyix.thermia.data.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.records.Rule;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;

public record BlockSealDataMap(List<Rule> statePorosity, float defaultPorosity)
{
    private static Set<String> getStateKeys(BlockState state)
    {
        Set<String> keys = new HashSet<>();

        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet())
        {
            String key = entry.getKey().getName() + "=" + getValueName(entry.getKey(), entry.getValue());
            keys.add(key);
        }

        return keys;
    }

    private static String getValueName(Property<?> property, Comparable<?> value)
    {
        return ((Property) property).getName(value);
    }

    public static final Codec<BlockSealDataMap> CODEC = RecordCodecBuilder.create(i -> i.group(
            Rule.CODEC.listOf().optionalFieldOf("state_block_seal", List.<Rule>of()).forGetter(BlockSealDataMap::statePorosity),
            Codec.FLOAT.fieldOf("default_block_seal").forGetter(BlockSealDataMap::defaultPorosity)
    ).apply(i, BlockSealDataMap::new));

    public static final StreamCodec<ByteBuf, BlockSealDataMap> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static BlockSealDataMap createDefault() {return new BlockSealDataMap(List.of(), 1f);}

    public float resolveForState(BlockState state)
    {
        float porosity = this.defaultPorosity;
        int matchCount = 0;
        float multiplier = 0.1f;

        Set<String> stateKeys = getStateKeys(state);

        for (Rule rule : this.statePorosity)
        {
            float entryPorosity = rule.value();
            List<String> conditions = rule.when();

            if (!conditions.isEmpty() && stateKeys.containsAll(conditions))
            {
                porosity = Math.min(porosity, entryPorosity);
                matchCount++;
            }
        }

        if (matchCount > 1)
        {
            porosity *= 1.0f + (matchCount - 1) * multiplier;
        }

        return Math.min(porosity, 1.0f);
    }
}
