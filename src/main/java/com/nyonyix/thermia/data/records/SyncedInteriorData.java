package com.nyonyix.thermia.data.records;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SyncedInteriorData(
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
    public static final Codec<SyncedInteriorData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("internal_humidity").forGetter(SyncedInteriorData::internalHumidity),
            Codec.FLOAT.fieldOf("external_humidity").forGetter(SyncedInteriorData::externalHumidity),
            Codec.FLOAT.fieldOf("internal_temperature").forGetter(SyncedInteriorData::internalTemperature),
            Codec.FLOAT.fieldOf("external_temperature").forGetter(SyncedInteriorData::externalTemperature),
            Codec.FLOAT.fieldOf("porosity").forGetter(SyncedInteriorData::porosity),
            Codec.FLOAT.fieldOf("external_pull").forGetter(SyncedInteriorData::externalPull),
            Codec.FLOAT.fieldOf("source_pull").forGetter(SyncedInteriorData::sourcePull),
            Codec.FLOAT.fieldOf("volume").forGetter(SyncedInteriorData::volume)
    ).apply(i, SyncedInteriorData::new));

    public static SyncedInteriorData createDefault() {return new SyncedInteriorData(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);}
}
