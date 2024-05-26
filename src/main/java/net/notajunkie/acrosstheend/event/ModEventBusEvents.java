package net.notajunkie.acrosstheend.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.entity.ModEntities;
import net.notajunkie.acrosstheend.entity.custom.EnderflyEntity;

@Mod.EventBusSubscriber(modid = AcrossTheEnd.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ENDERFLY.get(), EnderflyEntity.createAttributes().build());
    }
}
