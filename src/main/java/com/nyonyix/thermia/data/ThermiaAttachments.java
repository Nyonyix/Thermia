package com.nyonyix.thermia.data;

import com.nyonyix.thermia.Thermia;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ThermiaAttachments
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Thermia.MODID);

    public static final Supplier<AttachmentType<EntityTemperature>> ENTITY_TEMPERATURE = ATTACHMENTS.register("entity_temperature", () -> AttachmentType.builder(EntityTemperature::createDefault).serialize(EntityTemperature.CODEC).sync(EntityTemperature.STREAM_CODEC).build());
}
