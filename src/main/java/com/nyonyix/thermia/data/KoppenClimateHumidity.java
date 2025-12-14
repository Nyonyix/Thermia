package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.lwjgl.openal.SOFTLoopPoints;

import java.util.EnumMap;

public record KoppenClimateHumidity(
        float maxHumidity,
        float minHumidity,
        float seasonalVariation
)
{
    public static final Codec<KoppenClimateHumidity> CODEC = RecordCodecBuilder.create(koppenClimatesHumidityInstance -> koppenClimatesHumidityInstance.group(
            Codec.FLOAT.fieldOf("max_humidity").forGetter(KoppenClimateHumidity::maxHumidity),
            Codec.FLOAT.fieldOf("min_humidity").forGetter(KoppenClimateHumidity::minHumidity),
            Codec.FLOAT.fieldOf("seasonal_variation").forGetter(KoppenClimateHumidity::seasonalVariation)
    ).apply(koppenClimatesHumidityInstance, KoppenClimateHumidity::new));

    public static final StreamCodec<ByteBuf, KoppenClimateHumidity> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, KoppenClimateHumidity::maxHumidity,
            ByteBufCodecs.FLOAT, KoppenClimateHumidity::minHumidity,
            ByteBufCodecs.FLOAT, KoppenClimateHumidity::seasonalVariation,
            KoppenClimateHumidity::new
    );

    public static final EnumMap<KoppenClimateClassification, KoppenClimateHumidity> KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP = new EnumMap<>(KoppenClimateClassification.class);

    static {
        // Tropical climates - High humidity year-round
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.AF, new KoppenClimateHumidity(0.80f, 0.95f, 0.05f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.AM, new KoppenClimateHumidity(0.70f, 0.90f, 0.15f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.AW, new KoppenClimateHumidity(0.50f, 0.80f, 0.30f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.AS, new KoppenClimateHumidity(0.50f, 0.80f, 0.30f));

        // Arid climates - Very low humidity
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.BWH, new KoppenClimateHumidity(0.10f, 0.25f, 0.08f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.BWK, new KoppenClimateHumidity(0.10f, 0.25f, 0.08f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.BSH, new KoppenClimateHumidity(0.25f, 0.45f, 0.10f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.BSK, new KoppenClimateHumidity(0.25f, 0.45f, 0.10f));

        // Temperate - Mediterranean (dry summer)
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CSA, new KoppenClimateHumidity(0.30f, 0.65f, 0.35f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CSB, new KoppenClimateHumidity(0.35f, 0.70f, 0.35f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CSC, new KoppenClimateHumidity(0.40f, 0.70f, 0.30f));

        // Temperate - Dry winter
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CWA, new KoppenClimateHumidity(0.45f, 0.75f, 0.30f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CWB, new KoppenClimateHumidity(0.50f, 0.75f, 0.25f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CWC, new KoppenClimateHumidity(0.55f, 0.75f, 0.20f));

        // Temperate - Humid all year
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CFA, new KoppenClimateHumidity(0.65f, 0.85f, 0.15f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CFB, new KoppenClimateHumidity(0.70f, 0.85f, 0.10f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CFC, new KoppenClimateHumidity(0.70f, 0.85f, 0.10f));

        // Continental - Mediterranean influence (dry summer)
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DSA, new KoppenClimateHumidity(0.30f, 0.60f, 0.30f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DSB, new KoppenClimateHumidity(0.35f, 0.60f, 0.25f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DSC, new KoppenClimateHumidity(0.35f, 0.60f, 0.25f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DSD, new KoppenClimateHumidity(0.35f, 0.60f, 0.25f));

        // Continental - Dry winter (monsoon influence)
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DWA, new KoppenClimateHumidity(0.40f, 0.70f, 0.30f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DWB, new KoppenClimateHumidity(0.45f, 0.70f, 0.25f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DWC, new KoppenClimateHumidity(0.45f, 0.65f, 0.20f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DWD, new KoppenClimateHumidity(0.45f, 0.65f, 0.20f));

        // Continental - Humid all year
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DFA, new KoppenClimateHumidity(0.60f, 0.80f, 0.15f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DFB, new KoppenClimateHumidity(0.60f, 0.80f, 0.15f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DFC, new KoppenClimateHumidity(0.55f, 0.75f, 0.15f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DFD, new KoppenClimateHumidity(0.55f, 0.75f, 0.15f));

        // Polar climates
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.ET, new KoppenClimateHumidity(0.40f, 0.65f, 0.10f));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.EF, new KoppenClimateHumidity(0.50f, 0.75f, 0.05f));
    }

    public KoppenClimateHumidity createDefault() {return new KoppenClimateHumidity(0.75f, 0.50f, 0.25f);}

    public KoppenClimateHumidity withMaxHumidity(float maxHumidity) {return new KoppenClimateHumidity(maxHumidity, this.minHumidity, this.seasonalVariation);}

    public KoppenClimateHumidity withMinHumidity(float minHumidity) {return new KoppenClimateHumidity(this.maxHumidity, minHumidity, this.seasonalVariation);}

    public KoppenClimateHumidity withSeasonalVariation(float seasonalVariation) {return new KoppenClimateHumidity(this.maxHumidity, this.minHumidity, seasonalVariation);}

}
