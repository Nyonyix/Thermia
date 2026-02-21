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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class InteriorManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SIZE = ServerConfig.MAX_INTERIOR_VOLUME.getAsInt();
    private static final Map<BlockPos, CompletableFuture<Interior>> pendingInteriorScans = new HashMap<>();

    public static void onCreateEvent(Level level, BlockPos startPos)
    {
        if (!pendingInteriorScans.containsKey(startPos))
        {
            CompletableFuture<Interior> future = InteriorScanner.scanAsync(level, startPos, MAX_SIZE);
            pendingInteriorScans.put(startPos, future);
        }
    }

    public static void onTick(Level level, int serverTick)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT))
        {
            Map<BlockPos, Interior> interiors = new HashMap<>();
            level.setData(ThermiaAttachments.INTERIOR_ATTACHMENT, new InteriorAttachment(interiors));
        }

        Map<BlockPos, Interior> interiors = level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors();

        for (Map.Entry<BlockPos, CompletableFuture<Interior>> entry : pendingInteriorScans.entrySet())
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

                pendingInteriorScans.remove(entry.getKey());
            }
        }

        for (Map.Entry<BlockPos, Interior> entry : interiors.entrySet())
        {
            long interiorId = entry.getKey().asLong();

            if (serverTick % 20 == interiorId % 20)
            {
                //do stuff
            }
        }

        level.setData(ThermiaAttachments.INTERIOR_ATTACHMENT, new InteriorAttachment(interiors));
    }
}
