package com.nyonyix.thermia.compat.powergrid;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.patryk3211.powergrid.electricity.base.ThermalBehaviour;

import javax.annotation.Nullable;

public final class PowerGridCompat
{
    public static final String MOD_ID = "powergrid";
    private static final boolean IS_LOADED = ModList.get().isLoaded(MOD_ID);

    @Nullable
    public static Float resolveTemeprature(Level level, BlockPos pos)
    {
        if (!IS_LOADED) return null;

        ThermalBehaviour thermal = BlockEntityBehaviour.get(level, pos, ThermalBehaviour.TYPE);
        if (thermal == null) return null;

        return Math.max(thermal.getTemperature() - ThermalBehaviour.BASE_TEMPERATURE, 0.0f);
    }
}
