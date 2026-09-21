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

public class WoodcuttingRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStackTemplate outputStack;

    public WoodcuttingRecipe(ItemStackTemplate itemStack, Ingredient ingredient) {
        this.outputStack = itemStack;
        this.input = ingredient;
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getOutputStack() {
        return this.outputStack.create();
    }

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        return this.input.test(recipeInput.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(SingleRecipeInput recipeInput) {
        return this.outputStack.create();
    }

    public @NotNull ItemStack getResultItem() {
        return this.outputStack.create();
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.input);
        return list;
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
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<WoodcuttingRecipe> getSerializer() {
        return RecipeRegistry.WOODCUTTING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<WoodcuttingRecipe> getType() {
        return RecipeRegistry.WOODCUTTING.get();
    }

    public static class Serializer {
        public static final MapCodec<WoodcuttingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        ItemStackTemplate.MAP_CODEC.fieldOf("outputItem").forGetter(recipe -> recipe.outputStack),
                        Ingredient.CODEC.fieldOf("inputItem").forGetter(WoodcuttingRecipe::getInput)
                ).apply(instance, WoodcuttingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, WoodcuttingRecipe> STREAM_CODEC = StreamCodec.composite(
                ItemStackTemplate.STREAM_CODEC, recipe -> recipe.outputStack,
                Ingredient.CONTENTS_STREAM_CODEC, WoodcuttingRecipe::getInput,
                WoodcuttingRecipe::new
        );

        public static final RecipeSerializer<WoodcuttingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
