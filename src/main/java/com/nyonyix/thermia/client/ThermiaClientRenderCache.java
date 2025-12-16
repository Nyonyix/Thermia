package com.nyonyix.thermia.client;

import com.nyonyix.thermia.data.KoppenClimateHumidity;
import com.nyonyix.thermia.util.EnvironmentHelpers;
import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateModel;
import net.dries007.tfc.util.tracker.WeatherHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ThermiaClientRenderCache
{
    private static float humidity;
    private static float wetBulbTemperature;
    private static float solarRadiation;
    private static float globeTemperature;
    private static float wetBulbGlobeTemperature;
    private static KoppenClimateHumidity climate;

    public ThermiaClientRenderCache()
    {
        humidity = 0.5f;
    }

    public static void onClientTick()
    {
        Level level = ClientHelpers.getLevel();
        Player player = ClientHelpers.getPlayer();

        if (level != null && player != null)
        {
            BlockPos pos = player.blockPosition();
            ClimateModel model = Climate.get(level);

            float temperature = model.getTemperature(level, pos);

            climate = EnvironmentHelpers.getKoppenHumidity(level, pos);
            humidity = EnvironmentHelpers.getClimateSpecificHumidity(level, pos, level.random, humidity);
            humidity = EnvironmentHelpers.getHumidityWeather(level, pos);
            wetBulbTemperature = EnvironmentHelpers.calcWetBulbTemperature(temperature, humidity);
            wetBulbGlobeTemperature = EnvironmentHelpers.calcWetBulbGlobeTemperature(level, pos, temperature, humidity);
            solarRadiation = EnvironmentHelpers.getSolarRadiationWeather(level, pos);
            globeTemperature = EnvironmentHelpers.calcGlobeTemperature(temperature, solarRadiation, WeatherHelpers.windMS(model.getWind(level, pos)));
        }
    }

    public static float getHumidity()
    {
        return humidity;
    }

    public static float getWetBulbTemperature()
    {
        return wetBulbTemperature;
    }

    public static float getSolarRadiation()
    {
        return solarRadiation;
    }

    public static float getGlobeTemperature()
    {
        return globeTemperature;
    }

    public static float getWetBulbGlobeTemperature()
    {
        return wetBulbGlobeTemperature;
    }

    public static KoppenClimateHumidity getClimate()
    {
        return climate;
    }
}
