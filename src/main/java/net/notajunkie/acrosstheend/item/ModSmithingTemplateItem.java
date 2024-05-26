package net.notajunkie.acrosstheend.item;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;
import net.notajunkie.acrosstheend.AcrossTheEnd;

import java.util.List;

public class ModSmithingTemplateItem extends SmithingTemplateItem {

    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;


    private static final Component PULL_UPGRADE = Component.translatable(
            Util.makeDescriptionId("upgrade", new ResourceLocation(AcrossTheEnd.MOD_ID,"smithing_template.void_grasp_upgrade"))).withStyle(TITLE_FORMAT);
    private static final Component PULL_UPGRADE_APPLIES_TO = Component.translatable(
            Util.makeDescriptionId("item", new ResourceLocation(AcrossTheEnd.MOD_ID, "smithing_template.void_grasp_upgrade.applies_to"))).withStyle(DESCRIPTION_FORMAT);
    private static final Component PULL_UPGRADE_INGREDIENTS = Component.translatable(
            Util.makeDescriptionId("item", new ResourceLocation(AcrossTheEnd.MOD_ID,"smithing_template.void_grasp_upgrade.ingredients"))).withStyle(DESCRIPTION_FORMAT);
    private static final Component PULL_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(
            Util.makeDescriptionId("item", new ResourceLocation(AcrossTheEnd.MOD_ID,"smithing_template.void_grasp_upgrade.base_slot_description")));
    private static final Component PULL_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
            Util.makeDescriptionId("item", new ResourceLocation(AcrossTheEnd.MOD_ID,"smithing_template.void_grasp_upgrade.additions_slot_description")));

    private static final ResourceLocation EMPTY_SLOT_SHARD = new ResourceLocation("item/empty_slot_amethyst_shard");
    private static final ResourceLocation EMPTY_SLOT_HOE = new ResourceLocation("item/empty_slot_hoe");
    private static final ResourceLocation EMPTY_SLOT_SHOVEL = new ResourceLocation("item/empty_slot_shovel");
    private static final ResourceLocation EMPTY_SLOT_AXE = new ResourceLocation("item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SLOT_SWORD = new ResourceLocation("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE = new ResourceLocation("item/empty_slot_pickaxe");

    public ModSmithingTemplateItem(Component p_266834_, Component p_267043_, Component p_267048_, Component p_267278_, Component p_267090_, List<ResourceLocation> p_266755_, List<ResourceLocation> p_267060_) {
        super(p_266834_, p_267043_, p_267048_, p_267278_, p_267090_, p_266755_, p_267060_);
    }

    public static SmithingTemplateItem createVoidGraspUpgradeTemplate() {
        return new SmithingTemplateItem(PULL_UPGRADE_APPLIES_TO, PULL_UPGRADE_INGREDIENTS, PULL_UPGRADE, PULL_UPGRADE_BASE_SLOT_DESCRIPTION, PULL_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
                createVoidGraspUpgradeIconList(), createVoidGraspUpgradeMaterialList());
    }

    private static List<ResourceLocation> createVoidGraspUpgradeIconList() {
        return List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_AXE, EMPTY_SLOT_HOE, EMPTY_SLOT_SHOVEL);
    }

    private static List<ResourceLocation> createVoidGraspUpgradeMaterialList() {
        return List.of(EMPTY_SLOT_SHARD);
    }
}