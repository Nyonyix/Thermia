package com.nyonyix.thermia.data.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.compat.BlockEntityFluidCompat;
import com.nyonyix.thermia.compat.create.CreateHeatCompat;
import com.nyonyix.thermia.compat.powergrid.PowerGridCompat;
import net.dries007.tfc.common.blockentities.CharcoalForgeBlockEntity;
import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.blockentities.PitKilnBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

public record BlockTemperatureDataMap(
        float temperature,
        int searchCap,
        boolean hasTFCHeat,
        boolean isRadiative,
        Map<String, Float> stateTemps
)
{
    private static final Codec<Map<String, Float>> STATE_TEMPS = Codec.unboundedMap(Codec.STRING, Codec.FLOAT);

    public static final Codec<BlockTemperatureDataMap> CODEC = RecordCodecBuilder.create(blockTemperatureDataMapInstance -> blockTemperatureDataMapInstance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(BlockTemperatureDataMap::temperature),
            Codec.INT.fieldOf("search_cap").forGetter(BlockTemperatureDataMap::searchCap),
            Codec.BOOL.fieldOf("has_tfc_heat").forGetter(BlockTemperatureDataMap::hasTFCHeat),
            Codec.BOOL.fieldOf("is_radiative").forGetter(BlockTemperatureDataMap::isRadiative),
            STATE_TEMPS.optionalFieldOf("state_temps", Map.of()).forGetter(BlockTemperatureDataMap::stateTemps)
    ).apply(blockTemperatureDataMapInstance, BlockTemperatureDataMap::new));

    public static BlockTemperatureDataMap createDefault() {return new BlockTemperatureDataMap(256f, 32, false, true, Map.of());}

    public float resolveForState(Level level, BlockPos pos)
    {
        float temperature = this.temperature;
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof IHeatable heatable) return heatable.getTemperature();
        if (blockEntity instanceof CharcoalForgeBlockEntity charcoalForge) return charcoalForge.getTemperature();
        if (blockEntity instanceof PitKilnBlockEntity pitKiln) return pitKiln.isLit() ? this.temperature() : 0.0f;

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

        BlockState state = level.getBlockState(pos);

        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet())
        {
            String key = entry.getKey().getName() + "=" + entry.getValue().toString();

            if (this.stateTemps.containsKey(key)) temperature = Math.max(temperature, this.stateTemps.get(key));
        }

        return temperature;
    }
}