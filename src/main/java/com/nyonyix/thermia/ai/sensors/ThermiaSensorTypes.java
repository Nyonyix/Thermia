package com.nyonyix.thermia.ai.sensors;

import com.nyonyix.thermia.Thermia;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ThermiaSensorTypes
{
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(BuiltInRegistries.SENSOR_TYPE, Thermia.MODID);

    public static final Supplier<SensorType<PassiveTemperatureSensor>> PASSIVE_TEMPERATURE_SENSOR = SENSOR_TYPES.register("passive_temperature_sensor", () -> new SensorType<>(PassiveTemperatureSensor::new));

    public static final Supplier<SensorType<TemperatureComfortDecisionSensor>> TEMPERATURE_COMFORT_DECISION_SENSOR = SENSOR_TYPES.register("temperature_comfort_decision_sensor", () -> new SensorType<>(TemperatureComfortDecisionSensor::new));
}
