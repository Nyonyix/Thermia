package com.nyonyix.thermia.data.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record ClientInterior(
        BlockPos homePos,
        AABB boundingBox,
        boolean isValid,
        BlockPos origin,
        int sizeX,
        int sizeY,
        int sizeZ,
        long[] membership,
        float internalHumidity,
        float externalHumidity,
        float internalTemperature,
        float externalTemperature,
        float porosity,
        float externalPull,
        float sourcePull,
        InteriorCounts counts
)
{
    private static final Codec<long[]> MEMBERSHIP_CODEC = Codec.LONG.listOf().xmap(ClientInterior::toLongArray, ClientInterior::toLongList);

    private static long[] toLongArray(List<Long>list)
    {
        long[] out = new long[list.size()];
        for (int i =0; i < out.length; i++) out[i] = list.get(i);

        return out;
    }

    private static List<Long> toLongList(long[] arr)
    {
        LongArrayList list = new LongArrayList(arr.length);
        for (long l : arr) list.add(l);

        return list;
    }

    public record InteriorCounts(int volume, int area, int sourceBlocks, int sourceFluids, int sinkBlocks, int sinkFluids)
    {
        public static final Codec<InteriorCounts> CODEC = RecordCodecBuilder.create( i -> i.group(
                Codec.INT.fieldOf("volume").forGetter(InteriorCounts::volume),
                Codec.INT.fieldOf("area").forGetter(InteriorCounts::area),
                Codec.INT.fieldOf("source_blocks").forGetter(InteriorCounts::sourceBlocks),
                Codec.INT.fieldOf("source_fluids").forGetter(InteriorCounts::sourceFluids),
                Codec.INT.fieldOf("sink_blocks").forGetter(InteriorCounts::sinkBlocks),
                Codec.INT.fieldOf("sink_fluids").forGetter(InteriorCounts::sinkFluids)
        ).apply(i, InteriorCounts::new));

        public static InteriorCounts createDefault() {return new InteriorCounts(0, 0, 0, 0, 0, 0);}

        public static InteriorCounts from(Interior i) {return new InteriorCounts(i.internalAirBlocks().size(), i.interiorBlocks().edgeBlocks.size(), i.interiorBlocks().heatSourceBlocks.size(), i.interiorBlocks().heatSourceFluids.size(), i.interiorBlocks().heatSinkBlocks.size(), i.interiorBlocks().heatSinkFluids.size());}
    }

    public static final Codec<ClientInterior> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("home_pos").forGetter(ClientInterior::homePos),
            Interior.AABB_CODEC.fieldOf("bounding_boc").forGetter(ClientInterior::boundingBox),
            Codec.BOOL.fieldOf("is_valid").forGetter(ClientInterior::isValid),
            BlockPos.CODEC.fieldOf("origin").forGetter(ClientInterior::origin),
            Codec.INT.fieldOf("size_x").forGetter(ClientInterior::sizeX),
            Codec.INT.fieldOf("size_y").forGetter(ClientInterior::sizeY),
            Codec.INT.fieldOf("size_z").forGetter(ClientInterior::sizeZ),
            MEMBERSHIP_CODEC.fieldOf("membership").forGetter(ClientInterior::membership),
            Codec.FLOAT.fieldOf("internal_humidity").forGetter(ClientInterior::internalHumidity),
            Codec.FLOAT.fieldOf("external_humidity").forGetter(ClientInterior::externalHumidity),
            Codec.FLOAT.fieldOf("internal_temperature").forGetter(ClientInterior::internalTemperature),
            Codec.FLOAT.fieldOf("external_temperature").forGetter(ClientInterior::externalTemperature),
            Codec.FLOAT.fieldOf("porosity").forGetter(ClientInterior::porosity),
            Codec.FLOAT.fieldOf("external_pull").forGetter(ClientInterior::externalPull),
            Codec.FLOAT.fieldOf("source_pull").forGetter(ClientInterior::sourcePull),
            InteriorCounts.CODEC.fieldOf("counts").forGetter(ClientInterior::counts)
    ).apply(i, ClientInterior::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientInterior> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static ClientInterior createDefault() {return new ClientInterior(BlockPos.ZERO, new AABB(0, 0, 0, 0, 0, 0), false, BlockPos.ZERO, 0, 0, 0, new long[0], 0f, 0f, 0f, 0f, 0f, 0f, 0f, InteriorCounts.createDefault());}

    public static ClientInterior from(Interior i)
    {
        LongOpenHashSet union = new LongOpenHashSet(i.internalAirBlocks());
        union.addAll(i.interiorBlocks().edgeBlocks.keySet());
        union.addAll(i.interiorBlocks().heatSinkFluids.keySet());
        union.addAll(i.interiorBlocks().heatSinkBlocks.keySet());
        union.addAll(i.interiorBlocks().heatSourceFluids.keySet());
        union.addAll(i.interiorBlocks().heatSourceBlocks.keySet());

        long minX = Long.MAX_VALUE, minY = Long.MAX_VALUE, minZ = Long.MAX_VALUE;
        long maxX = Long.MIN_VALUE, maxY = Long.MIN_VALUE, maxZ = Long.MIN_VALUE;
        BlockPos.MutableBlockPos packedPos = new BlockPos.MutableBlockPos();
        for (long packed : union)
        {
            packedPos.set(packed);
            minX = Math.min(minX, packedPos.getX()); minY = Math.min(minY, packedPos.getY()); minZ = Math.min(minZ, packedPos.getZ());
            maxX = Math.max(maxX, packedPos.getX()); maxY = Math.max(maxY, packedPos.getY()); maxZ = Math.max(maxZ, packedPos.getZ());
        }
        if (union.isEmpty()) return createDefault();

        int sx = (int) (maxX - minX) + 1, sy = (int) (maxY - minY) + 1, sz = (int) (maxZ - minZ) + 1;
        BlockPos origin = new BlockPos((int) minX, (int) minY, (int) minZ);
        long[] membership = new long[(sx * sy * sz + 63) >> 6];

        for (long packed : union)
        {
            packedPos.set(packed);
            int idx = (packedPos.getX() - (int) minX) + (packedPos.getZ() - (int) minZ) * sx + (packedPos.getY() - (int) minY) * sx * sz;
            membership[idx >> 6] |= 1L << (idx & 63);
        }

        return new ClientInterior(i.homePos(), i.boundingBox(), i.isValid(), origin, sx, sy, sz, membership, i.internalHumidity(), i.externalHumidity(), i.internalTemperature(), i.externalTemperature(), i.porosity(), i.externalPull(), i.sourcePull(), InteriorCounts.from(i));
    }

    public boolean contains(BlockPos pos)
    {
        if (!isValid || sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) return false;
        if (!boundingBox.contains(Vec3.atCenterOf(pos))) return false;

        int ox = pos.getX() - origin.getX();
        int oy = pos.getY() - origin.getY();
        int oz = pos.getZ() - origin.getZ();
        if (ox < 0 || oy < 0 || oz < 0 || ox >= sizeX || oy >= sizeY || oz >= sizeZ) return false;

        int idx = ox + oz * sizeX + oy * sizeX * sizeZ;
        return (membership[idx >> 6] & (1L << (idx & 63))) != 0;
    }
}
