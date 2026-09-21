package net.satisfy.bakery.core.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.satisfy.bakery.core.registry.RecipeTypeRegistry;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;

public class BakingStationRecipe implements Recipe<RecipeInput> {

    private final NonNullList<Ingredient> inputs;
    private final ItemStackTemplate output;

    public BakingStationRecipe(NonNullList<Ingredient> inputs, ItemStackTemplate output) {
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        return GeneralUtil.matchesRecipe(recipeInput, inputs, 1, 3);
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput) {
        return ItemStack.EMPTY;
    }

    public Identifier getId() {
        return RecipeTypeRegistry.BAKING_STATION_RECIPE_TYPE.getId();
    }

    public @NotNull ItemStack getResultItem() {
        return this.output.create();
    }

    @Override
    public @NotNull RecipeSerializer<BakingStationRecipe> getSerializer() {
        return RecipeTypeRegistry.BAKING_STATION_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<BakingStationRecipe> getType() {
        return RecipeTypeRegistry.BAKING_STATION_RECIPE_TYPE.get();
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(getIngredients());
    }

    @Override
    public @NotNull java.util.List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
        java.util.List<net.minecraft.world.item.crafting.display.SlotDisplay> ings = new java.util.ArrayList<>();
        for (Ingredient ing : getIngredients()) ings.add(ing.display());
        return java.util.List.of(new net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay(
                ings,
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(this.output),
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(new net.minecraft.world.item.ItemStackTemplate(Items.CRAFTING_TABLE))));
    }

    @Override
    public @NotNull net.minecraft.world.item.crafting.RecipeBookCategory recipeBookCategory() {
        return net.minecraft.world.item.crafting.RecipeBookCategories.CRAFTING_MISC;
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

    public static class Serializer {

        public static final StreamCodec<RegistryFriendlyByteBuf, BakingStationRecipe> STREAM_CODEC = StreamCodec.of(BakingStationRecipe.Serializer::toNetwork, BakingStationRecipe.Serializer::fromNetwork);
        private static final MapCodec<BakingStationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return instance.group(Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap((list) -> {
                Ingredient[] ingredients = list.stream().filter((ingredient) -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
                if (ingredients.length == 0) {
                    return DataResult.error(() -> {
                        return "No ingredients for Baking Station recipe";
                    });
                } else if (ingredients.length > 3) {
                    return DataResult.error(() -> {
                        return "Too many ingredients for Baking Station recipe";
                    });
                } else {
                    NonNullList<Ingredient> merged = NonNullList.create();
                    java.util.Collections.addAll(merged, ingredients);
                    return DataResult.success(merged);
                }
            }, DataResult::success).forGetter(bakingStationRecipe -> bakingStationRecipe.inputs), ItemStackTemplate.CODEC.fieldOf("result").forGetter(bakingStationRecipe -> bakingStationRecipe.output)).apply(instance, BakingStationRecipe::new);
        });
        public static final RecipeSerializer<BakingStationRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        public static @NotNull BakingStationRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            int i = buf.readVarInt();
            NonNullList<Ingredient> nonNullList = NonNullList.create();
            for (int k = 0; k < i; k++) nonNullList.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            ItemStackTemplate itemStack = ItemStackTemplate.STREAM_CODEC.decode(buf);
            return new BakingStationRecipe(nonNullList, itemStack);
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, BakingStationRecipe recipe) {
            buf.writeVarInt(recipe.inputs.size());
            for (Ingredient ingredient : recipe.inputs) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }

            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.output);
        }
    }

}
