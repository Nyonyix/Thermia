package com.nyonyix.thermia.ai.behaviours;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ai.memories.ThermiaMemoryModules;
import com.nyonyix.thermia.util.AiHelpers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

public class MoveToSeekComfort extends Behavior<PathfinderMob>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final float speedModifier;

    public MoveToSeekComfort(float speedModifier)
    {
        super(Map.of(ThermiaMemoryModules.IS_SEEKING_WARM.get(), MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT), 200);
        this.speedModifier = speedModifier;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob)
    {
        Optional<WalkTarget> target = mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        if (target.isEmpty()) return false;

        return !AiHelpers.isAtPosition(mob, target.get().getTarget().currentBlockPosition());
    }


}
