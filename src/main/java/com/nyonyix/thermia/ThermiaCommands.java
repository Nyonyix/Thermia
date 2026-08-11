package com.nyonyix.thermia;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nyonyix.thermia.api.ThermiaEntityTemperatureAPI;
import com.nyonyix.thermia.api.ThermiaInteriorAPI;
import com.nyonyix.thermia.data.records.Interior;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;

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
            context.getSource().sendSuccess(() -> Component.literal(String.format("%s: %.2f", formatPropertyName(propertyName), value)), true);
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
                context.getSource().sendSuccess(() -> Component.literal(String.format("Set %s: %.2f", formatPropertyName(propertyName), value)), true);
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

    private static int executeRemoveInterior(CommandContext<CommandSourceStack> context)
    {
        try
        {
            BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");

            if (!ThermiaInteriorAPI.isInInterior(context.getSource().getLevel(), pos))
            {
                context.getSource().sendFailure(Component.literal("No valid interior found at: " + pos.toShortString()));
                return 0;
            }

            Interior interior = ThermiaInteriorAPI.getInteriorByPos(context.getSource().getLevel(), pos);

            if (!interior.isValid())
            {
                context.getSource().sendFailure(Component.literal("No valid interior found at: " + pos.toShortString()));
                return 0;
            }

            ThermiaInteriorAPI.removeInterior(context.getSource().getLevel(), interior.homePos());
            context.getSource().sendSuccess(() -> Component.literal("Removed interior at: " + pos.toShortString()), true);
            return 1;
        }
        catch (Exception e)
        {
            context.getSource().sendFailure(Component.literal("Error: " + e.getLocalizedMessage()));
            return 0;
        }
    }

    public static int executeListInteriors(CommandContext<CommandSourceStack> context)
    {
        try
        {
            Map<BlockPos, Interior> levelInteriors = ThermiaInteriorAPI.getAllInteriors(context.getSource().getLevel());

            if (levelInteriors.isEmpty())
            {
                 context.getSource().sendFailure(Component.literal("No Interiors for current level"));
                return 0;
            }

            for (Map.Entry<BlockPos, Interior> entry : levelInteriors.entrySet())
            {
                BlockPos pos = entry.getKey();
                Interior interior = entry.getValue();

                Component posComponent = Component.literal(String.format("[%d, %d, %d], ", pos.getX(), pos.getY(), pos.getZ()))
                        .withStyle(style -> style
                                .withColor(ChatFormatting.GREEN)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/tp @s %d %d %d", pos.getX(), pos.getY(), pos.getZ())))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click To Teleport"))));

                Component message = Component.literal("Interior: ")
                        .append(Component.literal("homePos: ").withStyle(ChatFormatting.AQUA)
                        .append(posComponent)
                        .append(Component.literal("internalAirBlocks: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.format("%d, ", interior.internalAirBlocks().size())).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("edgeBlocks: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.format("%d, ", interior.interiorBlocks().edgeBlocks.size())).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("heatSourceBlocks: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.format("%d, ", interior.interiorBlocks().heatSourceBlocks.size())).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("heatSourceFluids: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.format("%d, ", interior.interiorBlocks().heatSourceFluids.size())).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("heatSinkBlocks: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.format("%d, ", interior.interiorBlocks().heatSinkBlocks.size())).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("heatSinkFluids: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.format("%d, ", interior.interiorBlocks().heatSinkFluids.size())).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("internalHumidity: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.format("%.2f, ", interior.internalHumidity())).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("externalHumidity: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.format("%.2f, ", interior.externalHumidity())).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("internalTemperature: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.format("%.2f, ", interior.internalTemperature())).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("externalTemperature: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.format("%.2f, ", interior.externalTemperature())).withStyle(ChatFormatting.GREEN)
                        )))))))))))))))))))));

                context.getSource().sendSuccess(() -> message, true);
            }

            return 1;
        }
        catch (Exception e)
        {
            context.getSource().sendFailure(Component.literal("Error: " + e.getLocalizedMessage()));
            return 0;
        }
    }

    private static int executeDebugInterior(CommandContext<CommandSourceStack> context)
    {
        try
        {
            BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
            Interior interior = ThermiaInteriorAPI.getInterior(context.getSource().getLevel(), pos);

            if (!interior.isValid())
            {
                context.getSource().sendFailure(Component.literal("No valid interior found at: " + pos.toShortString()));
                return 0;
            }

            int count = interior.internalAirBlocks().size();

            for (long packedPos : interior.internalAirBlocks())
            {
                BlockPos airPos = BlockPos.of(packedPos);
                context.getSource().getLevel().setBlock(airPos, Blocks.GLASS.defaultBlockState(), 3);
            }

            context.getSource().sendSuccess(() -> Component.literal(String.format("Replaced %d air blocks with glass for interior at %s", count, pos.toShortString())), true);
            return 1;
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
        LiteralArgumentBuilder<CommandSourceStack> interiorCommand = Commands.literal("interior");

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

        interiorCommand.then(Commands.literal("remove").then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ThermiaCommands::executeRemoveInterior)));
        interiorCommand.then(Commands.literal("debug_glass").then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ThermiaCommands::executeDebugInterior)));
        interiorCommand.then(Commands.literal("list_interiors").executes(ThermiaCommands::executeListInteriors));

        baseCommand.then(getCommand);
        baseCommand.then(setCommand);
        baseCommand.then(interiorCommand);
        dispatcher.register(baseCommand);
    }
}
