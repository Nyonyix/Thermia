package com.nyonyix.thermia.item.wideBrimHat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack held = player.getItemInHand(hand);
        var curios = CuriosApi.getCuriosInventory(player);

        if (curios.isPresent())
        {
            var handler = curios.get().getStacksHandler("head");
            if (handler.isPresent())
            {
                IDynamicStackHandler stacks = handler.get().getStacks();
                ItemStack slotStack = stacks.getStackInSlot(0);
                if (slotStack.isEmpty())
                {
                    if (!level.isClientSide())
                    {
                        stacks.setStackInSlot(0, held.copyWithCount(1));
                        held.shrink(1);
                    }

                    return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
                }
                else
                {
                    if (!level.isClientSide())
                    {
                        player.setItemInHand(hand, slotStack.copy());
                        stacks.setStackInSlot(0, held.copyWithCount(1));
                    }

                    return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
                }
            }
        }

        return InteractionResultHolder.pass(held);
    }
}
