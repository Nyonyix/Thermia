package com.nyonyix.thermia.item.wideBrimHat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ThermiaWideBrimHatItem extends Item implements ICurioItem
{
    private final ThermiaWideBrimHatMaterial material;

    public ThermiaWideBrimHatItem(ThermiaWideBrimHatMaterial material, Properties properties)
    {
        super(properties);
        this.material = material;
    }

    public ThermiaWideBrimHatMaterial material()
    {
        return this.material;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack itemStack)
    {
        return "head".equals(slotContext.identifier());
    }
}
