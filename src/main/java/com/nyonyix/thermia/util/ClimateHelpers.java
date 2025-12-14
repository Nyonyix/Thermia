package com.nyonyix.thermia.util;

import net.dries007.tfc.client.overworld.SkyPos;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateModel;
import net.dries007.tfc.util.climate.OverworldClimateModel;
import net.dries007.tfc.util.tracker.WeatherHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

import java.util.prefs.PreferenceChangeListener;

public class ClimateHelpers
{
    public static float calcWetBulbTemperature(float temp, float humidity)
    {
        humidity *= 100f;
        return (float) (temp * Math.atan(0.151977 * Math.sqrt(humidity + 8.313659)) + Math.atan(temp + humidity) - Math.atan(humidity - 1.676331) + 0.00391838 * Math.pow(humidity, 1.5) * Math.atan(0.023101 * humidity) - 4.686035);
    }

    public static float calcGlobeTemperature(float temperature, float solarRadiation, float windSpeed)
    {
        float maxSolarHeating = 18f;
        float windChillFactor = calcWindChillFactor(windSpeed);
        float solarHeating = maxSolarHeating * solarRadiation * windChillFactor;

        return temperature + solarHeating;
    }

    public static float calcWetBulbGlobeTemperature(Level level, BlockPos pos, float temp, float humidity)
    {
        float wetBulb = calcWetBulbTemperature(temp, humidity);
        float solarRadiation = getSolarRadiation(level, pos);
        float windSpeed = getWindSpeed(level, pos);
        float globeTemp = calcGlobeTemperature(temp, solarRadiation, windSpeed);

        return (0.7f * wetBulb) + (0.2f * globeTemp) + (0.1f * temp);
    }

    public static float calcWindChillFactor(float windSpeed)
    {
        float k = 0.15f;
        return 1.0f / (1.0f + k * windSpeed);
    }

    public  static float calcHeatIndex(float temp, float humidity)
    {
        float t = temp * 9f / 5f + 32f;
        float rh = humidity * 100f;

        float hi = -42.379f
                + 2.04901523f * t
                + 10.14333127f * rh
                - 0.22475541f * t * rh
                - 0.00683783f * t * t
                - 0.05481717f * rh * rh
                + 0.00122874f * t * t * rh
                + 0.00085282f * t * rh * rh
                - 0.00000199f * t * t * rh * rh;

        return (hi - 32f) * 5f / 9f;
    }

    public static float calcApparentTemperature(Level level, BlockPos pos, float temp, float humidity)
    {
        float windSpeed = getWindSpeed(level, pos);
        float heatIndex = calcHeatIndex(temp, humidity);

        float windChill = temp;
        if (temp < 10f && windSpeed > 1.3f) windChill = 13.12f + 0.6215f * temp - 11.37f * (float) Math.pow(windSpeed * 3.6, 0.16) + 0.3965f * temp * (float) Math.pow(windSpeed * 3.6, 0.16);

        if (temp > 25f) return heatIndex;
        else if (temp < 10f) return windChill;
        else
        {
            float t = (temp - 10f) / 15f;
            return Mth.lerp(t, windChill, heatIndex);
        }
    }

    public static float calcEvaporativeCooling(float windSpeed, float humidity)
    {
        float windComponent = Mth.clampedMap(windSpeed, 0f, 5f, 0.2f, 1.0f);
        float humidityComponent = 1.0f - humidity;

        return (float) Math.sqrt(windComponent * humidityComponent);
    }

    public static float calcDewPoint(float temp, float humidity)
    {
        float rh = humidity * 100f;
        float a = 17.27f;
        float b =237.7f;

        float alpha = ((a + temp) / (b + temp)) + (float) Math.log(humidity);

        return (b * alpha) / (a - alpha);
    }

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

    public static float getHumidityRainfall(Level level, BlockPos pos)
    {
        if (!(Climate.get(level) instanceof OverworldClimateModel overworldClimateModel)) return 0.5f;

        float rainfall = overworldClimateModel.getAverageRainfall(level, pos);
        float baseHumidity = Mth.clampedMap(rainfall, 0.0f, 500f, 0.2f, 0.9f);

        float temperature = Climate.getTemperature(level, pos);
        float tempAdjustment = Mth.clampedMap(temperature, -10f, 35f, 0.1f, -0.1f);

        return Mth.clamp(baseHumidity + tempAdjustment, 0.0f, 1.0f);
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

    public static float getSolarRadiation(Level level, BlockPos pos)
    {
        long calendarTick = Calendars.get(level).getTicks();
        float fractionOfDay = Calendars.get(level).getCalendarFractionOfDay();
        float fractionOfYear = Calendars.get(level).getCalendarFractionOfYear();
        float fractionOfMonth = Calendars.get(level).getCalendarFractionOfMonth();
        float hemisphereScale = Climate.get(level).hemisphereScale();

        SkyPos sunPos = SolarCalculator.getSunPosition(pos.getZ(), hemisphereScale, fractionOfYear, fractionOfDay);
        float zenith = sunPos.zenith();

        if (zenith >= Math.PI / 2f) return 0.0f;

        float directRadiation = (float) Math.cos(zenith);
        float airMass = 1.0f / Math.max(0.01f, (float) Math.cos(zenith));
        float atmosphericTransmission = (float) Math.pow(0.7, airMass - 1);
        float radiation = directRadiation * atmosphericTransmission;

        if (level.canSeeSky(pos)) return radiation *= 0.1f;

        return Mth.clamp(radiation, 0.0f, 1.0f);
    }

    public static float getSolarRadiationWeather(Level level, BlockPos pos)
    {
        if (!(Climate.get(level) instanceof OverworldClimateModel overworldClimateModel)) return 0.5f;

        float baseRadiation = getSolarRadiation(level, pos);

        long calendarTick = Calendars.get(level).getTicks();
        float rainIntensity = overworldClimateModel.getRain(calendarTick);
        float rainfall = Climate.getRainfall(level, pos);

        if (WeatherHelpers.isPrecipitating(rainIntensity, rainfall)) baseRadiation *= 0.5f;

        if (overworldClimateModel.getThunder(calendarTick)) baseRadiation *= 0.3f;

        return Mth.clamp(baseRadiation, 0.0f, 1.0f);
    }

}
