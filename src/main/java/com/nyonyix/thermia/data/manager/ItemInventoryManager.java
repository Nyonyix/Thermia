package com.nyonyix.thermia.data.manager;

import com.nyonyix.thermia.ServerConfig;
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
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemInventoryManager
{
    public static float getInventoryConductionProtection(Entity entity)
    {
        if (!(entity instanceof LivingEntity livingEntity)) return 0.0f;

        float armourInsulation;
        float inventoryInsulation = 0.0f;

        if (entity instanceof Player player)
        {
            float vanillaHead = getConductionProtection(livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem());
            float vanillaBody = getConductionProtection(livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem());
            float vanillaLegs = getConductionProtection(livingEntity.getItemBySlot(EquipmentSlot.LEGS).getItem());
            float vanillaFeet = getConductionProtection(livingEntity.getItemBySlot(EquipmentSlot.FEET).getItem());

            float curioHead = getConductionProtection(CuriosApi.getCuriosInventory(livingEntity).flatMap(h -> h.findCurio("head", 0)).map(SlotResult::stack).orElse(ItemStack.EMPTY).getItem());
            float curioBody = getConductionProtection(CuriosApi.getCuriosInventory(livingEntity).flatMap(h -> h.findCurio("body", 0)).map(SlotResult::stack).orElse(ItemStack.EMPTY).getItem());
            float curioLegs = getConductionProtection(CuriosApi.getCuriosInventory(livingEntity).flatMap(h -> h.findCurio("legs", 0)).map(SlotResult::stack).orElse(ItemStack.EMPTY).getItem());
            float curioFeet = getConductionProtection(CuriosApi.getCuriosInventory(livingEntity).flatMap(h -> h.findCurio("feet", 0)).map(SlotResult::stack).orElse(ItemStack.EMPTY).getItem());
            float curioCape = getConductionProtection(CuriosApi.getCuriosInventory(livingEntity).flatMap(h -> h.findCurio("cape", 0)).map(SlotResult::stack).orElse(ItemStack.EMPTY).getItem());

            armourInsulation = Math.max(vanillaHead, curioHead) + Math.max(vanillaBody, curioBody) + Math.max(vanillaLegs, curioLegs) + Math.max(vanillaFeet, curioFeet) + curioCape;

            for (ItemStack stack : player.getInventory().items)
            {
                if (!stack.isEmpty())
                {
                    if (stack.getItem() instanceof Equipable || stack.getItem() instanceof ICurioItem)
                    {
                        inventoryInsulation += getConductionProtection(stack.getItem()) / 10f;
                    }
                    else inventoryInsulation += getConductionProtection(stack.getItem());
                }
            }

            return Math.clamp((armourInsulation / 4f) + (inventoryInsulation / 36f), -1f, 1f);
        }
        else
        {
            // TODO: Animal Slots
            return 0f;
        }
    }

    public static float getInventoryRadiationProtection(Entity entity)
    {
        if (!(entity instanceof LivingEntity livingEntity)) return 0.0f;

        if (livingEntity instanceof Player player)
        {
            float vanillaHead = getRadiationProtection(player.getItemBySlot(EquipmentSlot.HEAD).getItem());
            float vanillaBody = getRadiationProtection(player.getItemBySlot(EquipmentSlot.CHEST).getItem());
            float vanillaLegs = getRadiationProtection(player.getItemBySlot(EquipmentSlot.LEGS).getItem());
            float vanillaFeet = getRadiationProtection(player.getItemBySlot(EquipmentSlot.FEET).getItem());

            float curioHead = 0.0f;
            float curioBody = 0.0f;
            float curioLegs = 0.0f;
            float curioFeet = 0.0f;
            float curioCape = 0.0f;

            var curios = CuriosApi.getCuriosInventory(player);
            if (curios.isPresent())
            {
                var curio = curios.get();
                if (curios.isPresent())
                {
                    curioHead = curio.findCurio("head", 0).isPresent() ? getRadiationProtection(curio.findCurio("head", 0).get().stack().getItem()) : 0.0f;
                    curioBody = curio.findCurio("body", 0).isPresent() ? getRadiationProtection(curio.findCurio("body", 0).get().stack().getItem()) : 0.0f;
                    curioLegs = curio.findCurio("legs", 0).isPresent() ? getRadiationProtection(curio.findCurio("legs", 0).get().stack().getItem()) : 0.0f;
                    curioFeet = curio.findCurio("feet", 0).isPresent() ? getRadiationProtection(curio.findCurio("feet", 0).get().stack().getItem()) : 0.0f;
                    curioCape = curio.findCurio("cape", 0).isPresent() ? getRadiationProtection(curio.findCurio("cape", 0).get().stack().getItem()) : 0.0f;
                }
            }

            float headValue = Math.max(vanillaHead, curioHead) * 0.35f;
            float bodyValue = Math.max(vanillaBody, curioBody) * 0.50f;
            float legsValue = Math.max(vanillaLegs, curioLegs) * 0.10f;
            float feetValue = Math.max(vanillaFeet, curioFeet) * 0.05f;

            return Math.clamp(curioCape + headValue + bodyValue + legsValue + feetValue, -1f, 1f);
        }
        else
        {
            // TODO: Animal Slots
            return 0f;
        }
    }
    
    public static float getInventoryConvectionProtection(Entity entity)
    {
        if (!(entity instanceof LivingEntity livingEntity)) return 0.0f;

        if (livingEntity instanceof Player player)
        {
            float vanillaHead = getConvectionProtection(player.getItemBySlot(EquipmentSlot.HEAD).getItem());
            float vanillaBody = getConvectionProtection(player.getItemBySlot(EquipmentSlot.CHEST).getItem());
            float vanillaLegs = getConvectionProtection(player.getItemBySlot(EquipmentSlot.LEGS).getItem());
            float vanillaFeet = getConvectionProtection(player.getItemBySlot(EquipmentSlot.FEET).getItem());

            float curioHead = 0.0f;
            float curioBody = 0.0f;
            float curioLegs = 0.0f;
            float curioFeet = 0.0f;
            float curioCape = 0.0f;

            var curios = CuriosApi.getCuriosInventory(player);
            if (curios.isPresent())
            {
                var curio = curios.get();
                if (curios.isPresent())
                {
                    curioHead = curio.findCurio("head", 0).isPresent() ? getConvectionProtection(curio.findCurio("head", 0).get().stack().getItem()) : 0.0f;
                    curioBody = curio.findCurio("body", 0).isPresent() ? getConvectionProtection(curio.findCurio("body", 0).get().stack().getItem()) : 0.0f;
                    curioLegs = curio.findCurio("legs", 0).isPresent() ? getConvectionProtection(curio.findCurio("legs", 0).get().stack().getItem()) : 0.0f;
                    curioFeet = curio.findCurio("feet", 0).isPresent() ? getConvectionProtection(curio.findCurio("feet", 0).get().stack().getItem()) : 0.0f;
                    curioCape = curio.findCurio("cape", 0).isPresent() ? getConvectionProtection(curio.findCurio("cape", 0).get().stack().getItem()) : 0.0f;
                }
            }

            float headValue = Math.max(vanillaHead, curioHead) * 0.15f;
            float bodyValue = Math.max(vanillaBody, curioBody) * 0.50f;
            float legsValue = Math.max(vanillaLegs, curioLegs) * 0.25f;
            float feetValue = Math.max(vanillaFeet, curioFeet) * 0.10f;

            return Math.clamp(curioCape + headValue + bodyValue + legsValue + feetValue, -1f, 1f);
        }
        else
        {
            // TODO: Animal Slots
            return 0f;
        }
    }

    public static float getInventoryRainProtection(Entity entity)
    {
        if (!(entity instanceof LivingEntity livingEntity)) return 0.0f;

        if (livingEntity instanceof Player player)
        {
            float vanillaHead = getRainProtection(player.getItemBySlot(EquipmentSlot.HEAD).getItem());
            float vanillaBody = getRainProtection(player.getItemBySlot(EquipmentSlot.CHEST).getItem());
            float vanillaLegs = getRainProtection(player.getItemBySlot(EquipmentSlot.LEGS).getItem());
            float vanillaFeet = getRainProtection(player.getItemBySlot(EquipmentSlot.FEET).getItem());

            float curioHead = 0.0f;
            float curioBody = 0.0f;
            float curioLegs = 0.0f;
            float curioFeet = 0.0f;
            float curioCape = 0.0f;

            var curios = CuriosApi.getCuriosInventory(player);
            if (curios.isPresent())
            {
                var curio = curios.get();
                if (curios.isPresent())
                {
                    curioHead = curio.findCurio("head", 0).isPresent() ? getRainProtection(curio.findCurio("head", 0).get().stack().getItem()) : 0.0f;
                    curioBody = curio.findCurio("body", 0).isPresent() ? getRainProtection(curio.findCurio("body", 0).get().stack().getItem()) : 0.0f;
                    curioLegs = curio.findCurio("legs", 0).isPresent() ? getRainProtection(curio.findCurio("legs", 0).get().stack().getItem()) : 0.0f;
                    curioFeet = curio.findCurio("feet", 0).isPresent() ? getRainProtection(curio.findCurio("feet", 0).get().stack().getItem()) : 0.0f;
                    curioCape = curio.findCurio("cape", 0).isPresent() ? getRainProtection(curio.findCurio("cape", 0).get().stack().getItem()) : 0.0f;
                }
            }

            float headValue = Math.max(vanillaHead, curioHead) * 0.35f;
            float bodyValue = Math.max(vanillaBody, curioBody) * 0.40f;
            float legsValue = Math.max(vanillaLegs, curioLegs) * 0.15f;
            float feetValue = Math.max(vanillaFeet, curioFeet) * 0.10f;

            return Math.clamp(curioCape + headValue + bodyValue + legsValue + feetValue, -1f, 1f);
        }
        else
        {
            // TODO: Animal Slots
            return 0f;
        }
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
        else
        {
            // TODO: Animal Inventory
            return 0f;
        }

        return Mth.clamp(temperature, -maxEffect, maxEffect);
    }

    public static boolean hasCape(Entity entity)
    {
        if (!(entity instanceof LivingEntity living)) return false;
        return CuriosApi.getCuriosInventory(living).map(iCuriosItemHandler -> !iCuriosItemHandler.findCurios("cape").isEmpty()).orElse(false);
    }

    public static float getConductionProtection(Item item)
    {
        ItemInsulationDataMap data = BuiltInRegistries.ITEM.wrapAsHolder(item).getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);
        if (data == null) return 0f;

        return data.conductionProtection();
    }

    public static float getRadiationProtection(Item item)
    {
        ItemInsulationDataMap data = BuiltInRegistries.ITEM.wrapAsHolder(item).getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);
        if (data == null) return 0f;

        return data.radiationProtection();
    }

    public static float getConvectionProtection(Item item)
    {
        ItemInsulationDataMap data = BuiltInRegistries.ITEM.wrapAsHolder(item).getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);
        if (data == null) return 0f;

        return data.convectionProtection();
    }

    public static float getRainProtection(Item item)
    {
        ItemInsulationDataMap data = BuiltInRegistries.ITEM.wrapAsHolder(item).getData(ThermiaDataMaps.ITEM_INSULATION_DATA_MAP);
        if (data == null) return 0f;

        return data.rainProtection();
    }
}
