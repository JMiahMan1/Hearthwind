package net.satisfy.herbalbrews.core.compat.rei;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.satisfy.herbalbrews.core.compat.rei.category.CauldronCategory;
import net.satisfy.herbalbrews.core.compat.rei.category.TeaKettleCategory;
import net.satisfy.herbalbrews.core.compat.rei.display.CauldronDisplay;
import net.satisfy.herbalbrews.core.compat.rei.display.TeaKettleDisplay;
import net.satisfy.herbalbrews.core.recipe.CauldronRecipe;
import net.satisfy.herbalbrews.core.recipe.TeaKettleRecipe;
import net.satisfy.herbalbrews.core.registry.ObjectRegistry;
import net.satisfy.herbalbrews.core.registry.RecipeTypeRegistry;

import java.util.ArrayList;
import java.util.List;

public class HerbalBrewsReiClientPlugin {

    public static void registerCategories(CategoryRegistry registry) {
        registry.add(new TeaKettleCategory());
        registry.add(new CauldronCategory());

        registry.addWorkstations(TeaKettleCategory.TEA_KETTLE_DISPLAY, EntryStacks.of(ObjectRegistry.TEA_KETTLE.get()));
        registry.addWorkstations(TeaKettleCategory.TEA_KETTLE_DISPLAY, EntryStacks.of(ObjectRegistry.COPPER_TEA_KETTLE.get()));
        registry.addWorkstations(CauldronDisplay.CAULDRON_DISPLAY, EntryStacks.of(ObjectRegistry.CAULDRON.get()));

    }

    public static void registerDisplays(DisplayRegistry registry) {
        registry.beginFiller(net.minecraft.world.item.crafting.RecipeHolder.class)
                .filter(holder -> holder.value() instanceof TeaKettleRecipe)
                .fill(holder -> new TeaKettleDisplay((RecipeHolder<TeaKettleRecipe>) holder));
        registry.beginFiller(net.minecraft.world.item.crafting.RecipeHolder.class)
                .filter(holder -> holder.value() instanceof CauldronRecipe)
                .fill(holder -> new CauldronDisplay((RecipeHolder<CauldronRecipe>) holder));
    }

    public static List<Ingredient> ingredients(Recipe<RecipeInput> recipe, ItemStack stack){
        List<Ingredient> l = new ArrayList<>(recipe instanceof TeaKettleRecipe r ? r.getIngredients() : recipe instanceof CauldronRecipe c ? c.getIngredients() : List.<Ingredient>of());
        l.add(0, Ingredient.of(stack.getItem()));
        return l;
    }
}
