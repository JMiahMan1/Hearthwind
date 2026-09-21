package net.satisfy.brewery.core.compat.rei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;
import net.satisfy.brewery.core.compat.rei.category.BrewingStationCategory;
import net.satisfy.brewery.core.recipe.BrewingRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public class BrewingStationDisplay extends BasicDisplay {

    public BrewingStationDisplay(BrewingRecipe recipe) {
        this(EntryIngredients.ofIngredients(new ArrayList<>(recipe.getIngredients())), Collections.singletonList(EntryIngredients.of(recipe.getResultItem())), Optional.ofNullable(recipe.getId()));
    }

    public BrewingStationDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
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
        return BrewingStationCategory.BREWING_STATION_DISPLAY;
    }
}