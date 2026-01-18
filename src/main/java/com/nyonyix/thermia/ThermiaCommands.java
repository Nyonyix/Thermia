package com.nyonyix.thermia;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nyonyix.thermia.api.ThermiaEntityTemperatureAPI;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ThermiaCommands
{
    private static class PropertyDefinition
    {
        final Function<Entity, Float> getter;
        final BiFunction<Entity, Float, Boolean> setter;

        PropertyDefinition(Function<Entity, Float> getter, BiFunction<Entity, Float, Boolean> setter)
        {
            this.getter = getter;
            this.setter = setter;
        }
    }

    private static final Map<String, PropertyDefinition> PROPERTIES = new HashMap<>();

    static
    {
        PROPERTIES.put("internal_temperature", new PropertyDefinition(ThermiaEntityTemperatureAPI::getInternalTemperature, ThermiaEntityTemperatureAPI::setInternalTemperature));
        PROPERTIES.put("max_temperature", new PropertyDefinition(ThermiaEntityTemperatureAPI::getMaxInternalTemperature, ThermiaEntityTemperatureAPI::setMaxInternalTemperature));
        PROPERTIES.put("min_temperature", new PropertyDefinition(ThermiaEntityTemperatureAPI::getMinInternalTemperature, ThermiaEntityTemperatureAPI::setMinInternalTemperature));
        PROPERTIES.put("wetness", new PropertyDefinition(ThermiaEntityTemperatureAPI::getWetness, ThermiaEntityTemperatureAPI::setWetness));
        PROPERTIES.put("environment_temperature", new PropertyDefinition(ThermiaEntityTemperatureAPI::getEnvironmentTemperature, null));
        PROPERTIES.put("humidity", new PropertyDefinition(ThermiaEntityTemperatureAPI::getEnvironmentHumidity, null));
        PROPERTIES.put("mob_comfort_threshold_hot", new PropertyDefinition(entity -> ThermiaEntityTemperatureAPI.getMobComfortThresholds(entity)[1], null));
        PROPERTIES.put("mob_comfort_threshold_cold", new PropertyDefinition(entity -> ThermiaEntityTemperatureAPI.getMobComfortThresholds(entity)[0], null));

    }

    private static String formatPropertyName(String propertyName)
    {
        return propertyName.replace("_", " ").substring(0, 1).toUpperCase(Locale.ROOT) + propertyName.replace("_", " ").substring(1);
    }

    private static int executeGet(CommandContext<CommandSourceStack> context, String propertyName, PropertyDefinition propertyDefinition)
    {
        try
        {
            Entity entity = EntityArgument.getEntity(context, "target");

            if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE))
            {
                context.getSource().sendFailure(Component.literal("Entity does not have temperature data"));
                return 0;
            }

            float value = propertyDefinition.getter.apply(entity);
            context.getSource().sendSuccess(() -> Component.literal(String.format("%s: %.2f", formatPropertyName(propertyName), value)), false);
            return 1;
        }
        catch (Exception e)
        {
            context.getSource().sendFailure(Component.literal("Error: " + e.getLocalizedMessage()));
            return 0;
        }
    }

    private static int executeSet(CommandContext<CommandSourceStack> context, String propertyName, PropertyDefinition propertyDefinition)
    {
        try
        {
            Entity entity = EntityArgument.getEntity(context, "target");
            float value = FloatArgumentType.getFloat(context, "value");

            if (!entity.hasData(ThermiaAttachments.ENTITY_TEMPERATURE))
            {
                context.getSource().sendFailure(Component.literal("Entity does not have temperature data"));
                return 0;
            }

            boolean success = propertyDefinition.setter.apply(entity, value);
            if (success)
            {
                context.getSource().sendSuccess(() -> Component.literal(String.format("Set %s: %.2f", formatPropertyName(propertyName), value)), false);
                return 1;
            }
            else
            {
                context.getSource().sendFailure(Component.literal("Failed to set: " + propertyName));
                return 0;
            }
        }
        catch (Exception e)
        {
            context.getSource().sendFailure(Component.literal("Error: " + e.getLocalizedMessage()));
            return 0;
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseCommand = Commands.literal("thermia").requires(source -> source.hasPermission(2));
        LiteralArgumentBuilder<CommandSourceStack> getCommand = Commands.literal("get");
        LiteralArgumentBuilder<CommandSourceStack> setCommand = Commands.literal("set");

        for (Map.Entry<String, PropertyDefinition> entry : PROPERTIES.entrySet())
        {
            LiteralArgumentBuilder<CommandSourceStack> propertyCommand = Commands.literal(entry.getKey());
            RequiredArgumentBuilder<CommandSourceStack, EntitySelector> entityTarget = Commands.argument("target", EntityArgument.entity());

            getCommand.then(propertyCommand.then(entityTarget.executes(context -> executeGet(context, entry.getKey(), entry.getValue()))));
        }

        for (Map.Entry<String, PropertyDefinition> entry : PROPERTIES.entrySet())
        {
            if (entry.getValue().setter != null)
            {
                LiteralArgumentBuilder<CommandSourceStack> propertyCommand = Commands.literal(entry.getKey());
                RequiredArgumentBuilder<CommandSourceStack, EntitySelector> entityTarget = Commands.argument("target", EntityArgument.entity());
                RequiredArgumentBuilder<CommandSourceStack, Float> valueArgument = Commands.argument("value", FloatArgumentType.floatArg());

                setCommand.then(propertyCommand.then(entityTarget.then(valueArgument.executes(context -> executeSet(context, entry.getKey(), entry.getValue())))));
            }
        }

        baseCommand.then(getCommand);
        baseCommand.then(setCommand);
        dispatcher.register(baseCommand);
    }
}
