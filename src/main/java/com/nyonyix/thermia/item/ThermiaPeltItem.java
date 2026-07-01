package com.nyonyix.thermia.item;

import com.nyonyix.thermia.item.cape.ThermiaCapeAnimal;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ThermiaPeltItem extends Item implements ICurioItem
{
    private final ThermiaCapeAnimal animal;

    public ThermiaPeltItem(ThermiaCapeAnimal animal, Properties properties)
    {
        super(properties);
        this.animal = animal;
    }

    public ThermiaCapeAnimal animal()
    {
        return animal;
    }
}
