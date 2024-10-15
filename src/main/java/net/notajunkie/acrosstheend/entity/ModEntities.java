package net.notajunkie.acrosstheend.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.entity.custom.Enderfly;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AcrossTheEnd.MOD_ID);

    public static final RegistryObject<EntityType<Enderfly>> ENDERFLY =
            ENTITY_TYPES.register("enderfly", () -> EntityType.Builder.of(Enderfly::new,
                    MobCategory.CREATURE).sized(0.25f, 0.25f).build("enderfly"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
