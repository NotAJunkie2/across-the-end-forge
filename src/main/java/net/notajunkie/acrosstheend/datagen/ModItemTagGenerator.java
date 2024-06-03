package net.notajunkie.acrosstheend.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.item.ModItems;
import net.notajunkie.acrosstheend.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagGenerator extends ItemTagsProvider {
    public ModItemTagGenerator(PackOutput pOutput,
       CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags,
       @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, AcrossTheEnd.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(ModTags.Items.AMETHYST_INFUSED_DIAMOND_TOOLS)
                .add(ModItems.AMETHYST_INFUSED_DIAMOND_PICKAXE.get())
                .add(ModItems.AMETHYST_INFUSED_DIAMOND_AXE.get())
                .add(ModItems.AMETHYST_INFUSED_DIAMOND_SHOVEL.get())
                .add(ModItems.AMETHYST_INFUSED_DIAMOND_HOE.get())
        ;

        this.tag(ModTags.Items.AMETHYST_INFUSED_NETHERITE_TOOLS)
                .add(ModItems.AMETHYST_INFUSED_NETHERITE_PICKAXE.get())
                .add(ModItems.AMETHYST_INFUSED_NETHERITE_AXE.get())
                .add(ModItems.AMETHYST_INFUSED_NETHERITE_SHOVEL.get())
                .add(ModItems.AMETHYST_INFUSED_NETHERITE_HOE.get())
        ;

        this.tag(ModTags.Items.AMETHYST_INFUSED_WEAPONS)
                .add(ModItems.AMETHYST_INFUSED_NETHERITE_SWORD.get())
                .add(ModItems.AMETHYST_INFUSED_DIAMOND_SWORD.get())
                .add(ModItems.AMETHYST_INFUSED_BOW.get())
                .add(ModItems.AMETHYST_INFUSED_CROSSBOW.get())
        ;
    }
}
