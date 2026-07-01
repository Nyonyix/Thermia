package com.nyonyix.thermia.item.thick;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ThermiaThickTorsoItem extends Item implements ICurioItem
{
    private final ThermiaThickMaterial material;

    public ThermiaThickTorsoItem(ThermiaThickMaterial material, Properties properties)
    {
        super(properties);
        this.material = material;
    }

    public ThermiaThickMaterial material()
    {
        return this.material;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack itemStack)
    {
        return "torso".equals(slotContext.identifier());
    }
}
