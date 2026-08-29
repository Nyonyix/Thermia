package com.nyonyix.thermia.data.datamap;

import com.eerussianguy.firmalife.common.blockentities.OvenLike;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.compat.BlockEntityFluidCompat;
import com.nyonyix.thermia.compat.create.CreateHeatCompat;
import com.nyonyix.thermia.compat.powergrid.PowerGridCompat;
import com.nyonyix.thermia.data.records.Rule;
import net.dries007.tfc.common.blockentities.CharcoalForgeBlockEntity;
import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.blockentities.PitKilnBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record BlockTemperatureDataMap(
        float temperature,
        int searchCap,
        boolean hasTFCHeat,
        boolean isRadiative,
        List<Rule> stateTemps
)
{
    private static final Codec<Map<String, Float>> STATE_TEMPS = Codec.unboundedMap(Codec.STRING, Codec.FLOAT);

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

    public static final Codec<BlockTemperatureDataMap> CODEC = RecordCodecBuilder.create(blockTemperatureDataMapInstance -> blockTemperatureDataMapInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(BlockTemperatureDataMap::temperature),
            Codec.INT.fieldOf("search_cap").forGetter(BlockTemperatureDataMap::searchCap),
            Codec.BOOL.fieldOf("has_tfc_heat").forGetter(BlockTemperatureDataMap::hasTFCHeat),
            Codec.BOOL.fieldOf("is_radiative").forGetter(BlockTemperatureDataMap::isRadiative),
            Rule.CODEC.listOf().optionalFieldOf("state_temps", List.<Rule>of()).forGetter(BlockTemperatureDataMap::stateTemps)
    ).apply(blockTemperatureDataMapInstance, BlockTemperatureDataMap::new));

    public static BlockTemperatureDataMap createDefault() {return new BlockTemperatureDataMap(256f, 32, false, true, List.of());}

    public float resolveForState(Level level, BlockPos pos)
    {
        float temperature = this.temperature;
        var blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof IHeatable heatable) return heatable.getTemperature();
        if (blockEntity instanceof CharcoalForgeBlockEntity charcoalForge) return charcoalForge.getTemperature();
        if (blockEntity instanceof PitKilnBlockEntity pitKiln) return pitKiln.isLit() ? this.temperature() : 0.0f;
        if (blockEntity instanceof OvenLike oven) return oven.getTemperature();

        if (blockEntity != null)
        {
            Float pgTemp = PowerGridCompat.resolveTemeprature(level, pos);
            if (pgTemp != null) return pgTemp;

            Float boilerTemp = CreateHeatCompat.resolveBoilerTemperature(level, pos);
            if (boilerTemp != null) return boilerTemp;

            Float pipeTemp = CreateHeatCompat.resolvePipeTemperature(level, pos);
            if (pipeTemp != null) return pipeTemp;

            Float tankTemp = BlockEntityFluidCompat.resolveTemperature(level, pos);
            if (tankTemp != null) return tankTemp;
        }

        int matchCount = 0;
        float multiplier = 0.1f;

        Set<String> stateKeys = getStateKeys(level.getBlockState(pos));

        for (Rule rule : this.stateTemps)
        {
            float ruleTemperature = rule.value();
            List<String> conditions = rule.when();

            if (!conditions.isEmpty() && stateKeys.containsAll(conditions))
            {
                temperature = Math.max(temperature, ruleTemperature);
                matchCount++;
            }
        }

        if (matchCount > 1)
        {
            temperature *= 1.0f + (matchCount - 1) * multiplier;
        }

        return temperature;
    }
}