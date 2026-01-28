package com.nyonyix.thermia.ai.behaviours;

import com.mojang.logging.LogUtils;
import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.ai.memories.ThermiaMemoryModules;
import com.nyonyix.thermia.data.attachment.EntityTemperature;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.util.AiHelpers;
import net.dries007.tfc.util.calendar.Calendar;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.ICancellableEvent;
import org.apache.logging.log4j.core.jmx.Server;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

public class StayAtComfort extends Behavior<PathfinderMob>
{
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final float COMFORT_THRESHOLD = AiHelpers.COMFORT_THRESHOLD;

    private BlockPos comfortPos;
    private long arrivalTime;

    public StayAtComfort() {super(Map.of(ThermiaMemoryModules.IS_SEEKING_WARM.get(), MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT), Integer.MAX_VALUE);}

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob)
    {
        if (!mob.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        Optional<WalkTarget> target = mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        if (target.isEmpty()) return false;

        BlockPos targetPos = target.get().getTarget().currentBlockPosition();
        return AiHelpers.isAtPosition(mob, targetPos);
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob mob, long gameTime)
    {
        comfortPos = mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET).map(walkTarget -> walkTarget.getTarget().currentBlockPosition()).orElse(null);

        ICalendar calendar = Calendars.get(level);
        arrivalTime = calendar.getCalendarTicks();

        mob.getNavigation().stop();

        LOGGER.debug("Entity {}: Staying at {} at calendar time {}", mob.getType().getDescriptionId(), comfortPos, arrivalTime);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob mob, long gameTime)
    {
        if (comfortPos == null) return false;
        if (!mob.hasData(ThermiaAttachments.ENTITY_TEMPERATURE)) return false;

        EntityTemperature tempData = mob.getData(ThermiaAttachments.ENTITY_TEMPERATURE);
        float[] thresholds = AiHelpers.getComfortThresholds(tempData, COMFORT_THRESHOLD);
        float currentInternalTemperature = tempData.internalTemperature();

        if (currentInternalTemperature >= thresholds[0] && currentInternalTemperature <= thresholds[1])
        {
            LOGGER.debug("Entity {}: Now Comfortable", mob.getType().getDescriptionId());
            return false;
        }

        if (!AiHelpers.isAtPosition(mob, comfortPos))
        {
            LOGGER.debug("Entity {}: Wandered from pos {}", mob.getType().getDescriptionId(), comfortPos);
            return false;
        }

        int maxStayMinutes = ServerConfig.MAX_STAY_TIME_MINUTES.getAsInt();
        long maxStayTicks = (long) (Calendar.CALENDAR_TICKS_IN_HOUR / 60) * maxStayMinutes;
        long currentTime = Calendars.get(level).getCalendarTicks();

        if (currentTime - arrivalTime > maxStayTicks)
        {
            LOGGER.debug("Entity {}: Timeout after {} minutes", mob.getType().getDescriptionId(), maxStayMinutes);
            return false;
        }

        return true;
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob mob, long gameTime)
    {
        if (!mob.getNavigation().isDone()) mob.getNavigation().stop();

        if (gameTime % 40 == 0) mob.setYRot(mob.getYRot() + (mob.getRandom().nextFloat() - 0.5f) * 20f);
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob mob, long gameTime)
    {
        long stayDuration = Calendars.get(level).getCalendarTicks() - arrivalTime;
        long minutesStayed = (Calendar.CALENDAR_TICKS_IN_HOUR / 60) / stayDuration;

        LOGGER.debug("Entity {}: Stopped staying after {} minutes", mob.getType().getDescriptionId(), minutesStayed);

        comfortPos = null;
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        mob.getBrain().setActiveActivityIfPossible(Activity.IDLE);
    }
}
