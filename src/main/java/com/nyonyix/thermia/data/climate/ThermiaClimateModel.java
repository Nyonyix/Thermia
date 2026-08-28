package com.nyonyix.thermia.data.climate;

import com.nyonyix.thermia.api.ThermiaInteriorAPI;
import io.netty.buffer.ByteBuf;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.ClimateModelType;
import net.dries007.tfc.util.climate.OverworldClimateModel;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public final class ThermiaClimateModel extends OverworldClimateModel
{
    public ThermiaClimateModel(ServerLevel level, ChunkGeneratorExtension gen) {super(level, gen);}
    protected ThermiaClimateModel(long climateSeed, float temperatureScale) {super(climateSeed, temperatureScale);}
    public static final StreamCodec<ByteBuf, ThermiaClimateModel> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, m -> m.climateSeed,
            ByteBufCodecs.FLOAT, m -> m.temperatureScale,
            ThermiaClimateModel::new
    );

    @Override
    public float getInstantTemperature(LevelReader level, BlockPos pos, long calenderTicks, int daysInMonth)
    {
        if (level instanceof Level realLevel)
        {
            float interiorTemp = ThermiaInteriorAPI.getInteriorTemperature(realLevel, pos);
            if (!Float.isNaN(interiorTemp))
            {
                return interiorTemp;
            }
        }

        return super.getInstantTemperature(level, pos, calenderTicks, daysInMonth);
    }

    @Override
    public float getInstantTemperature(LevelReader level, BlockPos pos) {
        ICalendar calendar = Calendars.get(level);
        return this.getInstantTemperature(level, pos, calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());
    }

    @Override
    public ClimateModelType<?> type()
    {
        return ThermiaClimateModels.THERMIA.get();
    }

    public float getRawInstantTemperature(LevelReader level, BlockPos pos)
    {
        ICalendar calendar = Calendars.get(level);
        return super.getInstantTemperature(level, pos, calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());
    }
}
