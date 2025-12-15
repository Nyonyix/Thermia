package com.nyonyix.thermia.data.manager;

import com.nyonyix.thermia.data.attachment.ChunkClimate;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.util.ClimateHelpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChunkClimateManager
{
    public static void updateChunkClimate(Level level, LevelChunk chunk)
    {
        ChunkClimate current = chunk.getData(ThermiaAttachments.CHUNK_CLIMATE);
        if (current == null) current = ChunkClimate.createDefault();

        BlockPos basePos = chunk.getPos().getWorldPosition();
        int centerX = basePos.getX() + 8;
        int centerZ = basePos.getZ() + 8;
        int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 8, 8);
        BlockPos chunkCenter = new BlockPos(centerX, surfaceY, centerZ);

        float newHumidity = ClimateHelpers.getClimateSpecificHumidity(level, chunkCenter, level.random, current.humidity());

        ChunkClimate updated = current.withHumidity(newHumidity);

        chunk.setData(ThermiaAttachments.CHUNK_CLIMATE, updated);
    }

    public static void initChunkClimate(Level level, LevelChunk chunk)
    {
        ChunkClimate existing = chunk.getData(ThermiaAttachments.CHUNK_CLIMATE);

        if (existing == null) updateChunkClimate(level, chunk);
    }

    public static ChunkClimate getOrUpdateChunkClimate(Level level, BlockPos pos)
    {
        if (!level.hasChunk(pos.getX() / 16, pos.getZ() / 16)) return ChunkClimate.createDefault();

        LevelChunk chunk = level.getChunkAt(pos);
        ChunkClimate climate = chunk.getData(ThermiaAttachments.CHUNK_CLIMATE);

        if (climate == null)
        {
            updateChunkClimate(level, chunk);
            climate = chunk.getData(ThermiaAttachments.CHUNK_CLIMATE);
        }

        return climate != null ? climate : ChunkClimate.createDefault();
    }

    public static float getHumidityOrDefault(Level level, BlockPos pos) {return getOrUpdateChunkClimate(level, pos).humidity();}
}
