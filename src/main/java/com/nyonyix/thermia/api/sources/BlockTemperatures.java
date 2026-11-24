package com.nyonyix.thermia.api.sources;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.data.datamap.BlockTemperatureDataMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

/**
 * Dynamic block temperature providers.
 *
 * <p>Resolution order is block entity, then block, then the data map value.
 * Within each stage, keys are matched by exact class, then superclasses
 * nearest-first, then implemented interfaces breadth-first. A class that
 * implements two registered interfaces matches whichever is found first — do
 * not rely on it.</p>
 *
 * <p>A stage that returns {@link Float#NaN} falls through to the next one, so a
 * catch-all registered against {@link BlockEntity#getClass()} can decline and
 * still let a block-level provider answer.</p>
 *
 * <p>Providers run on the server thread once per second per block position in
 * the search radius. Keep them cheap: no block scans, no chunk loads.</p>
 *
 * <p>A provider only fires for a block that also has a
 * {@code thermia:block_temperature} data map entry — that entry is what makes
 * the block discoverable and supplies its {@code search_cap} and
 * {@code is_radiative}. This registry overrides the value, not the discovery.</p>
 */
public final class BlockTemperatures
{
    @FunctionalInterface
    public interface BlockEntityTemperatureProvider
    {
        /**
         * @return the block's temperature or {@linkFloat#NaN} is this block entity is not applicable
         */
        float get(Level level, BlockPos pos, BlockEntity blockEntity, BlockTemperatureDataMap dataMap);
    }

    @FunctionalInterface
    public interface BlockTemperatureProvider
    {
        /**
         * @return the block's temperature or {@link Float#NaN} if this block is not applicable
         */
        float get(Level level, BlockPos pos, BlockState blockState, BlockTemperatureDataMap dataMap);
    }

    private static final float NONE = Float.NaN;

    private static final Map<Class<?>, BlockEntityTemperatureProvider> BLOCK_ENTITY_PROVIDERS = new LinkedHashMap<>();
    private static final Map<Class<?>, BlockTemperatureProvider> BLOCK_PROVIDERS = new LinkedHashMap<>();

    private static ClassValue<BlockEntityTemperatureProvider> blockEntityCache = newBlockEntityCache();
    private static ClassValue<BlockTemperatureProvider> blockCache = newBlockCache();

    private static volatile boolean frozen;
    private static final Logger LOGGER = LogUtils.getLogger();

    private static ClassValue<BlockEntityTemperatureProvider> newBlockEntityCache()
    {
        return new ClassValue<BlockEntityTemperatureProvider>()
        {
            @Override
            protected BlockEntityTemperatureProvider computeValue(@NotNull Class<?> type)
            {
                return find(BLOCK_ENTITY_PROVIDERS, type, BlockEntity.class);
            }
        };
    }

    private static ClassValue<BlockTemperatureProvider> newBlockCache()
    {
        return new ClassValue<BlockTemperatureProvider>()
        {
            @Override
            protected BlockTemperatureProvider computeValue(@NotNull Class<?> type)
            {
                return find(BLOCK_PROVIDERS, type, null);
            }
        };
    }

    private static <T> T find(Map<Class<?>, T> providers, Class<?> type, Class<?> fallbackKey)
    {
        for (Class<?> c = type; c != null && c != Object.class && c != fallbackKey; c = c.getSuperclass())
        {
            T provider = providers.get(c);
            if (provider != null) return provider;
        }

        Deque<Class<?>> queue = new ArrayDeque<>();
        Set<Class<?>> seen = new HashSet<>();

        for (Class<?> c = type; c != null && c != Object.class && c != fallbackKey; c = c.getSuperclass())
        {
            for (Class<?> i : c.getInterfaces())
            {
                if (seen.add(i))
                {
                    queue.add(i);
                }
            }
        }

        while (!queue.isEmpty())
        {
            Class<?> i = queue.poll();

            T provider = providers.get(i);
            if (provider != null) return provider;

            for (Class<?> parent : i.getInterfaces())
            {
                if (seen.add(parent))
                {
                    queue.add(parent);
                }
            }
        }

        return fallbackKey == null ? null : providers.get(fallbackKey);
    }

    /**
     * Registers a block entity provider. Keys may be a concrete block entity
     * class or an interface it implements. {@link BlockEntity} itself is a valid
     * key and acts as the fallback for every block entity no other key matched.
     */
    public static void register(Class<?> type, BlockEntityTemperatureProvider provider)
    {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(provider, "provider");

        LOGGER.debug(describe(type));

        if (type == Object.class)
        {
            throw new IllegalArgumentException("Refusing to register '" + type.getName() + "' it matches every block");
        }

        if (!type.isInterface() && !BlockEntity.class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException("'" + type.getName() + "' is not a block entity class or interface");
        }

        if (frozen)
        {
            throw new IllegalStateException("Cannot register block entity temperature for '" + type.getName() + "' after freeze");
        }

        if (BLOCK_ENTITY_PROVIDERS.putIfAbsent(type, provider) != null)
        {
            throw new IllegalStateException("Duplicate block entity temperature provider for '" + type.getName() + "'");
        }
    }

    /**
     * Registers a block provider, for blocks whose temperature depends on
     * something other than their state but that have no block entity.
     */
    public static void registerBlock(Class<? extends Block> type, BlockTemperatureProvider provider)
    {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(provider, "provider");

        if (type == Block.class)
        {
            throw new IllegalArgumentException("Refusing to register '" + type.getName() + "' as it matches nearly every block");
        }

        if (frozen)
        {
            throw new IllegalStateException("Cannot register block temperature for '" + type.getName() + "' after freeze");
        }

        if (BLOCK_PROVIDERS.putIfAbsent(type, provider) != null)
        {
            throw new IllegalStateException("Duplicate block temperature provider for '" + type.getName() + "'");
        }
    }

    /**
     * @return the provider's value, or {@link Float#NaN} if no provider applies or the provider returned a non-finite value
     */
    public static float resolve(Level level, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity, BlockTemperatureDataMap dataMap)
    {
        if (blockEntity != null && !BLOCK_ENTITY_PROVIDERS.isEmpty())
        {
            BlockEntityTemperatureProvider provider = blockEntityCache.get(blockEntity.getClass());
            if (provider != null)
            {
                float value = provider.get(level, pos, blockEntity, dataMap);
                if (Float.isFinite(value)) return value;
            }
        }

        if (!BLOCK_PROVIDERS.isEmpty())
        {
            BlockTemperatureProvider provider = blockCache.get(blockState.getBlock().getClass());
            if (provider != null)
            {
                float value = provider.get(level, pos, blockState, dataMap);
                if (Float.isFinite(value)) return value;
            }
        }

        return NONE;
    }

    public static boolean hasBlockEntity(Class<?> type)
    {
        return BLOCK_ENTITY_PROVIDERS.containsKey(type);
    }

    public static boolean hasBlock(Class<?> type)
    {
        return BLOCK_PROVIDERS.containsKey(type);
    }

    public static int size()
    {
        return BLOCK_PROVIDERS.size() + BLOCK_ENTITY_PROVIDERS.size();
    }

    public static void freeze()
    {
        frozen = true;
        blockEntityCache = newBlockEntityCache();
        blockCache = newBlockCache();
    }

    public static String describe(Class<?> type)
    {
        BlockEntityTemperatureProvider provider = blockEntityCache.get(type);
        return type.getName() + " -> " + (provider == null ? "none" : provider.getClass().getName());
    }
}
