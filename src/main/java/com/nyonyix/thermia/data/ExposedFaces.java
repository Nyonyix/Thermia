package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;

import java.util.List;

public record ExposedFaces(List<Direction> directions)
{
    public static final Codec<ExposedFaces> CODEC = RecordCodecBuilder.create(exposedFacesInstance -> exposedFacesInstance.group(
            Codec.list(Direction.CODEC).fieldOf("directions").forGetter(ExposedFaces::directions)
    ).apply(exposedFacesInstance, ExposedFaces::new));

    public boolean isEmpty() {return directions.isEmpty();}

    public int count() {return directions.size();}
}
