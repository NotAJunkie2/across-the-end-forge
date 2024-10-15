package net.notajunkie.acrosstheend.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AcrossTheEnd.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Items
        simpleItem(ModItems.ENDERFLY_ESSENCE);
        simpleItem(ModItems.END_TEAR);
        simpleItem(ModItems.INFUSED_AMETHYST_SHARD);
        simpleItem(ModItems.DORMANT_VOID_GRASP_UPGRADE_SMITHING_TEMPLATE);
        // Smithing templates
        simpleSmithingTemplate(ModItems.VOID_GRASP_UPGRADE_SMITHING_TEMPLATE);
        // Amethyst infused diamond tools
        handheldItem(ModItems.AMETHYST_INFUSED_DIAMOND_SWORD);
        handheldItem(ModItems.AMETHYST_INFUSED_DIAMOND_PICKAXE);
        handheldItem(ModItems.AMETHYST_INFUSED_DIAMOND_AXE);
        handheldItem(ModItems.AMETHYST_INFUSED_DIAMOND_HOE);
        handheldItem(ModItems.AMETHYST_INFUSED_DIAMOND_SHOVEL);
        // Amethyst infused netherite tools
        handheldItem(ModItems.AMETHYST_INFUSED_NETHERITE_SWORD);
        handheldItem(ModItems.AMETHYST_INFUSED_NETHERITE_PICKAXE);
        handheldItem(ModItems.AMETHYST_INFUSED_NETHERITE_AXE);
        handheldItem(ModItems.AMETHYST_INFUSED_NETHERITE_HOE);
        handheldItem(ModItems.AMETHYST_INFUSED_NETHERITE_SHOVEL);

        withExistingParent(ModItems.ENDERFLY_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(AcrossTheEnd.MOD_ID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder handheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/handheld")).texture("layer0",
                new ResourceLocation(AcrossTheEnd.MOD_ID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItem(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(AcrossTheEnd.MOD_ID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleSmithingTemplate(RegistryObject<SmithingTemplateItem> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(AcrossTheEnd.MOD_ID, "item/" + item.getId().getPath()));
    }
}
