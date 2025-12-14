package com.nyonyix.thermia.data.attachment;

import com.nyonyix.thermia.Thermia;
import com.nyonyix.thermia.data.ItemInsulation;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.monster.piglin.StartAdmiringItemIfSeen;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ThermiaAttachments
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Thermia.MODID);

    public static final Supplier<AttachmentType<EntityTemperature>> ENTITY_TEMPERATURE = ATTACHMENTS.register("entity_temperature", () -> AttachmentType.builder(EntityTemperature::createDefault).serialize(EntityTemperature.CODEC).sync(EntityTemperature.STREAM_CODEC).build());

    public static final Supplier<AttachmentType<BlockTemperature>> BLOCK_TEMPERATURE = ATTACHMENTS.register("block_temperature", () -> AttachmentType.builder(BlockTemperature::createDefault).serialize(BlockTemperature.CODEC).sync(BlockTemperature.STREAM_CODEC).build());

    public static final Supplier<AttachmentType<ItemInsulation>> ITEM_INSULATION = ATTACHMENTS.register("item_insulation", () -> AttachmentType.builder(ItemInsulation::createDefault).serialize(ItemInsulation.CODEC).sync(ItemInsulation.STREAM_CODEC).build());

    public static final Supplier<AttachmentType<ChunkClimate>> CHUNK_CLIMATE = ATTACHMENTS.register("chunk_climate", () -> AttachmentType.builder(ChunkClimate::createDefault).serialize(ChunkClimate.CODEC).sync(ChunkClimate.STREAM_CODEC).build());
}
