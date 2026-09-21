package net.satisfy.vinery.core.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.satisfy.vinery.core.compat.rei.press.ApplePressCategory;
import net.satisfy.vinery.core.compat.rei.press.ApplePressDisplay;
import net.satisfy.vinery.core.compat.rei.press.ApplePressFermentingCategory;
import net.satisfy.vinery.core.compat.rei.press.ApplePressFermentingDisplay;
import net.satisfy.vinery.core.compat.rei.wine.FermentationBarrelCategory;
import net.satisfy.vinery.core.compat.rei.wine.FermentationBarrelDisplay;
import net.satisfy.vinery.core.recipe.ApplePressFermentingRecipe;
import net.satisfy.vinery.core.recipe.ApplePressMashingRecipe;
import net.satisfy.vinery.core.recipe.FermentationBarrelRecipe;
import net.satisfy.vinery.core.registry.ObjectRegistry;

import java.util.ArrayList;
import java.util.List;


public class VineryReiClientPlugin {
    public static void registerCategories(CategoryRegistry registry) {
        registry.add(new FermentationBarrelCategory());
        registry.add(new ApplePressCategory());
        registry.add(new ApplePressFermentingCategory());
        registry.addWorkstations(FermentationBarrelDisplay.FERMENTATION_BARREL_DISPLAY, EntryStacks.of(ObjectRegistry.FERMENTATION_BARREL.get()));
        registry.addWorkstations(ApplePressDisplay.APPLE_PRESS_DISPLAY, EntryStacks.of(ObjectRegistry.APPLE_PRESS.get()));
        registry.addWorkstations(ApplePressFermentingDisplay.APPLE_PRESS_DISPLAY, EntryStacks.of(ObjectRegistry.APPLE_PRESS.get()));
    }

    public static void registerDisplays(DisplayRegistry registry) {
        registry.beginFiller(net.minecraft.world.item.crafting.RecipeHolder.class)
                .filter(holder -> holder.value() instanceof FermentationBarrelRecipe)
                .fill(holder -> new FermentationBarrelDisplay((RecipeHolder<FermentationBarrelRecipe>) holder));
        registry.beginFiller(net.minecraft.world.item.crafting.RecipeHolder.class)
                .filter(holder -> holder.value() instanceof ApplePressMashingRecipe)
                .fill(holder -> new ApplePressDisplay((RecipeHolder<ApplePressMashingRecipe>) holder));
        registry.beginFiller(net.minecraft.world.item.crafting.RecipeHolder.class)
                .filter(holder -> holder.value() instanceof ApplePressFermentingRecipe)
                .fill(holder -> new ApplePressFermentingDisplay((RecipeHolder<ApplePressFermentingRecipe>) holder));
    }

    public static List<Ingredient> ingredients(Recipe<?> recipe, ItemStack stack){
        List<Ingredient> l = new ArrayList<>(recipe instanceof FermentationBarrelRecipe r ? r.getIngredients() : List.<Ingredient>of());
        l.add(0, Ingredient.of(stack.getItem()));
        return l;
    }

}
