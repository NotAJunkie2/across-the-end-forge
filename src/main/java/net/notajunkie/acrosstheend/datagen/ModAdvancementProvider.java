package net.notajunkie.acrosstheend.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.item.ModItems;
import net.notajunkie.acrosstheend.util.ModTags;

import java.util.function.Consumer;

public class ModAdvancementProvider implements ForgeAdvancementProvider.AdvancementGenerator {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<Advancement> saver, ExistingFileHelper existingFileHelper) {
        Advancement rootAdvancement = Advancement.Builder.advancement()
            .display(new DisplayInfo(
                new ItemStack(ModItems.ENDERFLY_ESSENCE.get()),
                Component.translatable("advancement.root_title"),
                Component.translatable("advancement.root_description"),
                new ResourceLocation("minecraft", "textures/block/end_stone.png"),
                FrameType.TASK,
                true,
                true,
                false)
            )
            .addCriterion("has_enderfly_essence", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERFLY_ESSENCE.get()))
            .save(saver, new ResourceLocation(AcrossTheEnd.MOD_ID, "across_the_end"), existingFileHelper)
        ;

        Advancement acquireAmethystInfusedToolOrWeapon = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(ModItems.VOID_GRASP_UPGRADE_SMITHING_TEMPLATE.get()),
                        Component.translatable("advancement.nap_time_title"),
                        Component.translatable("advancement.nap_time_description"),
                        null,
                        FrameType.GOAL,
                        true,
                        true,
                        false)
                )
                .parent(rootAdvancement)
                .addCriterion("obtain_void_grasp_upgrade", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ModItems.DORMANT_VOID_GRASP_UPGRADE_SMITHING_TEMPLATE.get()
                ))
                .save(saver, new ResourceLocation(AcrossTheEnd.MOD_ID, "void_grasp_upgrade"), existingFileHelper)
                ;
    }
}
