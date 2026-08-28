package com.nyonyix.thermia.util;

import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.api.ThermiaInteriorAPI;
import com.nyonyix.thermia.data.records.Interior;
import com.nyonyix.thermia.data.records.KoppenClimateHumidity;
import com.nyonyix.thermia.data.records.SolarShadeResult;
import com.nyonyix.thermia.data.records.WindOcclusionResult;
import com.nyonyix.thermia.data.attachment.Thermometer;
import net.dries007.tfc.client.overworld.SkyPos;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateModel;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.util.climate.OverworldClimateModel;
import net.dries007.tfc.util.tracker.WeatherHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;


public class EnvironmentHelpers
{
    // Temperature

    public static float calcWetBulbTemperature(float temp, float humidity)
    {
        humidity *= 100f;
        return (float) (temp * Math.atan(0.151977 * Math.sqrt(humidity + 8.313659)) + Math.atan(temp + humidity) - Math.atan(humidity - 1.676331) + 0.00391838 * Math.pow(humidity, 1.5) * Math.atan(0.023101 * humidity) - 4.686035);
    }

    public static float calcGlobeTemperature(float temperature, float solarRadiation)
    {
        float maxSolarHeating = (float) ServerConfig.MAX_SOLAR_HEATING.getAsDouble();
        float solarHeating = maxSolarHeating * solarRadiation;

        return temperature + solarHeating;
    }

    public static float calcWetBulbGlobeTemperature(Level level, BlockPos pos, float temp, float humidity, float solarRadiation)
    {
        float wetBulb = calcWetBulbTemperature(temp, humidity);
        float globeTemp = calcGlobeTemperature(temp, solarRadiation);

        return (0.7f * wetBulb) + (0.2f * globeTemp) + (0.1f * temp);
    }

    public static float calcForCold(float temp, float windSpeed, float solarRadiation, float humidity)
    {
        float maxSolar = (float) ServerConfig.MAX_SOLAR_HEATING.getAsDouble();
        float solarRaw = solarRadiation * maxSolar;

        float solarTempFactor = Mth.clampedMap(temp, -30f, 12f, 0.15f, 0.5f);
        float solarDelta = solarRaw * solarTempFactor;

        float solarWindLoss = 1f / (1f + 0.25f * windSpeed);
        solarDelta *= solarWindLoss;

        float windCooling = Mth.clampedMap(windSpeed, 0f, 32f, 0f, 24f);

        float dampPenalty = 0f;
        if (temp < 12f && humidity > 0.6f)
        {
            float tempFactor = Mth.clampedMap(temp, -5f, 12f, 1.0f, 0.0f);
            float humidityFactor = (humidity - 0.6f) / 0.4f;
            dampPenalty = tempFactor * humidityFactor * Mth.clampedMap(windSpeed, 0f, 24f, 0f, 10f);
        }

        return temp + solarDelta - windCooling - dampPenalty;
    }

    public static float calcForMild(float temp, float windSpeed, float solarRadiation, float humidity)
    {
        float maxSolar = (float) ServerConfig.MAX_SOLAR_HEATING.getAsDouble();
        float solarRaw = solarRadiation * maxSolar;

        float solarTempFactor = Mth.clampedMap(temp, 12f, 22f, 0.5f, 1.0f);
        float solarDelta = solarRaw * solarTempFactor;

        float solarWindloss = 1f / (1f + 0.2f * windSpeed);
        solarDelta *= solarWindloss;

        float windCooling = Mth.clampedMap(windSpeed, 0f, 32f, 0f, 8f);

        float humidityDiscomfort = 0f;
        if (temp > 16f && humidity > 0.55f) humidityDiscomfort = (humidity - 0.55f) * Mth.clampedMap(temp, 16f, 22f, 0f, 2.5f);

        return temp + solarDelta - windCooling + humidityDiscomfort;
    }

