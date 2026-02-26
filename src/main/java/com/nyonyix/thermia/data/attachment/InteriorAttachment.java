package com.nyonyix.thermia.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.data.Interior;
import net.minecraft.core.BlockPos;

import java.util.Map;

public record InteriorAttachment(Map<BlockPos, Interior> activeInteriors)
{
    public static final Codec<InteriorAttachment> CODEC = RecordCodecBuilder.create(interiorAttachmentInstance -> interiorAttachmentInstance.group(
            Codec.unboundedMap(Codec.STRING.xmap(
                    str -> BlockPos.of(Long.parseLong(str)),
                    pos -> String.valueOf(pos.asLong())
            ), Interior.CODEC).fieldOf("active_interiors").forGetter(InteriorAttachment::activeInteriors)
    ).apply(interiorAttachmentInstance, InteriorAttachment::new));

    public static InteriorAttachment createDefault() {return new InteriorAttachment(Map.of(BlockPos.ZERO, Interior.createDefault()));}

    public InteriorAttachment withActiveInteriors(Map<BlockPos, Interior> activeInteriors) {return new InteriorAttachment(activeInteriors);}
}
