package com.nyonyix.thermia.data.manager;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.data.attachment.ChunkHumidity;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

import java.util.*;

public class ChunkHumidityManager
{
    public static final Map<ServerLevel, Set<ChunkPos>> loadedChunkCache = new HashMap<>();
    public static final Map<ServerLevel, Set<ChunkPos>> workingChunkCache = new HashMap<>();
    public static int lastTickedTFCHour = -1;

    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean shouldGetSystem(LevelChunk chunk)
    {
        // Do some shit
        return true;
    }

    private static void updateChunk(LevelChunk chunk)
    {
        if (chunk.hasData(ThermiaAttachments.CHUNK_HUMIDITY))
        {
            float humidity = chunk.getData(ThermiaAttachments.CHUNK_HUMIDITY).humidity();
            Level level = chunk.getLevel();
            int chunkCenterX = chunk.getPos().getWorldPosition().getX() + 8;
            int chunkCenterZ = chunk.getPos().getWorldPosition().getZ() + 8;
            BlockPos pos = new BlockPos(chunkCenterX, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, chunkCenterX, chunkCenterZ), chunkCenterZ).above();

            humidity = EnvironmentHelpers.newEnvironmentHumidity(level, pos);

            chunk.setData(ThermiaAttachments.CHUNK_HUMIDITY, ChunkHumidity.createDefault().withHumidity(humidity));
        }
    }

    public static void initChunk(LevelChunk chunk)
    {
        if (shouldGetSystem(chunk))
        {
            if (chunk.hasData(ThermiaAttachments.CHUNK_HUMIDITY)) return;
            chunk.setData(ThermiaAttachments.CHUNK_HUMIDITY, ChunkHumidity.createDefault());
        }
    }

    public static void refreshWorkingCache(ServerLevel level)
    {
        Set<ChunkPos> loaded = loadedChunkCache.get(level);
        if (loaded == null || loaded.isEmpty()) return;

        Set<ChunkPos> working = workingChunkCache.computeIfAbsent(level, k -> new HashSet<>());
        working.clear();
        working.addAll(loaded);

        LOGGER.info("Hour {}: Refreshed workingChunkCache with {} chunks", Calendars.get(level).getHourOfDay(), working.size());
    }

    public static void processChunkBatch(ServerLevel level, int batchSize)
    {
        Set<ChunkPos> working = workingChunkCache.get(level);
        if (working == null || working.isEmpty()) return;

        int processed = 0;
        Iterator<ChunkPos> iterChunk = working.iterator();
        while (iterChunk.hasNext() && processed < batchSize)
        {
            ChunkPos pos = iterChunk.next();
            iterChunk.remove();

            if (!level.hasChunk(pos.x, pos.z)) continue;
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk == null) continue;

            if (level.getChunkSource().chunkMap.getVisibleChunkIfPresent(pos.toLong()) == null) return;

            updateChunk(chunk);
            processed++;
        }
    }
}
