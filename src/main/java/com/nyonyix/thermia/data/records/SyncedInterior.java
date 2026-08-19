package com.nyonyix.thermia.data.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.InteriorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

public record SyncedInterior(
        InteriorBlocks interiorBlocks,
        BlockPos homePos,
        AABB boundingBox,
        boolean isValid,
        float internalHumidity,
        float externalHumidity,
        float internalTemperature,
        float externalTemperature)
{
    public static final Codec<SyncedInterior> CODEC = RecordCodecBuilder.create(i -> i.group(
            InteriorBlocks.CODEC.fieldOf("interior_blocks").forGetter(SyncedInterior::interiorBlocks),
            BlockPos.CODEC.fieldOf("home_pos").forGetter(SyncedInterior::homePos),
            Interior.AABB_CODEC.fieldOf("bounding_box").forGetter(SyncedInterior::boundingBox),
            Codec.BOOL.fieldOf("is_valid").forGetter(SyncedInterior::isValid),
            Codec.FLOAT.fieldOf("internal_humidity").forGetter(SyncedInterior::internalHumidity),
            Codec.FLOAT.fieldOf("external_humidity").forGetter(SyncedInterior::externalHumidity),
            Codec.FLOAT.fieldOf("internal_temperature").forGetter(SyncedInterior::internalTemperature),
            Codec.FLOAT.fieldOf("external_temperature").forGetter(SyncedInterior::externalTemperature)
    ).apply(i, SyncedInterior::new));

    public static SyncedInterior createDefault() {return new SyncedInterior(InteriorBlocks.createDefault(), BlockPos.ZERO, new AABB(0, 0, 0, 0, 0, 0), false, 0f, 0f, 0f, 0f);}

    public static SyncedInterior from(Interior i) {return new SyncedInterior(i.interiorBlocks(), i.homePos(), i.boundingBox(), i.isValid(), i.internalHumidity(), i.externalHumidity(), i.internalTemperature(), i.externalTemperature());}
}
