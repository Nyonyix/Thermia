package com.nyonyix.thermia.data.records;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.datamap.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.FluidTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.slf4j.Logger;

import java.util.*;

public record BlockSearchResult(
        Vec3 searchOrigin,
        ResourceKey<Level> levelID,
        Map<Block, List<BlockPos>> allPositions,
        Map<Fluid, List<BlockPos>> allFluidPositions,
        Map<BlockPos, ExposedFaces> exposedFaces
)
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<BlockSearchResult> CODEC = RecordCodecBuilder.create(blockSearchResultInstance -> blockSearchResultInstance.group(
            Vec3.CODEC.fieldOf("search_origin").forGetter(BlockSearchResult::searchOrigin),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("level_id").forGetter(BlockSearchResult::levelID),
            Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), Codec.list(BlockPos.CODEC)).fieldOf("all_positions").forGetter(BlockSearchResult::allPositions),
            Codec.unboundedMap(BuiltInRegistries.FLUID.byNameCodec(), Codec.list(BlockPos.CODEC)).fieldOf("all_fluid_positions").forGetter(BlockSearchResult::allFluidPositions),
            Codec.unboundedMap(
                    Codec.STRING.xmap(
                            str -> BlockPos.of(Long.parseLong(str)),
                            pos -> String.valueOf(pos.asLong())
                    ), ExposedFaces.CODEC).fieldOf("exposed_faces").forGetter(BlockSearchResult::exposedFaces)
    ).apply(blockSearchResultInstance, BlockSearchResult::new));

    private record RankedFace(Vec3 faceCenter, float weight) {}

    private List<RankedFace> getBestFaces(Vec3 entityCenter, BlockPos sourcePos)
    {
        ExposedFaces blockExposedFaces = exposedFaces.get(sourcePos);
        if(blockExposedFaces == null || exposedFaces.isEmpty()) return List.of(new RankedFace(Vec3.atCenterOf(sourcePos), 1f));

        Vec3 blockCenter = Vec3.atCenterOf(sourcePos);
        Vec3 toEntity = entityCenter.subtract(blockCenter).normalize();

        List<RankedFace> rankedFaces = new ArrayList<>();

        for (Direction dir : blockExposedFaces.directions())
        {
            Vec3 faceNormal = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            float dot = (float) faceNormal.dot(toEntity);

            if (dot > 0)
            {
                Vec3 faceCenter = blockCenter.add(dir.getStepX() * 0.5, dir.getStepY() * 0.5, dir.getStepZ() * 0.5);

                rankedFaces.add(new RankedFace(faceCenter, dot));
            }
        }

        rankedFaces.sort((a, b) -> Float.compare(b.weight, a.weight));

        return rankedFaces.stream().limit(2).toList();
    }

    private float calcTemp(BlockPos pos, float temp, Vec3 entityPos)
    {
        float distance = (float) entityPos.distanceTo(Vec3.atCenterOf(pos));
        float effectiveDistance = Math.max(distance, 1f);

        return (temp) / (effectiveDistance * effectiveDistance) * 0.15f;
    }

    private float exposure(Level level, Entity entity, BlockPos sourcePos)
    {
        Vec3 sourcePosVec = Vec3.atCenterOf(sourcePos);
        float distance = (float) entity.position().distanceTo(sourcePosVec);
        float searchRadius = (float) ServerConfig.SEARCH_RANGE.getAsInt();
        float totalExposure = 0f;

        AABB entityBB = entity.getBoundingBox();
        Vec3 entityCenter = new Vec3((entityBB.minX + entityBB.maxX) * 0.5, (entityBB.minY + entityBB.maxY) * 0.5, (entityBB.minZ + entityBB.maxZ) * 0.5);
        List<RankedFace> bestFaces = getBestFaces(entityCenter, sourcePos);

        if (distance <= searchRadius * 0.25)
        {
            Vec3[] samplePoints = {new Vec3(entityBB.minX, entityCenter.y, entityBB.minZ), new Vec3(entityBB.maxX, entityCenter.y, entityBB.minZ), new Vec3(entityBB.minX, entityCenter.y, entityBB.maxZ), new Vec3(entityBB.maxX, entityCenter.y, entityBB.maxZ), new Vec3(entityCenter.x, entityBB.maxY, entityCenter.z), new Vec3(entityCenter.x, entityBB.minY, entityCenter.z)};

            for (RankedFace face : bestFaces)
            {
                for (Vec3 samplePoint : samplePoints)
                {
                    ClipContext context = new ClipContext(samplePoint, face.faceCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, CollisionContext.empty());
                    BlockHitResult hit = level.clip(context);

                    Block hitBlock = level.getBlockState(hit.getBlockPos()).getBlock();

                    if (hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(sourcePos)) totalExposure += face.weight;
                }
            }

            totalExposure /= samplePoints.length;
        }
        else if (distance <= searchRadius * 0.5)
        {
            Vec3[] samplePoints = {new Vec3(entityCenter.x, entityBB.minY, entityCenter.z), new Vec3(entityCenter.x, entityCenter.y, entityCenter.z), new Vec3(entityCenter.x, entityBB.maxY, entityCenter.z)};

            for (RankedFace face : bestFaces)
            {
                for (Vec3 samplePoint : samplePoints)
                {
                    ClipContext context = new ClipContext(samplePoint, face.faceCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, CollisionContext.empty());
                    BlockHitResult hit = level.clip(context);

                    if (hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(sourcePos)) totalExposure += face.weight;
                }
            }

            totalExposure /= samplePoints.length;
        }
        else
        {
            Vec3 entityEyeline = entity.getEyePosition();

            ClipContext context = new ClipContext(entityEyeline, sourcePosVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, CollisionContext.empty());
            BlockHitResult hit = level.clip(context);

            if (hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(sourcePos)) totalExposure = 1f;
        }

        return totalExposure;
    }

    private float parseBlocks(Level curLevel, Entity entity)
    {
        float totalBlockTemp = 0f;
        Vec3 entityPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);

        for (Map.Entry<Block, List<BlockPos>> entry : this.allPositions.entrySet())
        {
            Block block = entry.getKey();
            BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (dataMap == null || !dataMap.isRadiative()) continue;

            float totalTempForBlock = 0f;
            for (BlockPos pos : entry.getValue())
            {
                if (!curLevel.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

                float exposure = exposure(curLevel, entity, pos);
                BlockState state = curLevel.getBlockState(pos);

                if (dataMap.isRadiative()) totalTempForBlock += (calcTemp(pos, parseBlockState(curLevel, pos, dataMap), entityPos) * exposure);
            }

            totalBlockTemp += dataMap.temperature() == 0f ? totalTempForBlock : Math.min(totalTempForBlock, dataMap.temperature());
        }

        return totalBlockTemp;
    }

    private float parseFluid(Level curLevel, Entity entity)
    {
        float totalFluidTemp = 0f;
        Vec3 entityPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);

        for (Map.Entry<Fluid, List<BlockPos>> entry : allFluidPositions.entrySet())
        {
            Fluid fluid = entry.getKey();
            FluidTemperatureDataMap dataMap = BuiltInRegistries.FLUID.wrapAsHolder(fluid).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (dataMap == null || !dataMap.isRadiative()) continue;

            float totalTempForFluid = 0f;
            for (BlockPos pos : entry.getValue())
            {
                if (!curLevel.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

                BlockState state = curLevel.getBlockState(pos);
                Block block = state.getBlock();
                BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
                float blockTemp = blockDataMap != null ? parseBlockState(curLevel, pos, blockDataMap) : 0f;
                float exposure = exposure(curLevel, entity, pos);

                if (dataMap.isRadiative()) totalTempForFluid += (calcTemp(pos, blockTemp != 0f ? blockTemp : dataMap.temperature(), entityPos) * exposure);
            }

            totalFluidTemp += Math.min(totalTempForFluid, dataMap.temperature());
        }

        return totalFluidTemp;
    }

    private float getFluidImmersionTemperature(Level curLevel, Entity entity)
    {
        float totalImmersionTemp = 0f;

        for (Map.Entry<Fluid, List<BlockPos>> entry : this.allFluidPositions.entrySet())
        {
            Fluid fluid = entry.getKey();
            FluidTemperatureDataMap dataMap = BuiltInRegistries.FLUID.wrapAsHolder(fluid).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (dataMap == null) continue;

            float fluidHeight = (float) entity.getFluidTypeHeight(fluid.getFluidType());
            float baseImmersion = Math.min(fluidHeight / entity.getBbHeight(), 1f);

            if (baseImmersion >= 1.0f)
            {
                int maxDepthCheck = ServerConfig.MAX_FLUID_DEPTH_CHECK.getAsInt();
                BlockPos entityPos = entity.blockPosition();
                Fluid entityFluid = curLevel.getFluidState(entityPos).getType();

                int fluidBlocksAbove = 0;

                for (BlockPos pos : BlockPos.betweenClosed(entityPos, entityPos.above(maxDepthCheck)))
                {
                    FluidState fluidState = curLevel.getFluidState(pos);

                    if (fluidState.getType().equals(entityFluid) && !fluidState.isEmpty())
                    {
                        fluidBlocksAbove++;
                        continue;
                    }
                    else break;
                }

                float depthMultiplier = 1f + (fluidBlocksAbove / (float) maxDepthCheck);

                totalImmersionTemp += dataMap.temperature() * depthMultiplier;
            }
            else totalImmersionTemp = dataMap.temperature() * baseImmersion;
        }

        return totalImmersionTemp;
    }

    private float getContactTemperature(Level curLevel, Entity entity)
    {
        AABB bb = entity.getBoundingBox().inflate(0.001);
        float totalContactTemp = 0f;
        int contactCount = 0;

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        int minX = Mth.floor(bb.minX);
        int minY = Mth.floor(bb.minY);
        int minZ = Mth.floor(bb.minZ);
        int maxX = Mth.floor(bb.maxX);
        int maxY = Mth.floor(bb.maxY);
        int maxZ = Mth.floor(bb.maxZ);

        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++)
            {
                for (int z = minZ; z <= maxZ; z ++)
                {
                    mutableBlockPos.set(x, y, z);

                    if (!curLevel.hasChunkAt(mutableBlockPos)) continue;

                    BlockState state = curLevel.getBlockState(mutableBlockPos);
                    BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(state.getBlock()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
                    if (dataMap == null || dataMap.isRadiative()) continue;

                    float blockTemp = parseBlockState(curLevel, mutableBlockPos, dataMap);
                    totalContactTemp += blockTemp;
                    contactCount++;
                }
            }
        }

        if (contactCount > 0) return totalContactTemp;
        return 0f;
    }

    public static BlockSearchResult createDefault() {return new BlockSearchResult(Vec3.ZERO, Level.OVERWORLD, new HashMap<>(), new HashMap<>(), new HashMap<>());}

    public BlockSearchResult withSearchOrigin(Vec3 searchOrigin) {return new BlockSearchResult(searchOrigin, this.levelID, this.allPositions, this.allFluidPositions, this.exposedFaces);}

    public BlockSearchResult withLevelID(ResourceKey<Level> levelID) {return new BlockSearchResult(this.searchOrigin, levelID, this.allPositions, this.allFluidPositions, this.exposedFaces);}

    public BlockSearchResult withAllPositions(Map<Block, List<BlockPos>> allPositions) {return new BlockSearchResult(this.searchOrigin, this.levelID, allPositions, this.allFluidPositions, this.exposedFaces);}

    public BlockSearchResult withAllFluidPositions(Map<Fluid, List<BlockPos>> allFluidPositions) {return new BlockSearchResult(this.searchOrigin, this.levelID, this.allPositions, allFluidPositions, this.exposedFaces);}

    public BlockSearchResult withExposedFaces(Map<BlockPos, ExposedFaces> exposedFaces) {return new BlockSearchResult(this.searchOrigin, this.levelID, this.allPositions, this.allFluidPositions, exposedFaces);}

    public BlockPos getNearest(Entity entity, boolean isHot)
    {
        float closestDistance = 0f;
        BlockPos closestPos = BlockPos.ZERO;
        Vec3 entityPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);

        for (Map.Entry<Block, List<BlockPos>> entry : this.allPositions.entrySet())
        {
            BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (blockDataMap == null) continue;

            for (BlockPos pos : entry.getValue())
            {
                float distance = (float) entityPos.distanceTo(Vec3.atCenterOf(pos));

                if (distance < closestDistance)
                {
                    if (isHot && blockDataMap.temperature() >= 0f)
                    {
                        closestDistance = distance;
                        closestPos = pos.immutable();
                    }
                    else if (!isHot && blockDataMap.temperature() < 0f)
                    {
                        closestDistance = distance;
                        closestPos = pos.immutable();
                    }
                }
            }
        }

        for (Map.Entry<Fluid, List<BlockPos>> entry : this.allFluidPositions.entrySet())
        {
            FluidTemperatureDataMap fluidDataMap = BuiltInRegistries.FLUID.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (fluidDataMap == null) continue;

            for (BlockPos pos : entry.getValue())
            {
                float distance = (float) entityPos.distanceTo(Vec3.atCenterOf(pos));

                if (distance < closestDistance)
                {
                    if (isHot && fluidDataMap.temperature() >= 0f)
                    {
                        closestDistance = distance;
                        closestPos = pos.immutable();
                    }
                    else if (!isHot && fluidDataMap.temperature() < 0f)
                    {
                        closestDistance = distance;
                        closestPos = pos.immutable();
                    }
                }
            }
        }

        return closestPos;
    }

    public float getRadiance(Level curLevel, Entity entity)
    {
        if (this.levelID != curLevel.dimension()) return 0f;

        float maxValue = (float) ServerConfig.PEAK_BLOCK_TEMPERATURE.getAsInt();
        float blockTemp = parseBlocks(curLevel, entity);
        float fluidTemp = parseFluid(curLevel, entity);

        float totalTemperature = blockTemp + fluidTemp;

//        float maxRadiance = (float) ServerConfig.PEAK_BLOCK_TEMPERATURE.getAsInt();
//        return maxRadiance * (1f - (float) Math.exp(-totalTemperature / maxRadiance));

        return Mth.clamp(totalTemperature, -maxValue, maxValue);
    }

    public float getImmersion(Level curLevel, Entity entity)
    {
        if (this.levelID != curLevel.dimension()) return 0f;

        float maxValue = (float) ServerConfig.PEAK_BLOCK_TEMPERATURE.getAsInt();
        float fluidImmersion = getFluidImmersionTemperature(curLevel, entity);

        return Mth.clamp(fluidImmersion, -maxValue, maxValue);
    }

    public float getContact(Level curLevel, Entity entity, boolean isMob)
    {
        if (this.levelID != curLevel.dimension()) return 0f;

        float maxValue = (float) ServerConfig.PEAK_BLOCK_TEMPERATURE.getAsInt();
        float contactTemp = isMob ? 0f : getContactTemperature(curLevel, entity);

        return Mth.clamp(contactTemp, -maxValue, maxValue);
    }

    public static float parseBlockState(Level level, BlockPos pos, BlockTemperatureDataMap dataMap)
    {
//        StateDefinition<Block, BlockState> stateDef = state.getBlock().getStateDefinition();
//        float temp = dataMap.temperature();
//
//        BlockEntity blockEntity = level.getBlockEntity(pos);
//        if (blockEntity instanceof IHeatable heatable) return heatable.getTemperature();
//        if (blockEntity instanceof CharcoalForgeBlockEntity charcoalForge) return charcoalForge.getTemperature();
//        if (blockEntity instanceof PitKilnBlockEntity pitKiln) return pitKiln.isLit() ? dataMap.temperature() : 0.0f;
//
//        for (Map.Entry<String, Boolean> entry : dataMap.stateBools().entrySet())
//        {
//            Property<?> property = stateDef.getProperty(entry.getKey());
//
//            if (property != null)
//            {
//                Comparable<?> value = state.getValue(property);
//                if (!value.toString().equals(entry.getValue().toString())) return 0f;
//            }
//            else LOGGER.error("Property of {} was not found for block {}", entry.getKey(), state.getBlock().getDescriptionId());
//        }

        return dataMap.resolveForState(level, pos);
    }
}