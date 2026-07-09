package com.nyonyix.thermia.data.manager;

import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.ThermiaTags;
import com.nyonyix.thermia.data.datamap.ItemInsulationDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;

public class ItemInventoryManager
{
    private static float getInsulation(Item item)
    {
        ItemInsulationDataMap data = BuiltInRegistries.ITEM.wrapAsHolder(item).getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);
        if (data == null) return 0f;

        return data.insulationModifier();
    }

    public static float getInventoryInsulation(Entity entity)
    {
        if (!(entity instanceof LivingEntity livingEntity)) return 0.0f;

        float armourInsulation = 0.0f;
        float inventoryInsulation = 0.0f;

        float vanillaHead = getInsulation(livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem());
        float vanillaBody = getInsulation(livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem());
        float vanillaLegs = getInsulation(livingEntity.getItemBySlot(EquipmentSlot.LEGS).getItem());
        float vanillaFeet = getInsulation(livingEntity.getItemBySlot(EquipmentSlot.FEET).getItem());

        float curioHead = getInsulation(CuriosApi.getCuriosInventory(livingEntity).flatMap(h -> h.findCurio("head", 0)).map(SlotResult::stack).orElse(ItemStack.EMPTY).getItem());
        float curioBody = getInsulation(CuriosApi.getCuriosInventory(livingEntity).flatMap(h -> h.findCurio("body", 0)).map(SlotResult::stack).orElse(ItemStack.EMPTY).getItem());
        float curioLegs = getInsulation(CuriosApi.getCuriosInventory(livingEntity).flatMap(h -> h.findCurio("legs", 0)).map(SlotResult::stack).orElse(ItemStack.EMPTY).getItem());
        float curioFeet = getInsulation(CuriosApi.getCuriosInventory(livingEntity).flatMap(h -> h.findCurio("feet", 0)).map(SlotResult::stack).orElse(ItemStack.EMPTY).getItem());

        armourInsulation = Math.max(vanillaHead, curioHead) + Math.max(vanillaBody, curioBody) + Math.max(vanillaLegs, curioLegs) + Math.max(vanillaFeet, curioFeet);

        if (entity instanceof Player player)
        {
            for (ItemStack stack : player.getInventory().items)
            {
                if (!stack.isEmpty())
                {
                    ItemInsulationDataMap insulation = BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);

                    if (insulation != null)
                    {
                        if (stack.getItem() instanceof Equipable) inventoryInsulation += (insulation.insulationModifier() / 10f);
                        else inventoryInsulation += insulation.insulationModifier();
                    }
                }
            }
        }

        return (armourInsulation / 4f) + (inventoryInsulation / 36f);
    }

    public static float getInventoryTemperature(Entity entity)
    {
        float temperature = 0.0f;
        float maxEffect = (float) ServerConfig.PEAK_INVENTORY_TEMPERATURE.getAsInt();
        float multi = (float) ServerConfig.INVENTORY_HEAT_MULTI.getAsDouble();
        if (entity instanceof Player player)
        {
            for (ItemStack stack : player.getInventory().items)
            {
                temperature += HeatCapability.getTemperature(stack) * stack.getCount();
            }

            temperature = (float) Math.pow(temperature, 0.30) * multi;
        }

        return Mth.clamp(temperature, -maxEffect, maxEffect);
    }

    public static boolean hasCape(Entity entity)
    {
        if (!(entity instanceof LivingEntity living)) return false;
        return CuriosApi.getCuriosInventory(living).map(iCuriosItemHandler -> !iCuriosItemHandler.findCurios("cape").isEmpty()).orElse(false);
    }

    public static boolean hasHat(Entity entity)
    {
        if (!(entity instanceof LivingEntity living)) return false;

        var curios = CuriosApi.getCuriosInventory(living);
        if (curios.isPresent())
        {
            var curio = curios.get().findCurio("head", 0);
            if (curio.isPresent())
            {
                ItemStack stack = curio.get().stack();

                return stack.is(ThermiaTags.Items.WIDE_HAT);
            }
        }

        return false;
    }
}
