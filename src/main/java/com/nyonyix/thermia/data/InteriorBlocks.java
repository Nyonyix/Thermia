package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.Map;

public final class InteriorBlocks
{
    public final Long2ObjectOpenHashMap<Block> edgeBlocks;
    public final Long2ObjectOpenHashMap<EdgeBlockData> edgeBlockData;
    public final Long2ObjectOpenHashMap<Block> heatSourceBlocks;
    public final Long2ObjectOpenHashMap<Fluid> heatSourceFluids;
    public final Long2ObjectOpenHashMap<Block> heatSinkBlocks;
    public final Long2ObjectOpenHashMap<Fluid> heatSinkFluids;

    private static Long2ObjectOpenHashMap<Block> toPackedMap(Map<BlockPos, Block> source)
    {
        Long2ObjectOpenHashMap<Block> map = new Long2ObjectOpenHashMap<>(source.size());
        source.forEach((pos, block) -> map.put(pos.asLong(), block));
        return map;
    }

    private static Long2ObjectOpenHashMap<Fluid> toPackedFluidMap(Map<BlockPos, Fluid> source)
    {
        Long2ObjectOpenHashMap<Fluid> map = new Long2ObjectOpenHashMap<>(source.size());
        source.forEach((pos, fluid) -> map.put(pos.asLong(), fluid));
        return map;
    }

    private static Long2ObjectOpenHashMap<EdgeBlockData> toPackedEdgeDataMap(Map<BlockPos, EdgeBlockData> source)
    {
        Long2ObjectOpenHashMap<EdgeBlockData> map = new Long2ObjectOpenHashMap<>(source.size());
        source.forEach((pos, data) -> map.put(pos.asLong(), data));
        return map;
    }

    private <V> Map<BlockPos, V> toLongMap(Long2ObjectOpenHashMap<V> source)
    {
        Map<BlockPos, V> map = new java.util.HashMap<>(source.size());
        source.forEach((k, v) -> map.put(BlockPos.of(k), v));
        return map;
    }

    private <V> Map<BlockPos, V> toFluidLongMap(Long2ObjectOpenHashMap<V> source)
    {
        return toLongMap(source);
    }

    private static final Codec<BlockPos> BLOCKPOS_STRING_CODEC = Codec.STRING.flatXmap(
            str -> DataResult.success(BlockPos.of(Long.parseLong(str))),
            pos -> DataResult.success(String.valueOf(pos.asLong()))
    );

    private record SerialForm(
            Map<BlockPos, Block> edgeBlocks,
            Map<BlockPos, EdgeBlockData> edgeBlockData,
            Map<BlockPos, Block> heatSourceBlocks,
            Map<BlockPos, Fluid> heatSourceFluids,
            Map<BlockPos, Block> heatSinkBlocks,
            Map<BlockPos, Fluid> heatSinkFluids
    ) {}

    private static final Codec<SerialForm> SERIAL_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("edge_blocks").forGetter(SerialForm::edgeBlocks),
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, EdgeBlockData.CODEC).fieldOf("edge_blocks_data").forGetter(SerialForm::edgeBlockData),
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("heat_source_blocks").forGetter(SerialForm::heatSourceBlocks),
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, BuiltInRegistries.FLUID.byNameCodec()).fieldOf("heat_source_fluids").forGetter(SerialForm::heatSourceFluids),
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("heat_sink_blocks").forGetter(SerialForm::heatSinkBlocks),
            Codec.unboundedMap(BLOCKPOS_STRING_CODEC, BuiltInRegistries.FLUID.byNameCodec()).fieldOf("heat_sink_fluids").forGetter(SerialForm::heatSinkFluids)
    ).apply(i, SerialForm::new));

    public record EdgeBlockData(Direction outwardFace, boolean isExterior)
    {
        public static final Codec<EdgeBlockData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Direction.CODEC.fieldOf("outward_face").forGetter(EdgeBlockData::outwardFace),
                Codec.BOOL.fieldOf("is_exterior").forGetter(EdgeBlockData::isExterior)
        ).apply(i, EdgeBlockData::new));
    }

    public static final Codec<InteriorBlocks> CODEC = SERIAL_CODEC.xmap(
            s -> new InteriorBlocks(s.edgeBlocks(), s.edgeBlockData, s.heatSourceBlocks(), s.heatSourceFluids(), s.heatSinkBlocks(), s.heatSinkFluids()),
            ib -> new SerialForm(ib.toLongMap(ib.edgeBlocks), ib.toLongMap(ib.edgeBlockData), ib.toLongMap(ib.heatSourceBlocks), ib.toFluidLongMap(ib.heatSourceFluids), ib.toLongMap(ib.heatSinkBlocks), ib.toFluidLongMap(ib.heatSinkFluids))
    );

    public InteriorBlocks(Map<BlockPos, Block> edgeBlocks, Map<BlockPos, EdgeBlockData> edgeBlockData, Map<BlockPos, Block> heatSourceBlocks,
                          Map<BlockPos, Fluid> heatSourceFluids, Map<BlockPos, Block> heatSinkBlocks,
                          Map<BlockPos, Fluid> heatSinkFluids)
    {
        this.edgeBlocks = toPackedMap(edgeBlocks);
        this.edgeBlockData = toPackedEdgeDataMap(edgeBlockData);
        this.heatSourceBlocks = toPackedMap(heatSourceBlocks);
        this.heatSourceFluids = toPackedFluidMap(heatSourceFluids);
        this.heatSinkBlocks = toPackedMap(heatSinkBlocks);
        this.heatSinkFluids = toPackedFluidMap(heatSinkFluids);
    }

    public InteriorBlocks(Long2ObjectOpenHashMap<Block> edgeBlocks, Long2ObjectOpenHashMap<EdgeBlockData> edgeBlockData, Long2ObjectOpenHashMap<Block> heatSourceBlocks,
                          Long2ObjectOpenHashMap<Fluid> heatSourceFluids, Long2ObjectOpenHashMap<Block> heatSinkBlocks,
                          Long2ObjectOpenHashMap<Fluid> heatSinkFluids)
    {
        this.edgeBlocks = edgeBlocks;
        this.edgeBlockData = edgeBlockData;
        this.heatSourceBlocks = heatSourceBlocks;
        this.heatSourceFluids = heatSourceFluids;
        this.heatSinkBlocks = heatSinkBlocks;
        this.heatSinkFluids = heatSinkFluids;
    }

    public static InteriorBlocks createDefault() {return new InteriorBlocks(new Long2ObjectOpenHashMap<>(), new Long2ObjectOpenHashMap<>(), new Long2ObjectOpenHashMap<>(), new Long2ObjectOpenHashMap<>(), new Long2ObjectOpenHashMap<>(), new Long2ObjectOpenHashMap<>());}

    public boolean isEdge(long packedPos) { return edgeBlocks.containsKey(packedPos); }
    public boolean isHeatSource(long packedPos) { return heatSourceBlocks.containsKey(packedPos) || heatSourceFluids.containsKey(packedPos); }
    public boolean isHeatSink(long packedPos) { return heatSinkBlocks.containsKey(packedPos) || heatSinkFluids.containsKey(packedPos); }
}