package com.nyonyix.thermia.mixin;

import com.google.common.collect.ImmutableList;
import com.nyonyix.thermia.ai.sensors.ThermiaSensorTypes;
import net.dries007.tfc.common.entities.ai.livestock.LivestockAi;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivestockAi.class, remap = false)
public class LivestockSensorMixin
{
    @Shadow
    @Mutable
    public static ImmutableList<SensorType<? extends Sensor<? super TFCAnimal>>> SENSOR_TYPES;

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void thermia$addTemperatureSensors(CallbackInfo ci)
    {
        ImmutableList.Builder<SensorType<? extends Sensor<? super TFCAnimal>>> builder = ImmutableList.builder();
        builder.addAll(SENSOR_TYPES);

        builder.add((SensorType<? extends Sensor<? super TFCAnimal>>) (SensorType<?>) ThermiaSensorTypes.PASSIVE_TEMPERATURE_SENSOR.get());
        builder.add((SensorType<? extends Sensor<? super TFCAnimal>>) (SensorType<?>) ThermiaSensorTypes.TEMPERATURE_COMFORT_DECISION_SENSOR.get());

        SENSOR_TYPES = builder.build();
    }
}
