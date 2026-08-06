package com.nyonyix.thermia.data.datagen.datamap;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.oven.OvenType;
import com.nyonyix.thermia.data.datamap.BlockTemperatureDataMap;
import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockTemperatureDataMapProvider extends DataMapProvider
{
    public BlockTemperatureDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {super(packOutput, lookupProvider);}

    @Override
    protected void gather()
    {
        builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).replace(true)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/granite")).orElseThrow(), new BlockTemperatureDataMap(800f, 32, false,true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/diorite")).orElseThrow(), new BlockTemperatureDataMap(800f, 32, false, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/gabbro")).orElseThrow(), new BlockTemperatureDataMap(800f, 32, false, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/rhyolite")).orElseThrow(), new BlockTemperatureDataMap(800f, 32, false, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/andesite")).orElseThrow(), new BlockTemperatureDataMap(800f, 32, false, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/dacite")).orElseThrow(), new BlockTemperatureDataMap(800f, 32, false, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:rock/magma/basalt")).orElseThrow(), new BlockTemperatureDataMap(800f, 32, false, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("minecraft:magma_block")).orElseThrow(), new BlockTemperatureDataMap(800f, 32, false, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:pit_kiln")).orElseThrow(), new BlockTemperatureDataMap(1800f, 32, false, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("minecraft:fire")).orElseThrow(), new BlockTemperatureDataMap(600f, 32, false, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:burning_log_pile")).orElseThrow(), new BlockTemperatureDataMap(900f, 32, false, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:bloomery")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, false, true, Map.of("lit=true", 1200f)), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:molten")).orElseThrow(), new BlockTemperatureDataMap(0f, 16, false, true, Map.of("lit=true", 1540f)), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:crucible")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:firepit")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:pot")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:grill")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:blast_furnace")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:charcoal_forge")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:stove")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:stove_pot")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("tfc:firebox")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false)

                .add(BlockTags.SNOW, new BlockTemperatureDataMap(-3f, 4, false, false, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("minecraft:ice")).orElseThrow(), new BlockTemperatureDataMap(-5f, 4, false, false, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("minecraft:packed_ice")).orElseThrow(), new BlockTemperatureDataMap(-7.5f, 4, false, false, Map.of()), false)
                .add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("minecraft:blue_ice")).orElseThrow(), new BlockTemperatureDataMap(-10f, 4, false, false, Map.of()), false);

        String fl_id = "firmalife";
        if (ModList.get().isLoaded(fl_id))
        {
            for (Map.Entry<OvenType, TFCBlocks.Id<Block>> entry : FLBlocks.CURED_OVEN_BOTTOM.entrySet())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getValue().get());
                builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(blockHolder, new BlockTemperatureDataMap(0f, 32, false, true, Map.of("lit=true", 600f)), false, new ModLoadedCondition(fl_id));
            }

            for (Map.Entry<OvenType, TFCBlocks.Id<Block>> entry : FLBlocks.INSULATED_OVEN_BOTTOM.entrySet())
            {
                Holder<Block> blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getValue().get());
                builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(blockHolder, new BlockTemperatureDataMap(0f, 32, false, true, Map.of("lit=true", 600f)), false, new ModLoadedCondition(fl_id));
            }
        }

        String pg_id = "powergrid";
        if (ModList.get().isLoaded(pg_id))
        {
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:battery")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:heating_coil")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:basin_heater")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:wire_connector")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:heavy_wire_connector")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:voltage_gauge")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:current_gauge")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:power_gauge")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:generator_induction_rotor")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:generator_commutator")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:generator_vertical_commutator")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:generator_clutch")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:generator_housing")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:vertical_generator_housing")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:mv_switch")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:hv_switch")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:hv_breaker")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:spark_gap")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:contactor")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:power_resistor")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:transformer_core")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:variac")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:electric_motor")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:constant_speed_motor")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:servo")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:electromagnet")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:device_connector")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:rheostat")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("powergrid:carbon_pile_coil")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, true, true, Map.of()), false, new ModLoadedCondition(pg_id));
        }

        String create_id = "create";
        if (ModList.get().isLoaded(create_id))
        {
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:blaze_burner")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of("blaze=smouldering", 375f, "blaze=kindled", 750f, "blaze=seething", 1500f)), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:basin")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:fluid_pipe")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:glass_fluid_pipe")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:mechanical_pump")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:smart_fluid_pipe")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:fluid_valve")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:fluid_tank")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:creative_fluid_tank")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:hose_pulley")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:item_drain")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:spout")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:portable_fluid_interface")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
            builder(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP).add(BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse("create:steam_engine")).orElseThrow(), new BlockTemperatureDataMap(0f, 32, false, true, Map.of()), false, new ModLoadedCondition(create_id));
        }
    }

    @Override
    public @NotNull String getName() { return "Block_Temperature_DataMap";}
}
