package net.notajunkie.acrosstheend.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.item.ModItems;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
//    private static final List<ItemLike> BLASTABLES = List.of(ModItems.ITEM.get());
    private static final List<ItemLike> END_TEAR_RECIPE = List.of(ModItems.ENDERFLY_ESSENCE.get());

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        oreBlasting(pWriter, END_TEAR_RECIPE, RecipeCategory.MISC, ModItems.END_TEAR.get(), 2.5f, 320, "enderfly_essence");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.VOID_GRASP_UPGRADE_SMITHING_TEMPLATE.get(), 2)
                .pattern("#S#")
                .pattern("#C#")
                .pattern("###")
                .define('S', ModItems.VOID_GRASP_UPGRADE_SMITHING_TEMPLATE.get())
                .define('C', ModItems.ENDERFLY_ESSENCE.get())
                .define('#', Items.DIAMOND)
                .unlockedBy(getHasName(ModItems.VOID_GRASP_UPGRADE_SMITHING_TEMPLATE.get()), has(ModItems.VOID_GRASP_UPGRADE_SMITHING_TEMPLATE.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.INFUSED_AMETHYST_SHARD.get(), 1)
                .requires(ModItems.END_TEAR.get()).requires(Items.AMETHYST_SHARD).requires(Items.ENDER_EYE)
                .unlockedBy(getHasName(ModItems.INFUSED_AMETHYST_SHARD.get()), has(ModItems.END_TEAR.get()))
                .save(pWriter);
    }

    protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime,
                    pCookingSerializer).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike)).save(pFinishedRecipeConsumer,
                    AcrossTheEnd.MOD_ID + ":" + (pResult) + pRecipeName + "_" + getItemName(itemlike));
        }

    }
}
