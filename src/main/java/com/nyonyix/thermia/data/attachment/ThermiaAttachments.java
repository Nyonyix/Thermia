package com.nyonyix.thermia.data.attachment;

import com.nyonyix.thermia.Thermia;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ThermiaAttachments
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Thermia.MODID);

    public static final Supplier<AttachmentType<EntityTemperature>> ENTITY_TEMPERATURE = ATTACHMENTS.register("entity_temperature", () -> AttachmentType.builder(EntityTemperature::createDefault).serialize(EntityTemperature.CODEC).sync(EntityTemperature.STREAM_CODEC).build());

    public static final Supplier<AttachmentType<Thermometer>> THERMOMETER = ATTACHMENTS.register("thermometer", () -> AttachmentType.builder(Thermometer::createDefault).serialize(Thermometer.CODEC).sync(Thermometer.STREAM_CODEC).build());

    public static final Supplier<AttachmentType<BlockTemperature>> BLOCK_TEMPERATURE = ATTACHMENTS.register("block_temperature", () -> AttachmentType.builder(BlockTemperature::createDefault).serialize(BlockTemperature.CODEC).build());

    public static final Supplier<AttachmentType<ChunkHumidity>> CHUNK_HUMIDITY = ATTACHMENTS.register("chunk_humidity", () -> AttachmentType.builder(ChunkHumidity::createDefault).serialize(ChunkHumidity.CODEC).build());

    public static final Supplier<AttachmentType<InteriorAttachment>> INTERIOR_ATTACHMENT = ATTACHMENTS.register("interior_attachment", () -> AttachmentType.builder(InteriorAttachment::createDefault).serialize(InteriorAttachment.CODEC).build());

    public static final Supplier<AttachmentType<ClientInteriorAttachment>> CLIENT_INTERIOR_ATTACHMENT = ATTACHMENTS.register("synced_interior_temperature_attachment", () -> AttachmentType.builder(ClientInteriorAttachment::createDefault).sync(ClientInteriorAttachment.STREAM_CODEC).build());

    public static final Supplier<AttachmentType<SyncedInteriorAttachment>> SYNCED_INTERIOR_ATTACHMENT = ATTACHMENTS.register("synced_interior_attachment", () -> AttachmentType.builder(SyncedInteriorAttachment::createDefault).sync(SyncedInteriorAttachment.STREAM_CODEC).build());

}