    public static float calcEffectiveTemperature(Level level, BlockPos pos, float temp, float humidity, float shade, float wetness, float windOcclusion, float convectionProtection, float radiationProtection)
    {
        float windSpeed = getWindSpeed(level, pos, windOcclusion);
        windSpeed = windSpeed * (1f - convectionProtection);

        float solarRadiation = getSolarRadiationWeather(level, pos, shade);
        solarRadiation = solarRadiation * (1f - radiationProtection);

        float cold = calcForCold(temp, windSpeed, solarRadiation, humidity);
        float mild = calcForMild(temp, windSpeed, solarRadiation, humidity);
        float hot = calcWetBulbGlobeTemperature(level, pos, temp, humidity, solarRadiation);

        float evapCooling = wetness > 0.0f ? calcEvaporativeCooling(windSpeed, humidity, wetness) : 0.0f;

        float coldW =  1f - Mth.clampedMap(temp, 8f, 14f, 0f, 1f);
        float hotW = Mth.clampedMap(temp, 18f, 26f, 0f, 1f);
        float mildW = Math.max(0f, 1f - coldW - hotW);

        float totalW = coldW + mildW + hotW;
        if (totalW > 0)
        {
            coldW /= totalW;
            mildW /= totalW;
            hotW /= totalW;
        }

        return (cold * coldW + mild * mildW + hot * hotW) - evapCooling;
    }

    public static float calcEvaporativeCooling(float windSpeed, float humidity, float wetness)
    {
        float multi = (float) ServerConfig.EVAP_COOLING_MULTI.getAsDouble();
        float windComponent = Mth.clampedMap(windSpeed, 0f, 32f, 0.2f, 1.0f);
        float humidityResistance = Mth.clampedMap(humidity, 0f, 0.95f, 1.0f, 0.3f);

        return wetness * windComponent * humidityResistance * (8.0f * multi);
    }

    public static Thermometer getThermometer(Level level, BlockPos pos)
    {
        ClimateModel model = Climate.get(level);
        ICalendar calendar = Calendars.get(level);

        float baseTemp = model.getInstantTemperature(level, pos);
        float humidity = newEnvironmentHumidity(level, pos);

        float fracYear = calendar.getCalendarFractionOfYear();
        float fracDay = calendar.getCalendarFractionOfDay();
        float hemiScale = model.hemisphereScale();

        boolean isRaining = WeatherHelpers.isPrecipitating(model.getRain(calendar.getCalendarTicks()), model.getInstantRainfall(level, pos)) && baseTemp > 0f;
        boolean canSeeSky = level.canSeeSky(pos);

        float wetness = isRaining && canSeeSky ? 1.0f : 0f;

        SkyPos sunPos = SolarCalculator.getSunPosition(pos.getZ(), hemiScale, fracYear, fracDay);
        SolarShadeResult shade = BlockSearch.getSolarShade(level, pos, sunPos.zenith(), sunPos.azimuth());
        WindOcclusionResult windOcclusion = BlockSearch.getWindOcclusion(level, pos);

        float effTemp = calcEffectiveTemperature(level, pos, baseTemp, humidity, shade.shade(), wetness, windOcclusion.occlusionMultiplier(), 0f, 0f);

        return new Thermometer(effTemp, humidity);
    }

    // Wind

    public static float getWindSpeed(Level level, BlockPos pos, float windOcclusion)
    {
        ClimateModel model = Climate.get(level);
        return WeatherHelpers.windMS(model.getWind(level, pos)) * windOcclusion;
    }

    public static float getWindDirection(Level level, BlockPos pos)
    {
        ClimateModel model = Climate.get(level);
        Vec2 windVector = model.getWind(level, pos);

        return (float) Math.atan2(windVector.y, windVector.x);
    }

    // Solar Radiation

    public static float getSolarRadiation(Level level, BlockPos pos, float shade)
    {
        float fractionOfDay = Calendars.get(level).getCalendarFractionOfDay();
        float fractionOfYear = Calendars.get(level).getCalendarFractionOfYear();
        float hemisphereScale = Climate.get(level).hemisphereScale();

        SkyPos sunPos = SolarCalculator.getSunPosition(pos.getZ(), hemisphereScale, fractionOfYear, fractionOfDay);
        float zenith = sunPos.zenith();

        if (zenith >= Math.PI / 1.8f) return 0.0f;

        float directRadiation = (float) Math.cos(zenith);
        float airMass = 1.0f / Math.max(0.1f, (float) Math.cos(zenith));
        float atmosphericTransmission = (float) Math.pow(0.75, airMass - 1);
        float radiation = directRadiation * atmosphericTransmission;

        radiation *= shade;

        return Mth.clamp(radiation, 0.0f, 1.0f);
    }

