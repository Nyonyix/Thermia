package com.nyonyix.thermia.data.manager;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.api.ThermiaInteriorAPI;
import com.nyonyix.thermia.data.InteriorBlocks;
import com.nyonyix.thermia.data.attachment.ClientInteriorAttachment;
import com.nyonyix.thermia.data.climate.ThermiaClimateModel;
import com.nyonyix.thermia.data.records.ClientInterior;
import com.nyonyix.thermia.data.records.Interior;
import com.nyonyix.thermia.data.attachment.InteriorAttachment;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.datamap.BlockSealDataMap;
import com.nyonyix.thermia.data.datamap.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.FluidTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import com.nyonyix.thermia.util.InteriorScanner;
import net.dries007.tfc.util.calendar.Calendar;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateModel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class InteriorManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float CONVERGENCE_RATE = 1f;
    private static final float MAX_STEP = 0.5f;
    private static final int SECONDS_TO_KEEP = 60;
    private static final Map<BlockPos, CompletableFuture<Interior>> pendingInteriorScans = new HashMap<>();
    private static final Map<BlockPos, CompletableFuture<Interior>> pendingInteriorRescans = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, StasisEntry>> interiorStasis = new HashMap<>();

    public record StasisEntry(Long startTick, Interior interior) {}

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

                    if (interiors.containsKey(entry.getKey()))
                    {
                        Interior oldInterior = interiors.get(entry.getKey());

                        interior = interior.withInternalTemperature(oldInterior.internalTemperature());
                        if (!interior.isValid() && oldInterior.isValid()) interior = interior.withBoundingBox(oldInterior.boundingBox());
                    }
                    else
                    {
                        interior = interior.withInternalTemperature(getTemperatureSample(level, interior));

                        if (interior.isValid())
                        {
                            for (Player player : getPLayersInBox(level, interior.boundingBox()))
                            {
                                player.displayClientMessage(Component.translatable("thermia.interior.chatCreation").withStyle(ChatFormatting.DARK_GREEN), true);
                            }
                        }
                    }

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
        if (!toRemove.isEmpty()) updateSyncedInterior(level, interiors);

        return interiors;
    }

    private static float getTemperatureSample(Level level, Interior interior)
    {
        ClimateModel model = Climate.get(level);

        if (model instanceof ThermiaClimateModel thermiaModel)
        {
            return thermiaModel.getRawInstantTemperature(level, interior.homePos());
        }

        return model.getInstantTemperature(level, interior.homePos());
    }

    private static float calcSourcePull(Level level, Interior interior, float internalTemperature)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        float pull = getSolarRadiation(level, interior);;
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

    private static List<Player> getPLayersInBox(Level level, AABB box)
    {
        List<ServerPlayer> players = ((ServerLevel) level).players();
        return players.stream().filter(p -> box.contains(p.position())).collect(Collectors.toList());
    }

    private static void updateSyncedInterior(Level level, Map<BlockPos, Interior> interiors)
    {
        Map<BlockPos, ClientInterior> synced = new HashMap<>();

        interiors.forEach((pos, i) -> synced.put(pos, ClientInterior.from(i)));
        level.setData(ThermiaAttachments.CLIENT_INTERIOR_ATTACHMENT, new ClientInteriorAttachment(synced));
    }

    private static float getAveragedLeakiness(Level level, Interior interior)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbourPos = new BlockPos.MutableBlockPos();
        float totalOpenness = 0f;
        float windSPeedMS = EnvironmentHelpers.getWindSpeed(level, interior.homePos(), 1f);
        ClimateModel model = Climate.get(level);
        Vec2 windVector = model.getWind(level, interior.homePos());
        Vec2 windDir = windVector.lengthSquared() > 0.0001f ? windVector.normalized() : Vec2.ZERO;
        int shellSize = interior.interiorBlocks().edgeBlocks.size();

        for (Map.Entry<Long, Block> entry : interior.interiorBlocks().edgeBlocks.long2ObjectEntrySet())
        {
            pos.set(entry.getKey());
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

            InteriorBlocks.EdgeBlockData edgeData = interior.interiorBlocks().edgeBlockData.get(entry.getKey().longValue());
            if (edgeData == null || !edgeData.isExterior()) continue;

            BlockSealDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getValue()).getData(ThermiaDataMaps.BLOCK_POROSITY_DATA_MAP);
            if (dataMap == null) continue;

            BlockState state = level.getBlockState(pos);
            Direction inwardFace = edgeData.outwardFace().getOpposite();
            if (state.isFaceSturdy(level, pos, inwardFace)) continue;

            float blockSeal = 1f - dataMap.resolveForState(state);

            Vec2 outwardFaceNormal = new Vec2(edgeData.outwardFace().getStepX(), edgeData.outwardFace().getStepZ());
            float alignment = Math.abs(windDir.dot(outwardFaceNormal));
            float windBoost = 1.0f + alignment * windSPeedMS * (float) ServerConfig.INTERIOR_WIND_FACTOR.getAsDouble();

            totalOpenness += blockSeal * windBoost;
        }

        if (shellSize == 0) return 0f;

        float openFraction = totalOpenness / shellSize;
        float leakiness = 1.0f - (float) Math.exp(-openFraction * ServerConfig.INTERIOR_VENT_CURVE.getAsDouble());

        return Math.clamp(leakiness, 0.0f, 1.0f);
    }

    private static float getSolarRadiation(Level level, Interior interior)
    {
        Vec3 sunDir = EnvironmentHelpers.getSunDirection(level, interior.homePos());
        if (sunDir == Vec3.ZERO) return 0.0f;

        float baseRadiation = EnvironmentHelpers.getBeamRadiationWeather(level, interior.homePos(), 1.0f) * (float) ServerConfig.INTERIOR_SOLAR_MULTI.getAsDouble();
        if (baseRadiation <= 0f) return 0.0f;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        float totalSolarRadiation = 0.0f;
        int transparentSurfaces = 0;

        for (Map.Entry<Long, Block> entry : interior.interiorBlocks().edgeBlocks.long2ObjectEntrySet())
        {
            long packedPos = entry.getKey();
            pos.set(packedPos);
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

            InteriorBlocks.EdgeBlockData edgeData = interior.interiorBlocks().edgeBlockData.get(packedPos);
            if (edgeData == null || !edgeData.isExterior()) continue;

            BlockState state = level.getBlockState(pos);
            float opacity = state.getLightBlock(level, pos);
            if (opacity >= 15f) continue;

            float transparency = 1.0f - (opacity / 15.0f);

            Direction outwardFace = edgeData.outwardFace();
            Vec3 outwardFaceNormal = new Vec3(outwardFace.getStepX(), outwardFace.getStepY(), outwardFace.getStepZ());

            float alignment = (float) sunDir.dot(outwardFaceNormal);
            if (alignment > 0.0f)
            {
                totalSolarRadiation += transparency * alignment;
                transparentSurfaces++;
            }
        }

        if (transparentSurfaces == 0) return 0.0f;

        float averageRadiation = totalSolarRadiation / transparentSurfaces;
        return totalSolarRadiation * baseRadiation;
    }

    public static void onCreateEvent(Level level, BlockPos startPos)
    {
        int maxSize = ServerConfig.MAX_INTERIOR_VOLUME.getAsInt();

        if (!pendingInteriorScans.containsKey(startPos) && !ThermiaInteriorAPI.isInInterior(level, startPos))
        {
            CompletableFuture<Interior> future = InteriorScanner.scanAsync(level, startPos, maxSize);
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

        Map<BlockPos, Interior> interiors = new HashMap<>(level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors());
        List<BlockPos> toRemove = new ArrayList<>();
        boolean tempUpdated = false;

        interiors = joinPending(pendingInteriorScans,interiors, level);
        interiors = joinPending(pendingInteriorRescans, interiors, level);

        for (Map.Entry<BlockPos, Interior> entry : interiors.entrySet())
        {
            if (!entry.getValue().isValid())
            {
                interiorStasis.put(entry.getValue().homePos(), level.getServer().getTickCount());
            }

            long interiorId = entry.getKey().asLong();

            if (Math.floorMod(interiorId, 20) == serverTick % 20)
            {
                Interior interior = entry.getValue();
                ICalendar calendar = Calendars.SERVER;

                if (!level.isLoaded(BlockPos.of(interiorId))) continue;

                float internalTemperature = interior.internalTemperature();
                float externalTemperature = getTemperatureSample(level, interior);

                float volume = interior.internalAirBlocks().size();
                float sourcePull = calcSourcePull(level, interior, internalTemperature) / volume;

                float leakiness = Math.max(getAveragedLeakiness(level, interior), 0.01f);
                float externalPull = leakiness * (externalTemperature - internalTemperature);

                long calendarTicksElapsed = calendar.getFixedCalendarTicksFromTick(20);
                float hoursElapsed = calendarTicksElapsed / (float) Calendar.CALENDAR_TICKS_IN_HOUR;
                float dt = Math.min(hoursElapsed * CONVERGENCE_RATE, MAX_STEP);

                internalTemperature += (externalPull + sourcePull) * dt;

                interior = interior.withInternalTemperature(internalTemperature).withExternalTemperature(externalTemperature).withPorosity(leakiness).withExternalPull(externalPull).withSourcePull(sourcePull).withVolume(volume);

                interiors.put(BlockPos.of(interiorId), interior);
                tempUpdated = true;
            }
        }

        if (!interiorStasis.isEmpty())
        {
            for (Map.Entry<BlockPos, Integer> entry : interiorStasis.entrySet())
            {
                int serverTicks = level.getServer().getTickCount();
                int interiorTicks = serverTicks + (entry.getValue()) + (SECONDS_TO_KEEP * 20);

                if (interiorTicks == serverTicks)
                {
                    interiors.remove(entry.getKey());
                }
            }
            updateSyncedInterior(level, interiors);
        }

        level.setData(ThermiaAttachments.INTERIOR_ATTACHMENT, new InteriorAttachment(interiors));

        if (tempUpdated)
        {
            Map<BlockPos, ClientInterior> clientsInteriors = new HashMap<>();
            Map<BlockPos, ClientInterior> oldClientsInteriors = level.getData(ThermiaAttachments.CLIENT_INTERIOR_ATTACHMENT).activeClientInteriors();

            for (Interior i : interiors.values())
            {
                ClientInterior old = oldClientsInteriors.get(i.homePos());
                clientsInteriors.put(i.homePos(), old != null ? new ClientInterior(old.homePos(), old.boundingBox(), old.isValid(), old.origin(), old.sizeX(), old.sizeY(), old.sizeZ(), old.membership(), i.internalHumidity(), i.externalHumidity(), i.internalTemperature(), i.externalTemperature(), i.porosity(), i.externalPull(), i.sourcePull(), ClientInterior.InteriorCounts.from(i)) : ClientInterior.from(i));
            }

            level.setData(ThermiaAttachments.CLIENT_INTERIOR_ATTACHMENT, new ClientInteriorAttachment(clientsInteriors));
        }
    }

    public static void invalidateAndRescan(Level level, BlockPos pos)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT)) return;

        Map<BlockPos, Interior> interiors = level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors();

        for (BlockPos id : interiors.keySet())
        {
            if (pos.distManhattan(id) > 256) continue;
            if (!interiors.get(id).isValid()) continue;

            if (ThermiaInteriorAPI.isInInterior(pos, interiors.get(id)))
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
