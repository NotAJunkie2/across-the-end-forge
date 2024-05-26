package net.notajunkie.acrosstheend.event;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.entity.client.EnderflyModel;
import net.notajunkie.acrosstheend.entity.client.ModModelLayers;

@Mod.EventBusSubscriber(modid = AcrossTheEnd.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    @SubscribeEvent
    public static  void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.ENDERFLY, EnderflyModel::createBodyLayer);
    }
}
