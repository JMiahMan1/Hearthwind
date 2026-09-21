package net.satisfy.herbalbrews.core.compat.rei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.herbalbrews.core.compat.rei.category.TeaKettleCategory;
import net.satisfy.herbalbrews.core.recipe.TeaKettleRecipe;

import java.util.ArrayList;
import java.util.List;

public class TeaKettleDisplay extends BasicDisplay {
    private static final int REQUIRED_INPUT_SLOTS = 4;

    public TeaKettleDisplay(RecipeHolder<TeaKettleRecipe> recipe) {
        this(prepareInputs(recipe.value()), java.util.Collections.singletonList(EntryIngredients.of(recipe.value().getResultItem())), java.util.Optional.of(recipe.id().identifier()));
    }

    public TeaKettleDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        this(inputs, outputs, java.util.Optional.empty());
    }

    public TeaKettleDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, java.util.Optional<Identifier> location) {
        super(inputs, outputs, location);
    }

    private static List<EntryIngredient> prepareInputs(TeaKettleRecipe recipe) {
        List<EntryIngredient> inputs = new ArrayList<>(EntryIngredients.ofIngredients(new ArrayList<>(recipe.getIngredients())));

        if (inputs.size() > REQUIRED_INPUT_SLOTS) {
            inputs = inputs.subList(0, REQUIRED_INPUT_SLOTS);
        }

        while (inputs.size() < REQUIRED_INPUT_SLOTS) {
            inputs.add(EntryIngredient.empty());
        }

        return inputs;
    }

    @Override
    public me.shedaniel.rei.api.common.display.DisplaySerializer<? extends me.shedaniel.rei.api.common.display.Display> getSerializer() {
        return me.shedaniel.rei.api.common.display.DisplaySerializer.of(
                com.mojang.serialization.MapCodec.unit(() -> this),
                net.minecraft.network.codec.StreamCodec.unit(this));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return TeaKettleCategory.TEA_KETTLE_DISPLAY;
    }
}
