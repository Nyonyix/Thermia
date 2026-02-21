package com.nyonyix.thermia.util;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.Interior;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class InteriorScanner
{
    private static final int SOLID_CHECK_DISTANCE = 2;
    private static final float MAX_PERCENTAGE_OPEN_ALLOWED = (float) ServerConfig.MAX_PERCENTAGE_OPEN_ALLOWED.getAsDouble();
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean isSolid(Level level, BlockPos pos)
    {
        return level.getBlockState(pos).isSolidRender(level, pos);
    }

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

    private static boolean isOpening(Level level, BlockPos pos)
    {
        for (Direction dir : List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST))
        {
            if (isSolid(level, pos.relative(dir)))
            {
                Direction opposite = dir.getOpposite();

                for (int i = 1; i <= SOLID_CHECK_DISTANCE; i++)
                {
                    if (isSolid(level, pos.relative(opposite, i))) return true;
                }
            }
        }

        return false;
    }

    public static Interior scan(Level level, BlockPos startPos, int maxSize)
    {
        Map<BlockPos, BlockState> edgeBlocks = new HashMap<>();
        Map<BlockPos, BlockState> heatSources = new HashMap<>();
        Set<BlockPos> internalAirBlocks = new HashSet<>();
        Set<BlockPos> openings = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        int edgeAirCount = 0;

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty())
        {
            if (internalAirBlocks.size() >= maxSize)
            {
                LOGGER.debug("Interior scan failed, exceeded size {}", maxSize);
                return Interior.createDefault();
            }

            BlockPos current = queue.poll();

            if (isOpening(level, current))
            {
                edgeBlocks.put(current, level.getBlockState(current));
                openings.add(current);
                edgeAirCount++;
                continue;
            }

            internalAirBlocks.add(current);

            for (Direction dir : Direction.values())
            {
                BlockPos neighbour = current.relative(dir);

                if (visited.contains(neighbour) || openings.contains(neighbour)) continue;

                if (isSolid(level, neighbour))
                {
                    edgeBlocks.put(neighbour, level.getBlockState(neighbour));
                    visited.add(neighbour);
                }
                else if (level.canSeeSky(neighbour))
                {
                    LOGGER.debug("Interior scan failed, Reached sky at {}", neighbour);
                    return Interior.createDefault();
                }
                else
                {
                    if (visited.add(neighbour)) queue.add(neighbour);
                }
            }
        }

        float airRatio = edgeBlocks.isEmpty() ? 1f : (float) edgeAirCount / edgeBlocks.size();
        if (airRatio > MAX_PERCENTAGE_OPEN_ALLOWED)
        {
            LOGGER.debug("Interior scan failed, structure is too open ({}% air in edges)", airRatio * 100);
            return Interior.createDefault();
        }

        return new Interior(edgeBlocks, heatSources, internalAirBlocks, edgeAirCount, true);
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
