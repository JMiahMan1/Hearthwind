package net.satisfy.meadow.core.compat.rei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.meadow.core.compat.rei.category.WoodCutterCategory;
import net.satisfy.meadow.core.recipes.WoodcuttingRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WoodCutterDisplay extends BasicDisplay {


    public WoodCutterDisplay(RecipeHolder<WoodcuttingRecipe> recipe) {
        this(EntryIngredients.ofIngredients(new ArrayList<>(recipe.value().getIngredients())), Collections.singletonList(EntryIngredients.of(recipe.value().getResultItem())), Optional.of(recipe.id().identifier()));
    }

    public WoodCutterDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
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
        return WoodCutterCategory.WOOD_CUTTER_DISPLAY;
    }
}
