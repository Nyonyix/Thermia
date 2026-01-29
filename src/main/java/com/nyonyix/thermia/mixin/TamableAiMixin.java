package com.nyonyix.thermia.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ai.behaviours.MoveToSeekComfort;
import com.nyonyix.thermia.ai.behaviours.StayAtComfort;
import com.nyonyix.thermia.ai.behaviours.ThermiaActivities;
import com.nyonyix.thermia.ai.sensors.ThermiaSensorTypes;
import net.dries007.tfc.common.entities.ai.livestock.LivestockAi;
import net.dries007.tfc.common.entities.ai.pet.TamableAi;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.pet.TamableMammal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TamableAi.class, remap = false)
public class TamableAiMixin
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    @Mutable
    public static ImmutableList<SensorType<? extends Sensor<? super TamableMammal>>> SENSOR_TYPES;

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void addTemperatureSensors(CallbackInfo ci)
    {
        ImmutableList.Builder<SensorType<? extends Sensor<? super TamableMammal>>> builder = ImmutableList.builder();
        builder.addAll(SENSOR_TYPES);

        builder.add((SensorType<? extends Sensor<? super TamableMammal>>) (SensorType<?>) ThermiaSensorTypes.PASSIVE_TEMPERATURE_SENSOR.get());
        builder.add((SensorType<? extends Sensor<? super TamableMammal>>) (SensorType<?>) ThermiaSensorTypes.TEMPERATURE_COMFORT_DECISION_SENSOR.get());

        SENSOR_TYPES = builder.build();
    }

    @Inject(method = "makeBrain", at = @At("RETURN"), remap = false)
    private static void addSeekComfortActivity(Brain<? extends TamableMammal> brain, CallbackInfoReturnable<Brain<? extends TamableMammal>> cir)
    {
        Brain<? extends TamableMammal> returnedBrain = cir.getReturnValue();

        returnedBrain.addActivity(ThermiaActivities.SEEK_COMFORT.get(), ImmutableList.of(Pair.of(0, new MoveToSeekComfort(1f)), Pair.of(1, new StayAtComfort())));
    }
}