    public static float getSolarRadiationWeather(Level level, BlockPos pos, float shade)
    {
        if (!(Climate.get(level) instanceof OverworldClimateModel overworldClimateModel)) return 0.5f;

        float baseRadiation = getSolarRadiation(level, pos, shade);

        long calendarTick = Calendars.get(level).getTicks();
        float rainIntensity = overworldClimateModel.getRain(calendarTick);
        float rainfall = Climate.getAverageRainfall(level, pos);

        if (WeatherHelpers.isPrecipitating(rainIntensity, rainfall)) baseRadiation *= 0.5f;

        if (overworldClimateModel.getThunder(calendarTick)) baseRadiation *= 0.3f;

        return Mth.clamp(baseRadiation, 0.0f, 1.0f);
    }

    public static Vec3 getSunDirection(Level level, BlockPos pos)
    {
        ICalendar calender = Calendars.get(level);
        ClimateModel model = Climate.get(level);

        float fracDay = calender.getCalendarFractionOfDay();
        float fracYear = calender.getCalendarFractionOfYear();
        float hemiScale = model.hemisphereScale();

        SkyPos sunPos = SolarCalculator.getSunPosition(pos.getZ(), hemiScale, fracYear, fracDay);
        float zenith = sunPos.zenith();
        float azimuth = sunPos.azimuth();

        if (zenith >= Math.PI / 1.8f) return Vec3.ZERO;

        float sunY = (float) Math.cos(zenith);
        float horizonMag = (float) Math.sin(zenith);
        float sunX = -horizonMag * (float) Math.sin(azimuth);
        float sunZ = -horizonMag * (float) Math.cos(azimuth);

        return new Vec3(sunX, sunY, sunZ).normalize();
    }

    public static float getBeamRadiation(Level level, BlockPos pos, float shade)
    {
        float fractionDay = Calendars.get(level).getCalendarFractionOfDay();
        float fractionYear = Calendars.get(level).getCalendarFractionOfYear();
        float hemisphereScale = Climate.get(level).hemisphereScale();

        SkyPos sunPos = SolarCalculator.getSunPosition(pos.getZ(), hemisphereScale, fractionYear, fractionDay);
        float cosZenith = (float) Math.cos(sunPos.zenith());
        if (cosZenith <= 0.0f) return 0.0f;

        float airMass = 1.0f / Math.max(0.02f, cosZenith);
        float atmosphereTransmission = (float) Math.pow(0.75, airMass - 1);

        return Mth.clamp(atmosphereTransmission * shade, 0.0f, 1.0f);
    }

    public static float getBeamRadiationWeather(Level level, BlockPos pos, float shade)
    {
        if (!(Climate.get(level) instanceof OverworldClimateModel model)) return 0.5f;

        float baseRadiation = getBeamRadiation(level, pos, shade);

        long calendarTick = Calendars.get(level).getTicks();
        float rainIntensity = model.getRain(calendarTick);
        float rainfall = Climate.getAverageRainfall(level, pos);

        if (WeatherHelpers.isPrecipitating(rainIntensity, rainfall)) baseRadiation *= 0.5f;
        if (model.getThunder(calendarTick)) baseRadiation *= 0.3f;

        return Mth.clamp(baseRadiation, 0.0f, 1.0f);
    }

    // Util

    public static boolean isExposedToSky(Level level, BlockPos pos)
    {
        return pos.getY() >= level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
    }

    public static Month getEffectiveMonthOfYear(Level level, BlockPos pos)
    {
        Month month = Calendars.get(level).getAbsoluteCalendarMonthOfYear();
        boolean isNorth = SolarCalculator.getInNorthernHemisphere(pos, level);

        if (isNorth) return month;
        else return month.opposite();
    }

