package net.satisfy.farm_and_charm.core.compat.rei.stove;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.satisfy.farm_and_charm.FarmAndCharm;
import net.satisfy.farm_and_charm.core.recipe.StoveRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StoveDisplay extends BasicDisplay implements SimpleGridMenuDisplay {
    public static final CategoryIdentifier<StoveDisplay> STOVE_DISPLAY = CategoryIdentifier.of(FarmAndCharm.MOD_ID, "stove_display");
    private final float xp;

    public StoveDisplay(StoveRecipe recipe) {
        this(EntryIngredients.ofIngredients(recipe.getIngredients()), Collections.singletonList(EntryIngredients.of(net.satisfy.farm_and_charm.core.recipe.RecipeDisplays26.resultOf(recipe))), recipe.getExperience());
    }

    public StoveDisplay(List<EntryIngredient> input, List<EntryIngredient> output, net.minecraft.nbt.CompoundTag tag) {
        this(input, output, tag.getFloatOr("experience", 0.0F));
    }

    public StoveDisplay(List<EntryIngredient> input, List<EntryIngredient> output, float xp) {
        super(input, output, Optional.empty());
        this.xp = xp;
    }


    public float getXp() {
        return xp;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public me.shedaniel.rei.api.common.display.DisplaySerializer<? extends me.shedaniel.rei.api.common.display.Display> getSerializer() {
        return me.shedaniel.rei.api.common.display.DisplaySerializer.of(
                com.mojang.serialization.MapCodec.unit(() -> this),
                net.minecraft.network.codec.StreamCodec.unit(this));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return STOVE_DISPLAY;
    }
}
