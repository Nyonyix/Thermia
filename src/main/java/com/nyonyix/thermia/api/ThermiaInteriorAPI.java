package com.nyonyix.thermia.api;

import com.nyonyix.thermia.data.Interior;
import com.nyonyix.thermia.data.attachment.InteriorAttachment;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import com.nyonyix.thermia.data.manager.InteriorManager;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ThermiaInteriorAPI
{

    public static boolean isInInterior(Level level, BlockPos pos) {return InteriorManager.isInInterior(level, pos);}

    public static boolean isInInterior(BlockPos pos, Interior interior) {return InteriorManager.isInInterior(pos, interior);}

    public static Interior getInteriorByPos(Level level, BlockPos pos) {return InteriorManager.getInteriorByPos(level, pos);}

    public static Interior getInterior(Level level, BlockPos homePos)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT)) return Interior.createDefault();

        return level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors().getOrDefault(homePos, Interior.createDefault());
    }

    public static Map<BlockPos, Interior> getAllInteriors(Level level)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT)) return Map.of();

        return level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors();
    }

    public static void addInterior(Level level, BlockPos homePos, Interior interior)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT)) return;

        Map<BlockPos, Interior> interiors = new HashMap<>(level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors());

        interiors.put(homePos, interior);

        level.setData(ThermiaAttachments.INTERIOR_ATTACHMENT, new InteriorAttachment(interiors));
    }

    public static void removeInterior(Level level, BlockPos homePos)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT)) return;

        Map<BlockPos, Interior> interiors = new HashMap<>(level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors());
        Interior interior = interiors.get(homePos);

        interior = interior.withIsValid(false);
        interiors.put(homePos, interior);

        level.setData(ThermiaAttachments.INTERIOR_ATTACHMENT, new InteriorAttachment(interiors));
    }
}
