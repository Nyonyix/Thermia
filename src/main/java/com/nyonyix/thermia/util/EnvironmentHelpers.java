package com.nyonyix.thermia.util;

import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.KoppenClimateHumidity;
import com.nyonyix.thermia.data.WindOcclusionResult;
import net.dries007.tfc.client.overworld.SkyPos;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.util.calendar.Calendar;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

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

        float dampPenalty = 0;
        if (temp < 6f && humidity > 0.6f)
        {
            float humidityFactor = (humidity - 0.6f) / 0.4f;
            dampPenalty = humidityFactor * Mth.clampedMap(windSpeed, 0f, 24f, 0f, 10f);
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

        float humidityPenalty = 0f;
        if (temp > 16f && humidity > 0.55f) humidityPenalty = (humidity - 0.55f) * Mth.clampedMap(temp, 16f, 22f, 0f, 2.5f);

        return temp + solarDelta - windCooling + humidityPenalty;
    }

    public static float calcEffectiveTemperature(Level level, BlockPos pos, float temp, float humidity, float shade, float wetness)
    {
        float windSpeed = getWindSpeed(level, pos);
        float solarRadiation = getSolarRadiationWeather(level, pos, shade);

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
        float windComponent = Mth.clampedMap(windSpeed, 0f, 32f, 0.2f, 1.0f);
        float effectiveHumidity = Math.min(1.0f, humidity + (wetness * (1.0f - humidity)));
        float humidityComponent = 1.0f - effectiveHumidity;

        float wetnessBonus = wetness * windComponent * 0.3f;

        return (float) Math.sqrt(windComponent * humidityComponent) + wetnessBonus;
    }

    // Wind

    public static float getWindSpeed(Level level, BlockPos pos)
    {
        ClimateModel model = Climate.get(level);
        return WeatherHelpers.windMS(model.getWind(level, pos)) * getWindOcclusion(level, pos).occlusionMultiplier();
    }

    public static float getWindDirection(Level level, BlockPos pos)
    {
        ClimateModel model = Climate.get(level);
        Vec2 windVector = model.getWind(level, pos);

        return (float) Math.atan2(windVector.y, windVector.x);
    }

    public static WindOcclusionResult getWindOcclusion(Level level, BlockPos pos)
    {
        float direction = getWindDirection(level, pos);
        float directionX = (float) Math.cos(direction);
        float directionZ = (float) Math.sin(direction);

        Vec3 startVec = Vec3.atCenterOf(pos.above());
        Vec3 endVec = startVec.add(directionX * 4.0, 0, directionZ * 4.0);

        ClipContext context = new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty());
        BlockHitResult hit = level.clip(context);

        if (hit.getType() != HitResult.Type.MISS)
        {
            float hitDist = (float) startVec.distanceTo(hit.getLocation());

            return WindOcclusionResult.createDefault().withOccludingBlock(hit.getBlockPos()).withOcclusionMultiplier(Mth.clamp((float) (0.1 + (hitDist / 4.0) * 0.9), 0.1f, 1.0f));
        }

        return WindOcclusionResult.createDefault();
    }

    // Solar Radiation

    public static float getSolarRadiation(Level level, BlockPos pos, float shade)
    {
        long calendarTick = Calendars.get(level).getTicks();
        float fractionOfDay = Calendars.get(level).getCalendarFractionOfDay();
        float fractionOfYear = Calendars.get(level).getCalendarFractionOfYear();
        float fractionOfMonth = Calendars.get(level).getCalendarFractionOfMonth();
        float hemisphereScale = Climate.get(level).hemisphereScale();

        SkyPos sunPos = SolarCalculator.getSunPosition(pos.getZ(), hemisphereScale, fractionOfYear, fractionOfDay);
        float zenith = sunPos.zenith();

        if (zenith >= Math.PI / 1.8f) return 0.0f;

        float directRadiation = (float) Math.cos(zenith);
        float airMass = 1.0f / Math.max(0.01f, (float) Math.cos(zenith));
        float atmosphericTransmission = (float) Math.pow(0.7, airMass - 1);
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
        float rainfall = Climate.getRainfall(level, pos);

        if (WeatherHelpers.isPrecipitating(rainIntensity, rainfall)) baseRadiation *= 0.5f;

        if (overworldClimateModel.getThunder(calendarTick)) baseRadiation *= 0.3f;

        return Mth.clamp(baseRadiation, 0.0f, 1.0f);
    }

    // Util

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

    public static float calcDryingRate(Level level, BlockPos pos, float temperature, float humidity)
    {
        float tempComponent = Mth.clampedMap(temperature,-10f, 40f, 0.1f, 2.0f);
        float humidityComponent = 1.0f - humidity;
        float windComponent = 1.0f + (getWindSpeed(level, pos)* 0.15f);

        float dryingRate = 0.01f * tempComponent * humidityComponent * windComponent;

        return Mth.clamp(dryingRate, 0.001f, 0.2f);
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
        float rainfall = levelClimate.getRainfall(level, pos);

        float timeOfDay = levelCalendar.getCalendarFractionOfDay();

        float climateHumidity = newHumidityKoppen(koppenClimateHumidity, rainVar, fractionOfYear, isNorth);
        float rainDrivenHumidity = newHumidityRain(rainIntensity, rainfall);
        float diurnalModifier = 1f + 0.1f * (float) Math.cos((timeOfDay - 0.25) * 2 * Math.PI);

        float forestModifier = 0f; //Todo - Maybe add forest modifier based on forest density.

        return Mth.clamp((climateHumidity + rainDrivenHumidity) * diurnalModifier, 0f, 0.99f);
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

        return Mth.clamp(moisterFactor, 0f, 0.5f);
    }

    public static float newHumidityKoppen(KoppenClimateHumidity koppenClimateHumidity, float rainVar, float fractionOfYear, boolean isNorth)
    {
        if (!isNorth) rainVar = -rainVar;

        float seasonalWave = (float) Math.sin((fractionOfYear - 0.25) * 2 * Math.PI);
        float t = 0.5f + (seasonalWave * rainVar * 0.5f);

        return Mth.lerp(t, koppenClimateHumidity.minHumidity(), koppenClimateHumidity.maxHumidity());
    }

    public static float getKoppenClimateDefaultHumidity(KoppenClimateHumidity koppenClimateHumidity)
    {
        float delta = koppenClimateHumidity.maxHumidity() - koppenClimateHumidity.minHumidity();
        return koppenClimateHumidity.minHumidity() + delta / 2;
    }

    public static float getSeasonalHumidityShift(Month month, KoppenClimateHumidity koppenClimateHumidity)
    {
        float tempModifier = month.getTemperatureModifier();
        return tempModifier * koppenClimateHumidity.seasonalVariation();
    }

    public static float calcSeasonalHumidity(Level level, BlockPos pos, RandomSource random, float previousHumidity)
    {
        KoppenClimateHumidity koppenClimateHumidity = getKoppenHumidity(level, pos);
        Month month = getEffectiveMonthOfYear(level, pos);
        float seasonalShift = getSeasonalHumidityShift(month, koppenClimateHumidity);

        float seasonalMax = Math.max(1f, koppenClimateHumidity.maxHumidity() + seasonalShift);
        float seasonalMin = Math.min(0f, koppenClimateHumidity.minHumidity() + seasonalShift);

        float randomChange = (random.nextFloat() * 0.2f - 0.1f) * previousHumidity;
        float newHumidity = previousHumidity + randomChange;

        return Math.max(seasonalMin, Math.min(seasonalMax, newHumidity));
    }

    public static float getClimateSpecificHumidity(Level level, BlockPos pos, RandomSource random)
    {
        KoppenClimateHumidity koppenClimateHumidity = getKoppenHumidity(level, pos);
        Month month = getEffectiveMonthOfYear(level, pos);
        float previousHumidity = getKoppenClimateDefaultHumidity(koppenClimateHumidity);
        float humidity = calcSeasonalHumidity(level, pos, random, previousHumidity);

        switch (koppenClimateHumidity.climateClass())
        {
            case CSA, CSB, CSC, DSA, DSB, DSC, DSD ->
            {
                if (month == Month.JUNE || month == Month.JULY || month == Month.AUGUST)
                {
                    humidity = Mth.lerp(0.5f, humidity, koppenClimateHumidity.minHumidity());
                }
                else if (month == Month.DECEMBER || month == Month.JANUARY || month == Month.FEBRUARY)
                {
                    humidity = Mth.lerp(0.5f, humidity, koppenClimateHumidity.maxHumidity());
                }
            }
            case AW, CWA, CWB, CWC, DWA, DWB, DWC, DWD ->
            {
                if (month == Month.JUNE || month == Month.JULY || month == Month.AUGUST)
                {
                    humidity = Mth.lerp(0.5f, humidity, koppenClimateHumidity.maxHumidity());
                }
                else if (month == Month.DECEMBER || month == Month.JANUARY || month == Month.FEBRUARY)
                {
                    humidity = Mth.lerp(0.5f, humidity, koppenClimateHumidity.minHumidity());
                }
            }
            case AS ->
            {
                if (month == Month.DECEMBER || month == Month.JANUARY || month == Month.FEBRUARY)
                {
                    humidity = Mth.lerp(0.5f, humidity, koppenClimateHumidity.maxHumidity());
                }
            }
        }

        return Mth.clamp(humidity, 0.0f, 1.0f);
    }

    public static float getHumidityWeather(Level level, BlockPos pos)
    {
        if (!(Climate.get(level) instanceof OverworldClimateModel overworldClimateModel)) return 0.5f;

        float humidity = getHumidityRainfall(level, pos);

        long calenderTick = Calendars.get(level).getTicks();
        float rainIntensity = overworldClimateModel.getRain(calenderTick);
        float rainfall = Climate.getRainfall(level, pos);

        if (WeatherHelpers.isPrecipitating(rainIntensity, rainfall)) humidity = Mth.lerp(0.7f, humidity, 0.95f);

        if (overworldClimateModel.getThunder(calenderTick)) humidity = Math.max(humidity, 0.90f);

        return Mth.clamp(humidity, 0.0f, 1.0f);
    }

    public static float getHumidityRainfall(Level level, BlockPos pos)
    {
        if (!(Climate.get(level) instanceof OverworldClimateModel overworldClimateModel)) return 0.5f;

        float rainfall = overworldClimateModel.getAverageRainfall(level, pos);
        float baseHumidity = Mth.clampedMap(rainfall, 0.0f, 500f, 0.2f, 0.9f);

        float temperature = Climate.getTemperature(level, pos);
        float tempAdjustment = Mth.clampedMap(temperature, -10f, 35f, 0.1f, -0.1f);

        return Mth.clamp(baseHumidity + tempAdjustment, 0.0f, 1.0f);
    }
}
