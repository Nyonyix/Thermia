package com.nyonyix.thermia.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

import java.util.List;

public class ThermiaPeltLootModifier implements IGlobalLootModifier
{
    public static final MapCodec<ThermiaPeltLootModifier> CODEC = RecordCodecBuilder.mapCodec(thermiaPeltLootModifierInstance -> thermiaPeltLootModifierInstance.group(
            LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(m -> m.conditions), BuiltInRegistries.ITEM.byNameCodec().fieldOf("pelt").forGetter(m -> m.peltItem)
    ).apply(thermiaPeltLootModifierInstance, ThermiaPeltLootModifier::new));

    private final List<LootItemCondition> conditions;
    private final Item peltItem;

    public ThermiaPeltLootModifier(List<LootItemCondition> conditions, Item peltItem)
    {
        this.peltItem = peltItem;
        this.conditions = conditions;
    }

    private static boolean isTFCHide(ItemStack stack)
    {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!TerraFirmaCraft.MOD_ID.equals(id.getNamespace())) return false;
        String path = id.getPath();
        return path.contains("raw_hide") || path.contains("sheepskin");
    }

    @Override
    public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
    {
        for (LootItemCondition condition : conditions)
        {
            if (!condition.test(context)) return generatedLoot;
        }

        int hideCount = 0;
        var it = generatedLoot.iterator();
        while (it.hasNext())
        {
            ItemStack stack = it.next();
            if (isTFCHide(stack))
            {
                hideCount += stack.getCount();
                it.remove();
            }
        }

        generatedLoot.add(new ItemStack(peltItem, Math.max(hideCount, 1)));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec()
    {
        return CODEC;
    }
}
