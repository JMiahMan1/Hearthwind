package net.satisfy.vinery.core.compat.rei.press;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.resources.Identifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.recipe.ApplePressFermentingRecipe;
import net.satisfy.vinery.core.recipe.ApplePressMashingRecipe;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("all")
public class ApplePressFermentingDisplay extends BasicDisplay {

    public static final CategoryIdentifier<ApplePressFermentingDisplay> APPLE_PRESS_DISPLAY = CategoryIdentifier.of(Vinery.MOD_ID, "apple_press_fermenting_display");

    public ApplePressFermentingDisplay(RecipeHolder<ApplePressFermentingRecipe> recipe) {
        this(Collections.singletonList(EntryIngredients.ofIngredient(recipe.value().input)), Collections.singletonList(EntryIngredients.of(recipe.value().getResultItem())), java.util.Optional.of(recipe.id().identifier()));
    }

    public ApplePressFermentingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        this(inputs, outputs, java.util.Optional.empty());
    }

    public ApplePressFermentingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, java.util.Optional<Identifier> location) {
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
