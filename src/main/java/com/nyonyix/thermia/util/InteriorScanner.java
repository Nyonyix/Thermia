package com.nyonyix.thermia.util;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.Interior;
import com.nyonyix.thermia.data.InteriorBlocks;
import com.nyonyix.thermia.data.datamap.BlockPorosityDataMap;
import com.nyonyix.thermia.data.datamap.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.FluidTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class InteriorScanner
{
    private static final int SOLID_CHECK_DISTANCE = 3;
    private static final float MAX_PERCENTAGE_OPEN_ALLOWED = (float) ServerConfig.MAX_PERCENTAGE_OPEN_ALLOWED.getAsDouble();
    private static final Logger LOGGER = LogUtils.getLogger();

//    private static List<Direction> getPlaneDirections(Direction.Axis axis)
//    {
//        return switch (axis)
//        {
//            case X -> List.of(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH);
//            case Y -> List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
//            case Z -> List.of(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST);
//        };
//    }

//    private static boolean hasSolidNearby(Level level, BlockPos pos, List<Direction> directions)
//    {
//        for (Direction dir : directions)
//        {
//            for (int i = 1; i <= SOLID_CHECK_DISTANCE; i++)
//            {
//                if (isSolid(level, pos.relative(dir, i))) return true;
//            }
//        }
//
//        return false;
//    }

//    private static Set<BlockPos> gatherOpeningCluster(Level level, BlockPos start, Direction.Axis normalAxis, Set<BlockPos> internalAirBlocks)
//    {
//        Set<BlockPos> cluster = new HashSet<>();
//        Queue<BlockPos> queue = new ArrayDeque<>();
//
//        queue.add(start);
//        cluster.add(start);
//
//        List<Direction> planeDirections = getPlaneDirections(normalAxis);
//
//        while (!queue.isEmpty())
//        {
//            BlockPos current = queue.poll();
//
//            if (cluster.size() > MAX_OPENING_SIZE) return cluster;
//
//            for (Direction dir : planeDirections)
//            {
//                BlockPos neighbour = current.relative(dir);
//
//                if (cluster.contains(neighbour)) continue;
//                if (internalAirBlocks.contains(neighbour)) continue;
//                if (isSolid(level, neighbour)) continue;
//
//                if (hasSolidNearby(level, neighbour, planeDirections))
//                {
//                    cluster.add(neighbour);
//                    queue.add(neighbour);
//                }
//            }
//        }
//
//        return cluster;
//    }

//    private static boolean isOpening(Level level, BlockPos pos)
//    {
//        for (Direction dir : List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST))
//        {
//            if (isSolid(level, pos.relative(dir)))
//            {
//                Direction opposite = dir.getOpposite();
//
//                for (int i = 1; i <= SOLID_CHECK_DISTANCE; i++)
//                {
//                    if (isSolid(level, pos.relative(opposite, i))) return true;
//                }
//            }
//        }
//
//        return false;
//    }

    private static boolean isSolid(Level level, BlockPos pos)
    {
        BlockPorosityDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(level.getBlockState(pos).getBlock()).getData(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP);

        if (level.getBlockState(pos).isSolidRender(level, pos) || dataMap != null) return true;

        return false;
    }

    private static boolean isHeatSource(Block block)
    {
        BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
        if (dataMap == null) return false;

        return dataMap.temperature() > 0f;
    }

    private static boolean isHeatSource(Fluid fluid)
    {
        FluidTemperatureDataMap dataMap = BuiltInRegistries.FLUID.wrapAsHolder(fluid).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
        if (dataMap == null) return false;

        return dataMap.temperature() > 0f;
    }

    private static boolean isHeatSink(Block block)
    {
        BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
        if (dataMap == null) return false;

        return dataMap.temperature() < 0f;
    }

    private static boolean isHeatSink(Fluid fluid)
    {
        FluidTemperatureDataMap dataMap = BuiltInRegistries.FLUID.wrapAsHolder(fluid).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
        if (dataMap == null) return false;

        return dataMap.temperature() < 0f;
    }

    @SuppressWarnings("deprecation")
    public static Interior scan(Level level, BlockPos startPos, int maxSize)
    {
        Map<BlockPos, Block> edgeBlocks = new HashMap<>();
        Map<BlockPos, Block> heatSourceBlocks = new HashMap<>();
        Map<BlockPos, Fluid> heatSourceFluids = new HashMap<>();
        Map<BlockPos, Block> heatSinkBlocks = new HashMap<>();
        Map<BlockPos, Fluid> heatSinkFluids = new HashMap<>();
        Set<BlockPos> internalAirBlocks = new HashSet<>();
        LongOpenHashSet visited = new LongOpenHashSet();
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        Long startPosLong = startPos.asLong();

        queue.enqueue(startPosLong);
        visited.add(startPosLong);

        while (!queue.isEmpty())
        {
            if (internalAirBlocks.size() >= maxSize)
            {
                LOGGER.info("Interior scan failed, exceeded size {}", maxSize);
                return Interior.createDefault();
            }

            BlockPos current = BlockPos.of(queue.dequeueLong());

//            if (isOpening(level, current))
//            {
//                edgeBlocks.put(current, level.getBlockState(current));
//                openings.add(current);
//                edgeAirCount++;
//                continue;
//            }

            internalAirBlocks.add(current);

            for (Direction dir : Direction.values())
            {
                BlockPos neighbour = current.relative(dir);
                long neighbourLong = neighbour.asLong();
                BlockState state = level.getBlockState(neighbour);
                Block block = state.getBlock();

                if (visited.contains(neighbourLong)) continue;

                if (!state.getFluidState().isEmpty())
                {
                    Fluid fluid = state.getFluidState().getType();

                    if (isHeatSource(fluid)) heatSourceFluids.put(neighbour, fluid);
                    else if (isHeatSink(fluid)) heatSinkFluids.put(neighbour, fluid);
                }

                if (isHeatSource(state.getBlock())) heatSourceBlocks.put(neighbour, block);
                else if (isHeatSink(state.getBlock())) heatSinkBlocks.put(neighbour, block);

                if (isSolid(level, neighbour))
                {
                    edgeBlocks.put(neighbour, block);
                    visited.add(neighbourLong);
                }
                else if (level.canSeeSky(neighbour))
                {
                    LOGGER.info("Interior scan failed, Reached sky at {}", neighbour);
                    return Interior.createDefault();
                }
                else
                {
                    if (visited.add(neighbourLong)) queue.enqueue(neighbourLong);
                }
            }
        }

//        float airRatio = edgeBlocks.isEmpty() ? 1f : (float) edgeAirCount / edgeBlocks.size();
//        if (airRatio > MAX_PERCENTAGE_OPEN_ALLOWED)
//        {
//            LOGGER.info("Interior scan failed, structure is too open ({}% air in edges)", airRatio * 100);
//            return Interior.createDefault();
//        }

        return new Interior(new InteriorBlocks(edgeBlocks, heatSourceBlocks, heatSourceFluids, heatSinkBlocks, heatSinkFluids), internalAirBlocks, startPos, true, 0f, 0f, 0f, 0f);
    }

    public static CompletableFuture<Interior> scanAsync(Level level, BlockPos startPos, int maxSize)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            Interior interior = Interior.createDefault();
            int maxTries = 3;

            for (int i = 0; i < maxTries; i++)
            {
                try
                {
                    interior = scan(level, startPos, maxSize);
                    break;
                }
                catch (Exception e)
                {
                    LOGGER.error("Error in async interior flood fill attempt {}:", i, e);
                }
            }

            return interior;
        }, Util.backgroundExecutor());
    }
}
