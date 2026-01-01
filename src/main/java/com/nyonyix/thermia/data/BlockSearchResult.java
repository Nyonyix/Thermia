package com.nyonyix.thermia.data;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.ServerConfig;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public record BlockSearchResult(
        Vec3 searchOrigin,
        ResourceKey<Level> levelID,
        Map<Block, List<BlockPos>> allPositions,
        Map<Fluid, List<BlockPos>> allFluidPositions,
        Map<BlockPos, Float> blockExposures,
        Map<BlockPos, Float> fluidExposures
)
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private record ExposureEntry(BlockPos pos, float exposure)
    {
        public static final Codec<ExposureEntry> CODEC = RecordCodecBuilder.create(exposureEntryInstance -> exposureEntryInstance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(ExposureEntry::pos),
                Codec.FLOAT.fieldOf("exposure").forGetter(ExposureEntry::exposure)
        ).apply(exposureEntryInstance, ExposureEntry::new));
    }

    private static final Codec<Map<BlockPos, Float>> BLOCK_EXPOSURE_CODEC = Codec.list(ExposureEntry.CODEC).xmap(
            list -> list.stream().collect(Collectors.toMap(ExposureEntry::pos, ExposureEntry::exposure, (a, b) -> b, HashMap::new)),
            map -> map.entrySet().stream().map(e -> new ExposureEntry(e.getKey(), e.getValue())).toList()
    );

    public static final Codec<BlockSearchResult> CODEC = RecordCodecBuilder.create(blockSearchResultInstance -> blockSearchResultInstance.group(
            Vec3.CODEC.fieldOf("search_origin").forGetter(BlockSearchResult::searchOrigin),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("level_id").forGetter(BlockSearchResult::levelID),
            Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), Codec.list(BlockPos.CODEC)).fieldOf("all_positions").forGetter(BlockSearchResult::allPositions),
            Codec.unboundedMap(BuiltInRegistries.FLUID.byNameCodec(), Codec.list(BlockPos.CODEC)).fieldOf("all_fluid_positions").forGetter(BlockSearchResult::allFluidPositions),
            BLOCK_EXPOSURE_CODEC.fieldOf("block_exposures").forGetter(BlockSearchResult::blockExposures),
            BLOCK_EXPOSURE_CODEC.fieldOf("fluid_exposures").forGetter(BlockSearchResult::fluidExposures)
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

                buf.writeInt(result.blockExposures.size());
                for (Map.Entry<BlockPos, Float> entry : result.blockExposures.entrySet())
                {
                    BlockPos.STREAM_CODEC.encode(buf, entry.getKey());
                    buf.writeFloat(entry.getValue());
                }

                buf.writeInt(result.fluidExposures.size());
                for (Map.Entry<BlockPos, Float> entry : result.fluidExposures.entrySet())
                {
                    BlockPos.STREAM_CODEC.encode(buf, entry.getKey());
                    buf.writeFloat(entry.getValue());
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

                int blockExposureSize = buf.readInt();
                Map<BlockPos, Float> blockExposure = new HashMap<>();
                for (int i = 0; i < blockExposureSize; i++)
                {
                    blockExposure.put(BlockPos.STREAM_CODEC.decode(buf), buf.readFloat());
                }

                int fluidExposureSize = buf.readInt();
                Map<BlockPos, Float> fluidExposure = new HashMap<>();
                for (int i = 0; i < fluidExposureSize; i++)
                {
                    fluidExposure.put(BlockPos.STREAM_CODEC.decode(buf), buf.readFloat());
                }

                return new BlockSearchResult(searchOrigin, levelID, allPositions, allFluidPositions, blockExposure, fluidExposure);
            }
    );

    private float parseBlockState(BlockState state, Level level, BlockPos pos, BlockTemperatureDataMap dataMap)
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

    private float calcTemp(BlockPos pos, float temp, Map<BlockPos, Float> exposureMap)
    {
        float distance = (float) this.searchOrigin.distanceTo(Vec3.atCenterOf(pos));
        float effectiveDistance = Math.max(distance, 1f);
        float exposureFactor = exposureMap.getOrDefault(pos, 1.0f);

        return (temp * exposureFactor) / (effectiveDistance * effectiveDistance);

    }

    private float parseBlocks(Level curLevel)
    {
        float totalBlockTemp = 0f;

        for (Map.Entry<Block, List<BlockPos>> entry : this.allPositions.entrySet())
        {
            Block block = entry.getKey();
            BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
            if (dataMap == null) continue;

            for (BlockPos pos : entry.getValue())
            {
                if (!curLevel.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

                BlockState state = curLevel.getBlockState(pos);

                totalBlockTemp += calcTemp(pos, parseBlockState(state, curLevel, pos, dataMap), this.blockExposures);
            }
        }

        return totalBlockTemp;
    }

    private float parseFluid(Level curLevel)
    {
        float totalFluidTemp = 0f;

        for (Map.Entry<Fluid, List<BlockPos>> entry : allFluidPositions.entrySet())
        {
            Fluid fluid = entry.getKey();
            FluidTemperatureDataMap dataMap = BuiltInRegistries.FLUID.wrapAsHolder(fluid).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
            if (dataMap == null) continue;

            for (BlockPos pos : entry.getValue())
            {
                if (!curLevel.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;

                BlockState state = curLevel.getBlockState(pos);
                Block block = state.getBlock();
                BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);

                float blockTemp = blockDataMap != null ? parseBlockState(state, curLevel, pos, blockDataMap) : 0f;

                totalFluidTemp += calcTemp(pos, blockTemp != 0f ? blockTemp : dataMap.temperature(), this.fluidExposures);
            }
        }

        return totalFluidTemp;
    }

    public static BlockSearchResult createDefault() {return new BlockSearchResult(Vec3.ZERO, Level.OVERWORLD, new HashMap<>(), new HashMap<>(),  new HashMap<>(), new HashMap<>());}

    public BlockSearchResult withSearchOrigin(Vec3 searchOrigin) {return new BlockSearchResult(searchOrigin, this.levelID, this.allPositions, this.allFluidPositions, this.blockExposures, this.fluidExposures);}

    public BlockSearchResult withLevelID(ResourceKey<Level> levelID) {return new BlockSearchResult(this.searchOrigin, levelID, this.allPositions, this.allFluidPositions, this.blockExposures, this.fluidExposures);}

    public BlockSearchResult withAllPositions(Map<Block, List<BlockPos>> allPositions) {return new BlockSearchResult(this.searchOrigin, this.levelID, allPositions, this.allFluidPositions, this.blockExposures, this.fluidExposures);}

    public BlockSearchResult withAllFluidPositions(Map<Fluid, List<BlockPos>> allFluidPositions) {return new BlockSearchResult(this.searchOrigin, this.levelID, this.allPositions, allFluidPositions, this.blockExposures,  this.fluidExposures);}

    public BlockSearchResult withBlockExposures(Map<BlockPos, Float> blockExposures) {return new BlockSearchResult(this.searchOrigin, this.levelID, this.allPositions, this.allFluidPositions, blockExposures, this.fluidExposures);}

    public BlockSearchResult withFluidExposure(Map<BlockPos, Float> fluidExposures) {return new BlockSearchResult(this.searchOrigin, this.levelID, this.allPositions, this.allFluidPositions, this.blockExposures, fluidExposures);}

    public BlockPos getNearest()
    {
        return BlockPos.ZERO;
    }

    public float parseBlockSearchResult(Level curLevel)
    {
        if (this.levelID != curLevel.dimension()) return 0f;

        float blockTemp = parseBlocks(curLevel);
        float fluidTemp = parseFluid(curLevel);

        float totalTemperature = blockTemp + fluidTemp;

        float maxRadiance = (float) ServerConfig.MAX_RADIANT_HEATING.getAsInt();
        return maxRadiance * (1f - (float) Math.exp(-totalTemperature / maxRadiance));
    }
}