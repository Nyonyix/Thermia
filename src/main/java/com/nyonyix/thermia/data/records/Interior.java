package com.nyonyix.thermia.data.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.InteriorBlocks;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record Interior(
        InteriorBlocks interiorBlocks,
        LongOpenHashSet internalAirBlocks,
        BlockPos homePos,
        AABB boundingBox,
        boolean isValid,
        float internalHumidity,
        float externalHumidity,
        float internalTemperature,
        float externalTemperature,
        float porosity,
        float externalPull,
        float sourcePull,
        float volume
)
{

    private static final Codec<LongOpenHashSet> INTERNAL_AIR_BLOCKS_CODEC = Codec.LONG.listOf().xmap(LongOpenHashSet::new,set -> new LongArrayList(set.toLongArray()));

    public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(i -> i.group(
            Vec3.CODEC.fieldOf("min").forGetter(AABB::getMinPosition),
            Vec3.CODEC.fieldOf("max").forGetter(AABB::getMaxPosition)
    ).apply(i, AABB::new));

    public static final Codec<Interior> CODEC = RecordCodecBuilder.create(interiorInstance -> interiorInstance.group(
            InteriorBlocks.CODEC.fieldOf("interior_Blocks").forGetter(Interior::interiorBlocks),
            INTERNAL_AIR_BLOCKS_CODEC.fieldOf("internal_air_blocks").forGetter(Interior::internalAirBlocks),
            BlockPos.CODEC.fieldOf("home_pos").forGetter(Interior::homePos),
            AABB_CODEC.fieldOf("bounding_box").forGetter(Interior::boundingBox),
            Codec.BOOL.fieldOf("is_valid").forGetter(Interior::isValid),
            Codec.FLOAT.fieldOf("internal_humidity").forGetter(Interior::internalHumidity),
            Codec.FLOAT.fieldOf("external_humidity").forGetter(Interior::externalHumidity),
            Codec.FLOAT.fieldOf("internal_temperature").forGetter(Interior::internalTemperature),
            Codec.FLOAT.fieldOf("external_temperature").forGetter(Interior::externalTemperature),
            Codec.FLOAT.fieldOf("porosity").forGetter(Interior::porosity),
            Codec.FLOAT.fieldOf("external_pull").forGetter(Interior::externalPull),
            Codec.FLOAT.fieldOf("source_pull").forGetter(Interior::sourcePull),
            Codec.FLOAT.fieldOf("volume").forGetter(Interior::volume)
    ).apply(interiorInstance, Interior::new));

    public static Interior createDefault() {return new Interior(InteriorBlocks.createDefault(), LongOpenHashSet.of(), BlockPos.ZERO, new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0), false, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);}

    public Interior withBoundingBox(AABB boundingBox) {return  new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, boundingBox, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature, this.porosity, this.externalPull, this.sourcePull, this.volume);}
    public Interior withIsValid(boolean isValid) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.boundingBox, isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature, this.porosity, this.externalPull, this.sourcePull, this.volume);}
    public Interior withInternalTemperature(float internalTemperature) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.boundingBox, this.isValid, this.internalHumidity, this.externalHumidity, internalTemperature, this.externalTemperature, this.porosity, this.externalPull, this.sourcePull, this.volume);}
    public Interior withExternalTemperature(float externalTemperature) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.boundingBox, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, externalTemperature, this.porosity, this.externalPull, this.sourcePull, this.volume);}
    public Interior withVolume(float volume) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.boundingBox, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature, this.porosity, this.externalPull, this.sourcePull, volume);}
    public Interior withPorosity(float porosity) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.boundingBox, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature, porosity, this.externalPull, this.sourcePull, this.volume);}
    public Interior withExternalPull(float externalPull) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.boundingBox, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature, this.porosity, externalPull, this.sourcePull, this.volume);}
    public Interior withSourcePull(float sourcePull) {return new Interior(this.interiorBlocks, this.internalAirBlocks, this.homePos, this.boundingBox, this.isValid, this.internalHumidity, this.externalHumidity, this.internalTemperature, this.externalTemperature, this.porosity, this.externalPull, sourcePull, this.volume);}
}
