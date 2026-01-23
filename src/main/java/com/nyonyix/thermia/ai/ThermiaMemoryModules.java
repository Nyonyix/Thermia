package com.nyonyix.thermia.ai;

import com.mojang.serialization.Codec;
import com.nyonyix.thermia.Thermia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class ThermiaMemoryModules
{
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(BuiltInRegistries.MEMORY_MODULE_TYPE, Thermia.MODID);

    public static final Supplier<MemoryModuleType<BlockPos>> WARMEST_SPOT_BLOCK_POS = MEMORY_TYPES.register("warmest_spot_block_pos", () -> new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));

    public static final Supplier<MemoryModuleType<Float>> WARMEST_SPOT_TEMPERATURE = MEMORY_TYPES.register("warmest_spot_temperature", () -> new MemoryModuleType<>(Optional.of(Codec.FLOAT)));

    public static final Supplier<MemoryModuleType<Long>> WARMEST_SPOT_TIME  = MEMORY_TYPES.register("warmest_spot_time", () -> new MemoryModuleType<>(Optional.of(Codec.LONG)));

    public static final Supplier<MemoryModuleType<BlockPos>> COOLEST_SPOT_BLOCK_POS = MEMORY_TYPES.register("coolest_spot_block_pos", () -> new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));

    public static final Supplier<MemoryModuleType<Float>> COOLEST_SPOT_TEMPERATURE = MEMORY_TYPES.register("coolest_spot_temperature", () -> new MemoryModuleType<>(Optional.of(Codec.FLOAT)));

    public static final Supplier<MemoryModuleType<Long>> COOLEST_SPOT_TIME  = MEMORY_TYPES.register("coolest_spot_time", () -> new MemoryModuleType<>(Optional.of(Codec.LONG)));

    public static final Supplier<MemoryModuleType<Boolean>> IS_SEEKING_WARM = MEMORY_TYPES.register("is_seeking_warm", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
}
