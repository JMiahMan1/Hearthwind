package net.satisfy.farm_and_charm.core.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/** 26.2 helper: old Recipe.getResultItem(Provider) is gone; our 6 recipe
 * classes keep a public no-arg getResultItem() with identical contents. */
public final class RecipeDisplays26 {
    private RecipeDisplays26() {}

    public static net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> ingredientsOf(Recipe<?> recipe) {
        if (recipe instanceof SiloRecipe r) return r.getIngredients();
        if (recipe instanceof MincerRecipe r) return r.getIngredients();
        if (recipe instanceof CraftingBowlRecipe r) return r.getIngredients();
        if (recipe instanceof CookingPotRecipe r) return r.getIngredients();
        if (recipe instanceof RoasterRecipe r) return r.getIngredients();
        if (recipe instanceof StoveRecipe r) return r.getIngredients();
        return net.minecraft.core.NonNullList.create();
    }

    public static ItemStack resultOf(Recipe<?> recipe) {
        if (recipe instanceof SiloRecipe r) return r.getResultItem();
        if (recipe instanceof MincerRecipe r) return r.getResultItem();
        if (recipe instanceof CraftingBowlRecipe r) return r.getResultItem();
        if (recipe instanceof CookingPotRecipe r) return r.getResultItem();
        if (recipe instanceof RoasterRecipe r) return r.getResultItem();
        if (recipe instanceof StoveRecipe r) return r.getResultItem();
        return ItemStack.EMPTY;
    }
}
