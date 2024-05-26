package net.notajunkie.acrosstheend.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.util.ModTags;

import java.util.List;

public class ModToolTiers {
    public static final Tier AMETHYST_INFUSED_DIAMOND = TierSortingRegistry.registerTier(
        new ForgeTier(3, 1760, 8.4f, 3f, 12,
        ModTags.Blocks.NEEDS_AMETHYST_INFUSED_TOOL, () -> Ingredient.of(ModItems.INFUSED_AMETHYST_SHARD.get())),
        new ResourceLocation(AcrossTheEnd.MOD_ID, "amethyst_infused_diamond"), List.of(Tiers.DIAMOND), List.of()
    );

    public static final Tier AMETHYST_INFUSED_NETHERITE = TierSortingRegistry.registerTier(
            new ForgeTier(5, 2564, 10f, 4f, 17,
                    ModTags.Blocks.NEEDS_AMETHYST_INFUSED_TOOL, () -> Ingredient.of(ModItems.INFUSED_AMETHYST_SHARD.get())),
            new ResourceLocation(AcrossTheEnd.MOD_ID, "amethyst_infused_netherite"), List.of(Tiers.NETHERITE), List.of()
    );
}
