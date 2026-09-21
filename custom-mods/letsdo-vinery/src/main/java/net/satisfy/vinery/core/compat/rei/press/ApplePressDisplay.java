package net.satisfy.vinery.core.compat.rei.press;


import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.recipe.ApplePressFermentingRecipe;
import net.satisfy.vinery.core.recipe.ApplePressMashingRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public class ApplePressDisplay extends BasicDisplay {

    public static final CategoryIdentifier<ApplePressDisplay> APPLE_PRESS_DISPLAY = CategoryIdentifier.of(Vinery.MOD_ID, "apple_press_display");


    public ApplePressDisplay(RecipeHolder<ApplePressMashingRecipe> recipe) {
        this(Collections.singletonList(EntryIngredients.ofIngredient(recipe.value().input)), Collections.singletonList(EntryIngredients.of(recipe.value().getResultItem())), java.util.Optional.of(recipe.id().identifier()));
    }

    public ApplePressDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        this(inputs, outputs, java.util.Optional.empty());
    }

    public ApplePressDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, java.util.Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    @Override
    public me.shedaniel.rei.api.common.display.DisplaySerializer<? extends me.shedaniel.rei.api.common.display.Display> getSerializer() {
        return me.shedaniel.rei.api.common.display.DisplaySerializer.of(
                com.mojang.serialization.MapCodec.unit(() -> this),
                net.minecraft.network.codec.StreamCodec.unit(this));
    }


    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return APPLE_PRESS_DISPLAY;
    }


}
