package com.nyonyix.thermia.api;

import com.nyonyix.thermia.data.records.Interior;
import com.nyonyix.thermia.data.attachment.InteriorAttachment;
import com.nyonyix.thermia.data.attachment.ThermiaAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class ThermiaInteriorAPI
{
    public static boolean isInInterior(Level level, BlockPos pos)
    {
        return getInteriorByPos(level, pos).isValid();
    }

    public static boolean isInInterior(BlockPos pos, Interior interior)
    {
        if (interior.boundingBox().contains(Vec3.atCenterOf(pos)))
        {
            return interior.internalAirBlocks().contains(pos.asLong()) || interior.interiorBlocks().edgeBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSourceBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSinkBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSourceFluids.containsKey(pos.asLong()) || interior.interiorBlocks().heatSinkFluids.containsKey(pos.asLong());
        }

        return false;
    }

    public static Interior getInteriorByPos(Level level, BlockPos pos)
    {
        if (!level.hasData(ThermiaAttachments.INTERIOR_ATTACHMENT)) return Interior.createDefault();

        for (Interior interior : level.getData(ThermiaAttachments.INTERIOR_ATTACHMENT).activeInteriors().values())
        {
            if (!interior.isValid()) continue;

            if (interior.boundingBox().contains(Vec3.atCenterOf(pos)))
            {
                if (interior.internalAirBlocks().contains(pos.asLong()) || interior.interiorBlocks().edgeBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSourceBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSinkBlocks.containsKey(pos.asLong()) || interior.interiorBlocks().heatSourceFluids.containsKey(pos.asLong()) || interior.interiorBlocks().heatSinkFluids.containsKey(pos.asLong())) return interior;
            }
        }

        return Interior.createDefault();
    }

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
