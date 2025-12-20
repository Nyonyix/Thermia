package com.nyonyix.thermia.util;

import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.KoppenClimateHumidity;
import net.dries007.tfc.client.overworld.SkyPos;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateModel;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.util.climate.OverworldClimateModel;
import net.dries007.tfc.util.tracker.WeatherHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
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

    public static float calcGlobeTemperature(float temperature, float solarRadiation, float windSpeed)
    {
        float maxSolarHeating = (float) ServerConfig.MAX_SOLAR_HEATING.getAsDouble();
        float windChillFactor = calcWindChillFactor(windSpeed);
        float solarHeating = maxSolarHeating * solarRadiation * windChillFactor;

        return temperature + solarHeating;
    }

    public static float calcWetBulbGlobeTemperature(Level level, BlockPos pos, float temp, float humidity, float shade)
    {
        float wetBulb = calcWetBulbTemperature(temp, humidity);
        float solarRadiation = getSolarRadiationWeather(level, pos, shade);
        float windSpeed = getWindSpeed(level, pos);
        float globeTemp = calcGlobeTemperature(temp, solarRadiation, windSpeed);

        return (0.7f * wetBulb) + (0.2f * globeTemp) + (0.1f * temp);
    }

    public static float calcWindChillFactor(float windSpeed)
    {
        float k = (float) ServerConfig.WIND_CHILL_FACTOR.getAsDouble();
        return 1.0f / (1.0f + k * windSpeed);
    }

    public static float calcEvaporativeCooling(float windSpeed, float humidity)
    {
        float windComponent = Mth.clampedMap(windSpeed, 0f, 5f, 0.2f, 1.0f);
        float humidityComponent = 1.0f - humidity;

        return (float) Math.sqrt(windComponent * humidityComponent);
    }

    // Wind

    public static float getWindSpeed(Level level, BlockPos pos)
    {
        ClimateModel model = Climate.get(level);
        return WeatherHelpers.windMS(model.getWind(level, pos));
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
        float avgTemperature = Climate.getAverageTemperature(level, pos);
        float rainfall = Climate.getRainfall(level, pos);
        float rainfallVariance = Climate.getRainfallVariance(level, pos);
        boolean isNorth = SolarCalculator.getInNorthernHemisphere(pos, level);

        KoppenClimateClassification climate = KoppenClimateClassification.classify(avgTemperature, rainfall, rainfallVariance, isNorth);

        return KoppenClimateHumidity.KOPPEN_CLIMATE_HUMIDITY_ENUM_MAP.getOrDefault(climate, KoppenClimateHumidity.createDefault());
    }

    // Humidity

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
