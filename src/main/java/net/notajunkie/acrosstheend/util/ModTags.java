package net.notajunkie.acrosstheend.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.notajunkie.acrosstheend.AcrossTheEnd;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> NEEDS_AMETHYST_INFUSED_TOOL = tag("needs_amethyst_infused_tool");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(AcrossTheEnd.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> AMETHYST_INFUSED_DIAMOND_TOOLS = tag("amethyst_infused_diamond_tools");
        public static final TagKey<Item> AMETHYST_INFUSED_NETHERITE_TOOLS = tag("amethyst_infused_netherite_tools");
        public static final TagKey<Item> AMETHYST_INFUSED_WEAPONS = tag("amethyst_infused_netherite_weapons");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(AcrossTheEnd.MOD_ID, name));
        }
    }


}