    public static KoppenClimateHumidity getKoppenHumidity(Level level, BlockPos pos)
    {
        ClimateModel levelModel = Climate.get(level);

        float avgTemperature = levelModel.getAverageTemperature(level, pos);
        float rainfall = levelModel.getAverageRainfall(level, pos);
        float rainfallVariance = levelModel.getRainfallVariance(level, pos);
        boolean isNorth = SolarCalculator.getInNorthernHemisphere(pos, level);

        KoppenClimateClassification climate = KoppenClimateClassification.classify(avgTemperature, rainfall, rainfallVariance, isNorth);

        return KoppenClimateHumidity.KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.getOrDefault(climate, KoppenClimateHumidity.createDefault());
    }

    public static float calcDryingRate(Level level, BlockPos pos, float temperature, float humidity, float shade, float windOcclusion)
    {
        float tempComponent = Mth.clampedMap(temperature,-10f, 40f, 0.1f, 2.0f);
        float humidityComponent = 1.0f - humidity;
        float windComponent = (1.0f + (getWindSpeed(level, pos, windOcclusion)* 0.15f));
        float solarComponent = (1.0f + (getSolarRadiationWeather(level, pos, shade))) * (float) ServerConfig.MAX_SOLAR_HEATING.getAsDouble();

        float multi = (float) ServerConfig.DRYING_MULTI.getAsDouble();

        float dryingRate = (0.01f * tempComponent * humidityComponent * windComponent * solarComponent) * multi;

        return Mth.clamp(dryingRate, 0.001f, 0.02f);
    }

    // Humidity

    public static float newEnvironmentHumidity(Level level, BlockPos pos)
    {
        KoppenClimateHumidity koppenClimateHumidity = getKoppenHumidity(level, pos);
        ICalendar levelCalendar = Calendars.get(level);
        ClimateModel levelClimate = Climate.get(level);

        float fractionOfYear = levelCalendar.getCalendarFractionOfYear();
        float rainVar = levelClimate.getRainfallVariance(level, pos);
        boolean isNorth = SolarCalculator.getInNorthernHemisphere(pos, level);

        float rainIntensity = levelClimate.getRain(levelCalendar.getCalendarTicks());
        float rainfall = levelClimate.getAverageRainfall(level, pos);

        float timeOfDay = levelCalendar.getCalendarFractionOfDay();

        float climateHumidity = newHumidityKoppen(koppenClimateHumidity, rainVar, fractionOfYear, isNorth);
        float rainBoost = newHumidityRain(rainIntensity, rainfall);
        float diurnalModifier = 1f + 0.1f * (float) Math.cos((timeOfDay - 0.25) * 2 * Math.PI);

        float forestModifier = 0f; //Todo - Maybe add forest modifier based on forest density.

        float baseHumidity = climateHumidity * diurnalModifier;
        float t = Mth.clamp(rainBoost * 2f, 0f, 1f);
        float rainAdjusted = Mth.lerp(t, baseHumidity, 1f);

        return Mth.clamp(rainAdjusted, 0f, 0.99f);
    }

    public static float getEntityHumidity(float humidity, int nonEmptyAbove)
    {
        float depthDrivenHumidity = Mth.clampedMap(nonEmptyAbove, 0, ServerConfig.MAX_BLOCKS_ABOVE.getAsInt(), 0f, 0.8f);

        return Mth.clamp(humidity + depthDrivenHumidity, 0f, 0.99f);
    }

    public static float newHumidityRain(float rainIntensity, float rainfall)
    {
        if (rainIntensity < 0) return 0f;

        float realIntensity = WeatherHelpers.calculateRealRainIntensity(rainIntensity, rainfall);
        float moisterFactor = Math.max(rainIntensity * 0.3f, realIntensity);

        return Mth.clamp(moisterFactor, 0f, 1f);
    }

    public static float newHumidityKoppen(KoppenClimateHumidity koppenClimateHumidity, float rainVar, float fractionOfYear, boolean isNorth)
    {
        if (!isNorth) rainVar = -rainVar;

        float seasonalWave = (float) Math.sin((fractionOfYear - 0.25) * 2 * Math.PI);
        float t = 0.5f + (seasonalWave * rainVar * 0.5f);

        return Mth.lerp(t, koppenClimateHumidity.minHumidity(), koppenClimateHumidity.maxHumidity());
    }
}
