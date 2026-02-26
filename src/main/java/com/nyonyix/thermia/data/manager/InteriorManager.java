package com.nyonyix.thermia.data.manager;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.Interior;
import com.nyonyix.thermia.data.attachment.InteriorAttachment;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.util.InteriorScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class InteriorManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SIZE = ServerConfig.MAX_INTERIOR_VOLUME.getAsInt();
    private static final Map<BlockPos, CompletableFuture<Interior>> pendingInteriorScans = new HashMap<>();
    private static final Map<BlockPos, CompletableFuture<Interior>> pendingInteriorRescans = new HashMap<>();

    private static void interiorRescan(Level level, BlockPos startPos)
    {
        if (!pendingInteriorRescans.containsKey(startPos))
        {
            CompletableFuture<Interior> future = InteriorScanner.scanAsync(level, startPos, MAX_SIZE);
            pendingInteriorRescans.put(startPos, future);
        }
    }

    private static Map<BlockPos, Interior> joinPending(Map<BlockPos, CompletableFuture<Interior>> pendingMap, Map<BlockPos, Interior> interiors, Level level)
    {
        List<BlockPos> toRemove = new ArrayList<>();

        for (Map.Entry<BlockPos, CompletableFuture<Interior>> entry : pendingMap.entrySet())
        {
            if (entry.getValue().isDone())
            {
                try
                {
                    interiors.put(entry.getKey(), entry.getValue().join());
                }
                catch (Exception e)
                {
                    LOGGER.error("Error in async interior flood fill attempt:", e);
                }

                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(pendingMap::remove);
        toRemove.clear();

        return interiors;
    }

    private static float calcInteriorHeat(Level level, Interior interior)
    {
        float temperature = 0f;

        return temperature;
    }

    public static boolean isInInterior(Level level, BlockPos pos) {return getInteriorByPos(level, pos).isValid();}

    public static boolean isInInterior(BlockPos pos, Interior interior) {return interior.internalAirBlocks().contains(pos) || interior.edgeBlocks().containsKey(pos) || interior.heatSourceBlocks().containsKey(pos) || interior.heatSinkBlocks().containsKey(pos) || interior.heatSourceFluids().containsKey(pos) || interior.heatSinkFluids().containsKey(pos);}

    public static void onCreateEvent(Level level, BlockPos startPos)
    {
        if (!pendingInteriorScans.containsKey(startPos) && !isInInterior(level, startPos))
        {
            CompletableFuture<Interior> future = InteriorScanner.scanAsync(level, startPos, MAX_SIZE);
            pendingInteriorScans.put(startPos, future);
        }
    }

    public static Interior getInteriorByPos(Level level, BlockPos pos)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT)) return Interior.createDefault();

        for (Interior interior : level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors().values())
        {
            if (!interior.isValid()) continue;

            if (interior.internalAirBlocks().contains(pos) || interior.edgeBlocks().containsKey(pos) || interior.heatSourceBlocks().containsKey(pos) || interior.heatSinkBlocks().containsKey(pos) || interior.heatSourceFluids().containsKey(pos) || interior.heatSinkFluids().containsKey(pos)) return interior;
        }

        return Interior.createDefault();
    }

    public static void onTick(Level level, int serverTick)
    {

        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT))
        {
            Map<BlockPos, Interior> interiors = new HashMap<>();
            level.setData(ThermiaAttachments.INTERIOR_ATTACHMENT, new InteriorAttachment(interiors));
        }

        Map<BlockPos, Interior> interiors = new HashMap<>(level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors());
        List<BlockPos> toRemove = new ArrayList<>();

        interiors = joinPending(pendingInteriorScans,interiors, level);
        interiors = joinPending(pendingInteriorRescans, interiors, level);

        for (Map.Entry<BlockPos, Interior> entry : interiors.entrySet())
        {
            if (!entry.getValue().isValid())
            {
                if (!pendingInteriorRescans.containsKey(entry.getKey()))
                {
                    toRemove.add(entry.getKey());
                    continue;
                }
            }

            long interiorId = entry.getKey().asLong();

            if (serverTick % 20 == interiorId % 20)
            {

            }
        }

        toRemove.forEach(interiors::remove);

        level.setData(ThermiaAttachments.INTERIOR_ATTACHMENT, new InteriorAttachment(interiors));
    }

    public static void invalidateAndRescan(Level level, BlockPos pos)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT)) return;

        Map<BlockPos, Interior> interiors = new HashMap<>(level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors());

        for (Map.Entry<BlockPos, Interior> entry : interiors.entrySet())
        {
            if (pos.distManhattan(entry.getKey()) > 256) continue;
            if (!entry.getValue().isValid()) continue;

            if (isInInterior(pos, entry.getValue()))
            {
                interiorRescan(level, entry.getKey());
                interiors.put(entry.getKey(), entry.getValue().withIsValid(false));
                level.setData(ThermiaAttachments.INTERIOR_ATTACHMENT, new InteriorAttachment(interiors));

                return;
            }
        }
    }
}
