package com.nyonyix.thermia.compat.jade;

import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadeIntegration implements IWailaPlugin
{
    @Override
    public void register(IWailaCommonRegistration registration)
    {
        // TODO
    }

    @Override
    public void registerClient(IWailaClientRegistration registration)
    {
        registration.registerEntityComponent(EntityTemperatureComponentProvider.INSTANCE, LivingEntity.class);
    }
}
