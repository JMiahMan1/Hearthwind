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

public class CheeseFormRecipe implements Recipe<CraftingInput> {
    private final Ingredient bucket;
    private final Ingredient ingredient;
    private final ItemStackTemplate result;

    public CheeseFormRecipe(Ingredient bucket, Ingredient ingredient, ItemStackTemplate result) {
        this.bucket = bucket;
        this.ingredient = ingredient;
        this.result = result;
    }

    public ItemStack assemble() {
        return this.result.create();
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> defaultedList = NonNullList.create();
        defaultedList.add(this.bucket);
        defaultedList.add(this.ingredient);
        return defaultedList;
    }

    public Ingredient bucket() {
        return this.bucket;
    }

    public Ingredient ingredient() {
        return this.ingredient;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level level) {
        return bucket.test(inventory.getItem(0)) && ingredient.test(inventory.getItem(1))
                || bucket.test(inventory.getItem(1)) && ingredient.test(inventory.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput recipeInput) {
        return this.result.create();
    }

    public @NotNull ItemStack getResultItem() {
        return this.result.create();
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
    public @NotNull RecipeSerializer<CheeseFormRecipe> getSerializer() {
        return RecipeRegistry.CHEESE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<CheeseFormRecipe> getType() {
        return RecipeRegistry.CHEESE.get();
    }

    public static class Serializer {
        public static final MapCodec<CheeseFormRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Ingredient.CODEC.fieldOf("bucket").forGetter(CheeseFormRecipe::bucket),
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(CheeseFormRecipe::ingredient),
                        ItemStackTemplate.MAP_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                ).apply(instance, CheeseFormRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, CheeseFormRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, CheeseFormRecipe::bucket,
                Ingredient.CONTENTS_STREAM_CODEC, CheeseFormRecipe::ingredient,
                ItemStackTemplate.STREAM_CODEC, r -> r.result,
                CheeseFormRecipe::new
        );

        public static final RecipeSerializer<CheeseFormRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

}
