package com.nyonyix.thermia.data.manager;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.Interior;
import com.nyonyix.thermia.data.attachment.InteriorAttachment;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.datamap.BlockPorosityDataMap;
import com.nyonyix.thermia.data.datamap.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.FluidTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import com.nyonyix.thermia.util.InteriorScanner;
import net.dries007.tfc.util.calendar.Calendar;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateModel;
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

public class InteriorManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float CONVERGENCE_RATE = 1.0f;
    private static final float MAX_STEP = 0.5f;
    private static final Map<BlockPos, CompletableFuture<Interior>> pendingInteriorScans = new HashMap<>();
    private static final Map<BlockPos, CompletableFuture<Interior>> pendingInteriorRescans = new HashMap<>();

    private static void interiorRescan(Level level, BlockPos startPos)
    {
        int maxSize = ServerConfig.MAX_INTERIOR_VOLUME.getAsInt();

        if (pendingInteriorRescans.containsKey(startPos))
        {
            pendingInteriorRescans.get(startPos).cancel(true);
            pendingInteriorRescans.remove(startPos);
        }

        CompletableFuture<Interior> future = InteriorScanner.scanAsync(level, startPos, maxSize);
        pendingInteriorRescans.put(startPos, future);
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
                    Interior interior = entry.getValue().join();

                    if (interiors.containsKey(interior.homePos()))
                    {
                        Interior oldInterior = interiors.get(interior.homePos());

                        interior = interior.withInternalTemperature(oldInterior.internalTemperature());
                    }
                    else interior = interior.withInternalTemperature(getTemperatureSample(level, interior));

                    interiors.put(entry.getKey(), interior);
                }
                catch (Exception e)
                {
                    LOGGER.error("Error in async interior flood fill attempt:", e);
                }

                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(pendingMap::remove);

        return interiors;
    }

    private static float getTemperatureSample(Level level, Interior interior)
    {
        ClimateModel model = Climate.get(level);

        return model.getInstantTemperature(level, interior.homePos());
    }

    private static float calcSourcePull(Level level, Interior interior, float internalTemperature)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        float pull = 0f;
        float multi = (float) ServerConfig.INTERIOR_SOURCE_MULTI.getAsDouble();

        for (Map.Entry<Long, Block> entry : interior.interiorBlocks().heatSinkBlocks.long2ObjectEntrySet())
        {
            pos.set(entry.getKey());
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

            BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getValue()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (dataMap == null) continue;

            pull += (dataMap.resolveForState(level, pos) - internalTemperature) * multi;
        }

        for (Map.Entry<Long, Block> entry : interior.interiorBlocks().heatSourceBlocks.long2ObjectEntrySet())
        {
            pos.set(entry.getKey());
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

            BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getValue()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (dataMap == null) continue;

            pull += (dataMap.resolveForState(level, pos) - internalTemperature) * multi;
        }

        for (Map.Entry<Long, Fluid> entry : interior.interiorBlocks().heatSinkFluids.long2ObjectEntrySet())
        {
            pos.set(entry.getKey());
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

            FluidTemperatureDataMap dataMap = BuiltInRegistries.FLUID.wrapAsHolder(entry.getValue()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (dataMap == null) continue;

            Block block = level.getBlockState(pos).getBlock();
            BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            float blockTemperature = blockDataMap != null ? blockDataMap.resolveForState(level, pos) : 0f;
            float sourceTemp = blockTemperature != 0f ? blockTemperature : dataMap.temperature();

            pull += (sourceTemp - internalTemperature) * multi;
        }

        for (Map.Entry<Long, Fluid> entry : interior.interiorBlocks().heatSourceFluids.long2ObjectEntrySet())
        {
            pos.set(entry.getKey());
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

            FluidTemperatureDataMap dataMap = BuiltInRegistries.FLUID.wrapAsHolder(entry.getValue()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (dataMap == null) continue;

            Block block = level.getBlockState(pos).getBlock();
            BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            float blockTemperature = blockDataMap != null ? blockDataMap.resolveForState(level, pos) : 0f;
            float sourceTemp = blockTemperature != 0f ? blockTemperature : dataMap.temperature();

            pull += (sourceTemp - internalTemperature) * multi;
        }

        return pull;
    }

    private static float getLeakiness(Level level, Interior interior)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbourPos = new BlockPos.MutableBlockPos();
        Set<BlockState> debugBlockStates = new HashSet<>();
        float leakiness = 0f;
        int leakyBlocks = 0;

        for (Map.Entry<Long, Block> entry : interior.interiorBlocks().edgeBlocks.long2ObjectEntrySet())
        {
            pos.set(entry.getKey());
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

            BlockPorosityDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getValue()).getData(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP);
            if (dataMap == null) continue;

            BlockState state = level.getBlockState(pos);
            Direction exposedFace = null;
            int exposedCount = 0;

            for (Direction dir : Direction.values())
            {
                neighbourPos.setWithOffset(pos, dir);

                if (interior.internalAirBlocks().contains(neighbourPos))
                {
                    exposedFace = dir;
                    exposedCount++;
                }
            }

            if (exposedCount != 1) continue;
            if (!state.isFaceSturdy(level, pos, exposedFace))
            {
                leakiness +=  1f - dataMap.resolveForState(state);
                leakyBlocks++;
                debugBlockStates.add(state);
            }
        }

        return leakyBlocks > 0 ? (float) Math.pow(leakiness / leakyBlocks, 0.75) : 0f;
    }

    public static boolean isInInterior(Level level, BlockPos pos) {return getInteriorByPos(level, pos).isValid();}

    public static boolean isInInterior(BlockPos pos, Interior interior) {return interior.internalAirBlocks().contains(pos) || interior.interiorBlocks().edgeBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSourceBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSinkBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSourceFluids.containsKey(pos.asLong()) || interior.interiorBlocks().heatSinkFluids.containsKey(pos.asLong());}

    public static void onCreateEvent(Level level, BlockPos startPos)
    {
        int maxSize = ServerConfig.MAX_INTERIOR_VOLUME.getAsInt();

        if (!pendingInteriorScans.containsKey(startPos) && !isInInterior(level, startPos))
        {
            CompletableFuture<Interior> future = InteriorScanner.scanAsync(level, startPos, maxSize);
            pendingInteriorScans.put(startPos, future);
        }
    }

    public static Interior getInteriorByPos(Level level, BlockPos pos)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT)) return Interior.createDefault();

        for (Interior interior : level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors().values())
        {
            if (!interior.isValid()) continue;

            if (interior.internalAirBlocks().contains(pos) || interior.interiorBlocks().edgeBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSourceBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSinkBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSourceFluids.containsKey(pos.asLong()) || interior.interiorBlocks().heatSinkFluids.containsKey(pos.asLong())) return interior;
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
                toRemove.add(entry.getKey());
                continue;
            }

            long interiorId = entry.getKey().asLong();

            if (Math.floorMod(interiorId, 20) == serverTick % 20)
            {
                Interior interior = entry.getValue();
                ICalendar calendar = Calendars.SERVER;

                if (!level.isLoaded(BlockPos.of(interiorId))) continue;

                float volume = interior.internalAirBlocks().size();
                float internalTemperature = interior.internalTemperature();
                float externalTemperature = getTemperatureSample(level, interior);

                float sourcePull = calcSourcePull(level, interior, internalTemperature) / volume;

                float leakiness = Math.max(getLeakiness(level, interior), 0.01f);
                float externalPull = leakiness * (externalTemperature - internalTemperature);

                long calendarTicksElapsed = calendar.getFixedCalendarTicksFromTick(20);
                float hoursElapsed = calendarTicksElapsed / (float) Calendar.CALENDAR_TICKS_IN_HOUR;
                float dt = Math.min(hoursElapsed * CONVERGENCE_RATE, MAX_STEP);

                internalTemperature += (externalPull + sourcePull) * dt;

                interior = interior.withInternalTemperature(internalTemperature);
                interior = interior.withExternalTemperature(externalTemperature);

                interiors.put(BlockPos.of(interiorId), interior);
            }
        }

        toRemove.forEach(interiors::remove);

        level.setData(ThermiaAttachments.INTERIOR_ATTACHMENT, new InteriorAttachment(interiors));
    }

    public static void invalidateAndRescan(Level level, BlockPos pos)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT)) return;

        Map<BlockPos, Interior> interiors = level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors();

        for (BlockPos id : interiors.keySet())
        {
            if (pos.distManhattan(id) > 256) continue;
            if (!interiors.get(id).isValid()) continue;

            if (isInInterior(pos, interiors.get(id)))
            {
//                Interior interior = interiors.get(id);

                interiorRescan(level, id);
//                interiors.put(id, interior.withIsValid(false));
//                level.setData(ThermiaAttachments.INTERIOR_ATTACHMENT, new InteriorAttachment(interiors));

                return;
            }
        }
    }
}
