package net.notajunkie.acrosstheend.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.block.ModBlocks;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AcrossTheEnd.MOD_ID);

    public static final RegistryObject<CreativeModeTab> BEFORE_THE_END_TAB = CREATIVE_MODE_TABS.register(
    "across_the_end", () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.ENDERFLY_ESSENCE.get()))
            .title(Component.translatable("creativetab.across_the_end_tab"))
            .displayItems((pParameters, pOutput) -> {
                // Items
                pOutput.accept(ModItems.ENDERFLY_ESSENCE.get());
                pOutput.accept(ModItems.END_TEAR.get());
                pOutput.accept(ModItems.INFUSED_AMETHYST_SHARD.get());

                // Smithing templates
                pOutput.accept(ModItems.DORMANT_VOID_GRASP_UPGRADE_SMITHING_TEMPLATE.get());
                pOutput.accept(ModItems.VOID_GRASP_UPGRADE_SMITHING_TEMPLATE.get());

                // Tools
                pOutput.accept(ModItems.AMETHYST_INFUSED_DIAMOND_SWORD.get());
                pOutput.accept(ModItems.AMETHYST_INFUSED_DIAMOND_PICKAXE.get());
                pOutput.accept(ModItems.AMETHYST_INFUSED_DIAMOND_AXE.get());
                pOutput.accept(ModItems.AMETHYST_INFUSED_DIAMOND_SHOVEL.get());
                pOutput.accept(ModItems.AMETHYST_INFUSED_DIAMOND_HOE.get());

                pOutput.accept(ModItems.AMETHYST_INFUSED_NETHERITE_SWORD.get());
                pOutput.accept(ModItems.AMETHYST_INFUSED_NETHERITE_PICKAXE.get());
                pOutput.accept(ModItems.AMETHYST_INFUSED_NETHERITE_AXE.get());
                pOutput.accept(ModItems.AMETHYST_INFUSED_NETHERITE_SHOVEL.get());
                pOutput.accept(ModItems.AMETHYST_INFUSED_NETHERITE_HOE.get());

                // Ranged weapons
                pOutput.accept(ModItems.AMETHYST_INFUSED_BOW.get());
                pOutput.accept(ModItems.AMETHYST_INFUSED_CROSSBOW.get());

                // Spawn eggs
                pOutput.accept(ModItems.ENDERFLY_SPAWN_EGG.get());

                // Blocks
                pOutput.accept(ModBlocks.TEST_BLOCK.get());
            })
            .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}