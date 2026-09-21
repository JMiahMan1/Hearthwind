package net.satisfy.meadow.core.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.satisfy.meadow.core.registry.RecipeRegistry;
import org.jetbrains.annotations.NotNull;

public class FeltingRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient ingredient;
    private final ItemStackTemplate result;

    public FeltingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }

    public @NotNull ItemStack getResultItem() {
        return result.create();
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(getIngredients());
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull String group() {
        return "";
    }

    @Override
    public @NotNull RecipeSerializer<FeltingRecipe> getSerializer() {
        return RecipeRegistry.FELTING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<FeltingRecipe> getType() {
        return RecipeRegistry.FELTING.get();
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStackTemplate getResult() {
        return result;
    }

    public static class Serializer {
        public static final MapCodec<FeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(FeltingRecipe::getIngredient),
                ItemStackTemplate.MAP_CODEC.fieldOf("result").forGetter(FeltingRecipe::getResult)
        ).apply(i, FeltingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FeltingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, FeltingRecipe::getIngredient,
                ItemStackTemplate.STREAM_CODEC, FeltingRecipe::getResult,
                FeltingRecipe::new
        );

        public static final RecipeSerializer<FeltingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
