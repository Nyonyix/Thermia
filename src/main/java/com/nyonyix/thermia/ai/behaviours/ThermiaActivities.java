package com.nyonyix.thermia.ai.behaviours;

import com.nyonyix.thermia.Thermia;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ThermiaActivities
{
    public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(BuiltInRegistries.ACTIVITY, Thermia.MODID);

    public static final Supplier<Activity> SEEK_COMFORT = ACTIVITIES.register("seek_comfort", () -> new Activity("seek_comfort"));
}
