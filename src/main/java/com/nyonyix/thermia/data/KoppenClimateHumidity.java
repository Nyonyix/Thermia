package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.EnumMap;

public record KoppenClimateHumidity(
        float maxHumidity,
        float minHumidity,
        float seasonalVariation,
        KoppenClimateClassification climateClass
)
{
    private static final Codec<KoppenClimateClassification> KOPPEN_CODEC = Codec.STRING.xmap(KoppenClimateClassification::valueOf, KoppenClimateClassification::getSerializedName);

    public static final Codec<KoppenClimateHumidity> CODEC = RecordCodecBuilder.create(koppenClimatesHumidityInstance -> koppenClimatesHumidityInstance.group(
            Codec.FLOAT.fieldOf("max_humidity").forGetter(KoppenClimateHumidity::maxHumidity),
            Codec.FLOAT.fieldOf("min_humidity").forGetter(KoppenClimateHumidity::minHumidity),
            Codec.FLOAT.fieldOf("seasonal_variation").forGetter(KoppenClimateHumidity::seasonalVariation),
            KOPPEN_CODEC.fieldOf("climate_class").forGetter(KoppenClimateHumidity::climateClass)
    ).apply(koppenClimatesHumidityInstance, KoppenClimateHumidity::new));

    public static final StreamCodec<ByteBuf, KoppenClimateHumidity> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, KoppenClimateHumidity::maxHumidity,
            ByteBufCodecs.FLOAT, KoppenClimateHumidity::minHumidity,
            ByteBufCodecs.FLOAT, KoppenClimateHumidity::seasonalVariation,
            ByteBufCodecs.fromCodec(KOPPEN_CODEC), KoppenClimateHumidity::climateClass,
            KoppenClimateHumidity::new
    );

    public static final EnumMap<KoppenClimateClassification, KoppenClimateHumidity> KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP = new EnumMap<>(KoppenClimateClassification.class);

    static {
        // Tropical climates - High humidity year-round
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.AF, new KoppenClimateHumidity(0.95f, 0.80f, 0.05f, KoppenClimateClassification.AF));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.AM, new KoppenClimateHumidity(0.90f, 0.70f, 0.15f, KoppenClimateClassification.AM));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.AW, new KoppenClimateHumidity(0.80f, 0.50f, 0.30f, KoppenClimateClassification.AW));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.AS, new KoppenClimateHumidity(0.80f, 0.50f, 0.30f, KoppenClimateClassification.AS));

        // Arid climates - Very low humidity
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.BWH, new KoppenClimateHumidity(0.25f, 0.10f, 0.08f, KoppenClimateClassification.BWH));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.BWK, new KoppenClimateHumidity(0.25f, 0.10f, 0.08f, KoppenClimateClassification.BWK));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.BSH, new KoppenClimateHumidity(0.45f, 0.25f, 0.10f, KoppenClimateClassification.BSH));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.BSK, new KoppenClimateHumidity(0.45f, 0.25f, 0.10f, KoppenClimateClassification.BSK));

        // Temperate - Mediterranean (dry summer)
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CSA, new KoppenClimateHumidity(0.65f, 0.30f, 0.35f, KoppenClimateClassification.CSA));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CSB, new KoppenClimateHumidity(0.70f, 0.35f, 0.35f, KoppenClimateClassification.CSB));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CSC, new KoppenClimateHumidity(0.70f, 0.40f, 0.30f, KoppenClimateClassification.CSC));

        // Temperate - Dry winter
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CWA, new KoppenClimateHumidity(0.75f, 0.45f, 0.30f, KoppenClimateClassification.CWA));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CWB, new KoppenClimateHumidity(0.75f, 0.50f, 0.25f, KoppenClimateClassification.CWB));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CWC, new KoppenClimateHumidity(0.75f, 0.55f, 0.20f, KoppenClimateClassification.CWC));

        // Temperate - Humid all year
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CFA, new KoppenClimateHumidity(0.85f, 0.65f, 0.15f, KoppenClimateClassification.CFA));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CFB, new KoppenClimateHumidity(0.85f, 0.70f, 0.10f, KoppenClimateClassification.CFB));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.CFC, new KoppenClimateHumidity(0.85f, 0.70f, 0.10f, KoppenClimateClassification.CFC));

        // Continental - Mediterranean influence (dry summer)
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DSA, new KoppenClimateHumidity(0.60f, 0.30f, 0.30f, KoppenClimateClassification.DSA));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DSB, new KoppenClimateHumidity(0.60f, 0.35f, 0.25f, KoppenClimateClassification.DSB));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DSC, new KoppenClimateHumidity(0.60f, 0.35f, 0.25f, KoppenClimateClassification.DSC));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DSD, new KoppenClimateHumidity(0.60f, 0.35f, 0.25f, KoppenClimateClassification.DSD));

        // Continental - Dry winter (monsoon influence)
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DWA, new KoppenClimateHumidity(0.70f, 0.40f, 0.30f, KoppenClimateClassification.DWA));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DWB, new KoppenClimateHumidity(0.70f, 0.45f, 0.25f, KoppenClimateClassification.DWB));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DWC, new KoppenClimateHumidity(0.65f, 0.45f, 0.20f, KoppenClimateClassification.DWC));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DWD, new KoppenClimateHumidity(0.65f, 0.45f, 0.20f, KoppenClimateClassification.DWD));

        // Continental - Humid all year
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DFA, new KoppenClimateHumidity(0.80f, 0.60f, 0.15f, KoppenClimateClassification.DFA));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DFB, new KoppenClimateHumidity(0.80f, 0.60f, 0.15f, KoppenClimateClassification.DFB));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DFC, new KoppenClimateHumidity(0.75f, 0.55f, 0.15f, KoppenClimateClassification.DFC));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.DFD, new KoppenClimateHumidity(0.75f, 0.55f, 0.15f, KoppenClimateClassification.DFD));

        // Polar climates
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.ET, new KoppenClimateHumidity(0.65f, 0.40f, 0.10f, KoppenClimateClassification.ET));
        KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.put(KoppenClimateClassification.EF, new KoppenClimateHumidity(0.75f, 0.50f, 0.05f, KoppenClimateClassification.EF));
    }

    public static KoppenClimateHumidity createDefault() {return new KoppenClimateHumidity(0.75f, 0.50f, 0.25f, KoppenClimateClassification.AF);}

    public String climateToString() {return "%s: Max: %.2f Min: %.2f Variation: %.2f".formatted(this.climateClass.getSerializedName(), this.maxHumidity, this.minHumidity, this.seasonalVariation);}

    public KoppenClimateHumidity withMaxHumidity(float maxHumidity) {return new KoppenClimateHumidity(maxHumidity, this.minHumidity, this.seasonalVariation, this.climateClass);}

    public KoppenClimateHumidity withMinHumidity(float minHumidity) {return new KoppenClimateHumidity(this.maxHumidity, minHumidity, this.seasonalVariation, this.climateClass);}

    public KoppenClimateHumidity withSeasonalVariation(float seasonalVariation) {return new KoppenClimateHumidity(this.maxHumidity, this.minHumidity, seasonalVariation, this.climateClass);}

    public KoppenClimateHumidity withClimateClass(KoppenClimateClassification climateClass) {return new KoppenClimateHumidity(this.maxHumidity, this.minHumidity, this.seasonalVariation, climateClass);}

}
