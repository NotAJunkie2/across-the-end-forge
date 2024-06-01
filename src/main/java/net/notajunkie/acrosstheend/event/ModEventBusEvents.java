package net.notajunkie.acrosstheend.event;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.entity.ModEntities;
import net.notajunkie.acrosstheend.entity.custom.Enderfly;

@Mod.EventBusSubscriber(modid = AcrossTheEnd.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ENDERFLY.get(), Enderfly.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(
            ModEntities.ENDERFLY.get(),
            SpawnPlacements.Type.NO_RESTRICTIONS,
            Heightmap.Types.WORLD_SURFACE,
            EnderMan::checkMobSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }
}
