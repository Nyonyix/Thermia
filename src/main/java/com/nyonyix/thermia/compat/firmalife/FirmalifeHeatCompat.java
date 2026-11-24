package com.nyonyix.thermia.compat.firmalife;

import com.eerussianguy.firmalife.common.blockentities.OvenLike;
import com.nyonyix.thermia.api.sources.BlockTemperatures;
import net.neoforged.fml.ModList;

public final class FirmalifeHeatCompat
{
    public static final String MOD_ID = "firmalife";

    public static void register()
    {
        if (ModList.get().isLoaded(MOD_ID))
        {
            BlockTemperatures.register(OvenLike.class, (level, pos, blockEntity, dataMap) -> ((OvenLike) blockEntity).getTemperature());
        }
    }
}
