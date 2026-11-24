package com.nyonyix.thermia.registry.builtins;

import com.nyonyix.thermia.api.sources.BlockTemperatures;
import net.dries007.tfc.common.blockentities.CharcoalForgeBlockEntity;
import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.blockentities.PitKilnBlockEntity;

public final class BlockTemperatureBuiltins
{
    public static void register()
    {
        BlockTemperatures.register(IHeatable.class, (level, pos, blockEntity, dataMap) -> ((IHeatable) blockEntity).getTemperature());
        BlockTemperatures.register(CharcoalForgeBlockEntity.class, (level, pos, blockEntity, dataMap) -> ((CharcoalForgeBlockEntity) blockEntity).getTemperature());
        BlockTemperatures.register(PitKilnBlockEntity.class, (level, pos, blockEntity, dataMap) -> ((PitKilnBlockEntity) blockEntity).isLit() ? dataMap.temperature() : 0f);
    }
}
