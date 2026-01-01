package com.nyonyix.thermia.data.manager;

import com.nyonyix.thermia.ServerConfig;
import com.nyonyix.thermia.data.map.ItemInsulationDataMap;
import com.nyonyix.thermia.data.map.ThermiaDataMaps;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

public class ItemInventoryManager
{
    public static float getInventoryInsulation(Entity entity)
    {
        if (!(entity instanceof LivingEntity livingEntity)) return 0.0f;

        float armourInsulation = 0.0f;
        float inventoryInsulation = 0.0f;

        for (ItemStack armour : livingEntity.getArmorSlots())
        {
            if (!armour.isEmpty())
            {
                ItemInsulationDataMap insulation = BuiltInRegistries.ITEM.wrapAsHolder(armour.getItem()).getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);

                if (insulation != null) armourInsulation += insulation.insulationModifier();


            }
        }

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

        return (armourInsulation / 4f) + (inventoryInsulation / 18f);
    }

    public static float getInventoryTemperature(Entity entity)
    {
        float temperature = 0.0f;
        if (entity instanceof Player player)
        {
            for (ItemStack stack : player.getInventory().items)
            {
                temperature += HeatCapability.getTemperature(stack) * stack.getCount();
            }

            float maxEffect = (float) ServerConfig.MAX_INVENTORY_HEATING.getAsDouble();
            float scale = (float) ServerConfig.INVENTORY_HEATING_SCALE.getAsDouble();

            temperature = maxEffect * (temperature / (temperature + scale));
        }

        return temperature;
    }
}
