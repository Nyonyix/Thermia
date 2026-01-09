package com.nyonyix.thermia.data;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.attachment.BlockTemperature;
import com.nyonyix.thermia.data.map.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.map.FluidTemperatureDataMap;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import net.dries007.tfc.common.blockentities.CharcoalForgeBlockEntity;
import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.blockentities.PitKilnBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.slf4j.Logger;

import javax.swing.plaf.BorderUIResource;
import java.util.*;
import java.util.stream.Collectors;

public record BlockSearchResult(
        Vec3 searchOrigin,
        ResourceKey<Level> levelID,
        Map<Block, List<BlockPos>> allPositions,
        Map<Fluid, List<BlockPos>> allFluidPositions
)
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<BlockSearchResult> CODEC = RecordCodecBuilder.create(blockSearchResultInstance -> blockSearchResultInstance.group(
            Vec3.CODEC.fieldOf("search_origin").forGetter(BlockSearchResult::searchOrigin),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("level_id").forGetter(BlockSearchResult::levelID),
            Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), Codec.list(BlockPos.CODEC)).fieldOf("all_positions").forGetter(BlockSearchResult::allPositions),
            Codec.unboundedMap(BuiltInRegistries.FLUID.byNameCodec(), Codec.list(BlockPos.CODEC)).fieldOf("all_fluid_positions").forGetter(BlockSearchResult::allFluidPositions)
    ).apply(blockSearchResultInstance, BlockSearchResult::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC_3_STREAM_CODEC = StreamCodec.of(
            (buf, vec) -> {
             buf.writeDouble(vec.x);
             buf.writeDouble(vec.y);
             buf.writeDouble(vec.z);
            }, (buf) -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockSearchResult> STREAM_CODEC = StreamCodec.of(
            (buf, result) ->
            {
                VEC_3_STREAM_CODEC.encode(buf, result.searchOrigin);
                buf.writeResourceKey(result.levelID);

                buf.writeInt(result.allPositions.size());
                for (Map.Entry<Block, List<BlockPos>> entry : result.allPositions.entrySet())
                {
                    buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(entry.getKey()));
                    buf.writeInt(entry.getValue().size());
                    for (BlockPos pos : entry.getValue())
                    {
                        BlockPos.STREAM_CODEC.encode(buf, pos);
                    }
                }

                buf.writeInt(result.allFluidPositions.size());
                for (Map.Entry<Fluid, List<BlockPos>> entry : result.allFluidPositions.entrySet())
                {
                    buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(entry.getKey()));
                    buf.writeInt(entry.getValue().size());
                    for (BlockPos pos : entry.getValue())
                    {
                        BlockPos.STREAM_CODEC.encode(buf, pos);
                    }
                }
            },
            (buf) ->
            {
                Vec3 searchOrigin = VEC_3_STREAM_CODEC.decode(buf);
                ResourceKey<Level> levelID = buf.readResourceKey(Registries.DIMENSION);

                int allPositionSize = buf.readInt();
                Map<Block, List<BlockPos>> allPositions = new HashMap<>();
                for (int i = 0; i < allPositionSize; i++)
                {
                    Block block = BuiltInRegistries.BLOCK.get(buf.readResourceLocation());
                    int listSize = buf.readInt();
                    List<BlockPos> positions = new ArrayList<>();
                    for (int o = 0; o < listSize; o++)
                    {
                        positions.add(BlockPos.STREAM_CODEC.decode(buf));
                    }
                    allPositions.put(block, positions);
                }

                int allFluidPositionSize = buf.readInt();
                Map<Fluid, List<BlockPos>> allFluidPositions = new HashMap<>();
                for (int i = 0; i < allFluidPositionSize; i++)
                {
                    Fluid fluid = BuiltInRegistries.FLUID.get(buf.readResourceLocation());
                    int listSize = buf.readInt();
                    List<BlockPos> positions = new ArrayList<>();
                    for (int o = 0; o < listSize; o++)
                    {
                        positions.add(BlockPos.STREAM_CODEC.decode(buf));
                    }
                    allFluidPositions.put(fluid, positions);
                }

                return new BlockSearchResult(searchOrigin, levelID, allPositions, allFluidPositions);
            }
    );

    private float calcTemp(BlockPos pos, float temp, Vec3 entityPos)
    {
        float distance = (float) entityPos.distanceTo(Vec3.atCenterOf(pos));
        float effectiveDistance = Math.max(distance, 1f);

        return (temp) / (effectiveDistance * effectiveDistance) * 0.15f;

    }

    private boolean getIsExposed(Level level, Vec3 entityPos, BlockPos sourcePos)
    {
        Vec3 sourcePosVec = Vec3.atCenterOf(sourcePos);
        float distance = (float) entityPos.distanceTo(sourcePosVec);

        if (distance < 1.5f || distance > 5) return true;

        ClipContext context = new ClipContext(entityPos, sourcePosVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, CollisionContext.empty());
        BlockHitResult hit = level.clip(context);

        if (hit.getType() != HitResult.Type.MISS && !hit.getBlockPos().equals(sourcePos)) return false;

        return true;
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

                boolean isExposed = getIsExposed(curLevel, entityPos, pos);
                BlockState state = curLevel.getBlockState(pos);

                if (isExposed && dataMap.isRadiative()) totalTempForBlock += calcTemp(pos, parseBlockState(state, curLevel, pos, dataMap), entityPos);
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
                float blockTemp = blockDataMap != null ? parseBlockState(state, curLevel, pos, blockDataMap) : 0f;
                boolean isExposed = getIsExposed(curLevel, entityPos, pos);

                if (isExposed && dataMap.isRadiative()) totalTempForFluid += calcTemp(pos, blockTemp != 0f ? blockTemp : dataMap.temperature(), entityPos);
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
            float immersion = Math.min(fluidHeight / entity.getBbHeight(), 1f);

            totalImmersionTemp = dataMap.temperature() * immersion;
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

                    float blockTemp = parseBlockState(state, curLevel, mutableBlockPos, dataMap);
                    totalContactTemp += blockTemp;
                    contactCount++;
                }
            }
        }

        if (contactCount > 0)
        {
            float contactMultiplier = 1f + (float) Math.log1p(contactCount) * 0.3f;
            return totalContactTemp * contactMultiplier;
        }
        return 0f;
    }

    public static BlockSearchResult createDefault() {return new BlockSearchResult(Vec3.ZERO, Level.OVERWORLD, new HashMap<>(), new HashMap<>());}

    public BlockSearchResult withSearchOrigin(Vec3 searchOrigin) {return new BlockSearchResult(searchOrigin, this.levelID, this.allPositions, this.allFluidPositions);}

    public BlockSearchResult withLevelID(ResourceKey<Level> levelID) {return new BlockSearchResult(this.searchOrigin, levelID, this.allPositions, this.allFluidPositions);}

    public BlockSearchResult withAllPositions(Map<Block, List<BlockPos>> allPositions) {return new BlockSearchResult(this.searchOrigin, this.levelID, allPositions, this.allFluidPositions);}

    public BlockSearchResult withAllFluidPositions(Map<Fluid, List<BlockPos>> allFluidPositions) {return new BlockSearchResult(this.searchOrigin, this.levelID, this.allPositions, allFluidPositions);}

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

    public float parseBlockSearchResult(Level curLevel, Entity entity, boolean isMob)
    {
        if (this.levelID != curLevel.dimension()) return 0f;

        float blockTemp = parseBlocks(curLevel, entity);
        float fluidTemp = parseFluid(curLevel, entity);
        float fluidImmersion = getFluidImmersionTemperature(curLevel, entity);
        float contactTemp = isMob ? 0f : getContactTemperature(curLevel, entity);

        float totalTemperature = blockTemp + fluidTemp + fluidImmersion + contactTemp;

        float maxRadiance = (float) ServerConfig.MAX_RADIANT_HEATING.getAsInt();
        return maxRadiance * (1f - (float) Math.exp(-totalTemperature / maxRadiance));
    }

    public static float parseBlockState(BlockState state, Level level, BlockPos pos, BlockTemperatureDataMap dataMap)
    {
        StateDefinition<Block, BlockState> stateDef = state.getBlock().getStateDefinition();
        float temp = dataMap.temperature();

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IHeatable heatable) return heatable.getTemperature();
        if (blockEntity instanceof CharcoalForgeBlockEntity charcoalForge) return charcoalForge.getTemperature();
        if (blockEntity instanceof PitKilnBlockEntity pitKiln) return pitKiln.isLit() ? dataMap.temperature() : 0.0f;

        for (Map.Entry<String, Boolean> entry : dataMap.stateBools().entrySet())
        {
            Property<?> property = stateDef.getProperty(entry.getKey());

            if (property != null)
            {
                Comparable<?> value = state.getValue(property);
                if (!value.toString().equals(entry.getValue().toString())) return 0f;
            }
            else LOGGER.error("Property of {} was not found for block {}", entry.getKey(), state.getBlock().getDescriptionId());
        }

        return temp;
    }
}