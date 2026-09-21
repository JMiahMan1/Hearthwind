package net.satisfy.bakery.core.compat.rei.caking;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.satisfy.bakery.core.recipe.BakingStationRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public class BakerStationDisplay extends BasicDisplay {

    public BakerStationDisplay(BakingStationRecipe recipe) {
        this(createInputs(recipe), createOutputs(recipe), Optional.of(recipe.getId()));
    }

    public BakerStationDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    private static List<EntryIngredient> createInputs(BakingStationRecipe recipe) {
        List<EntryIngredient> inputs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (i < recipe.getIngredients().size()) {
                inputs.add(EntryIngredients.ofIngredient(recipe.getIngredients().get(i)));
            } else {
                inputs.add(EntryIngredients.of(ItemStack.EMPTY));
            }
        }
        return inputs;
    }

    private static List<EntryIngredient> createOutputs(BakingStationRecipe recipe) {
        return List.of(EntryIngredients.of(recipe.getResultItem()));
    }

    @Override
    public me.shedaniel.rei.api.common.display.DisplaySerializer<? extends me.shedaniel.rei.api.common.display.Display> getSerializer() {
        return me.shedaniel.rei.api.common.display.DisplaySerializer.of(
                com.mojang.serialization.MapCodec.unit(() -> this),
                net.minecraft.network.codec.StreamCodec.unit(this));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BakerStationCategory.BAKER_STATION_DISPLAY;
    }
}
