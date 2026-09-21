package net.satisfy.brewery.core.recipe;

import com.mojang.serialization.Codec;
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
import net.satisfy.brewery.core.block.property.BrewMaterial;
import net.satisfy.brewery.core.registry.RecipeTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class BrewingRecipe implements Recipe<RecipeInput> {
    private final NonNullList<Ingredient> ingredients;
    private final ItemStackTemplate output;
    private final BrewMaterial material;

    public BrewingRecipe(NonNullList<Ingredient> ingredients, ItemStackTemplate output, BrewMaterial material) {
        this.ingredients = ingredients;
        this.output = output;
        this.material = material;
    }

    public BrewMaterial getMaterial() {
        return material;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        java.util.List<ItemStack> provided = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ItemStack stack = recipeInput.getItem(i);
            if (!stack.isEmpty()) provided.add(stack);
        }
        if (provided.size() != this.ingredients.size()) return false;
        java.util.List<Ingredient> unmatched = new java.util.ArrayList<>(this.ingredients);
        for (ItemStack stack : provided) {
            boolean matched = false;
            java.util.Iterator<Ingredient> iter = unmatched.iterator();
            while (iter.hasNext()) {
                if (iter.next().test(stack)) {
                    iter.remove();
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }
        return unmatched.isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput) {
        return ItemStack.EMPTY;
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public @NotNull ItemStack getResultItem() {
        return this.output.create();
    }

    public Identifier getId() {
        return RecipeTypeRegistry.BREWING_RECIPE_TYPE.getId();
    }

    @Override
    public @NotNull RecipeSerializer<BrewingRecipe> getSerializer() {
        return RecipeTypeRegistry.BREWING_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<BrewingRecipe> getType() {
        return RecipeTypeRegistry.BREWING_RECIPE_TYPE.get();
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
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.CRAFTING_TABLE))));
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

    public static class Serializer {
        public static final StreamCodec<RegistryFriendlyByteBuf, BrewingRecipe> STREAM_CODEC = StreamCodec.of(BrewingRecipe.Serializer::toNetwork, BrewingRecipe.Serializer::fromNetwork);
        private static final MapCodec<BrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap((list) -> {
            Ingredient[] ingredients = list.stream().filter((ingredient) -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
            if (ingredients.length == 0) {
                return DataResult.error(() -> "No ingredients for Brewing recipe");
            } else if (ingredients.length > 3) {
                return DataResult.error(() -> "Too many ingredients for Brewing recipe");
            } else {
                NonNullList<Ingredient> merged = NonNullList.create();
                java.util.Collections.addAll(merged, ingredients);
                return DataResult.success(merged);
            }
        }, DataResult::success).forGetter(brewingRecipe -> brewingRecipe.ingredients),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(brewingRecipe -> brewingRecipe.output),
                Codec.STRING.fieldOf("material").forGetter(brewingRecipe -> brewingRecipe.material.name())).apply(instance, (ingredients1, stack, string) -> new BrewingRecipe(ingredients1, stack, BrewMaterial.valueOf(string))));
        public static final RecipeSerializer<BrewingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        public static @NotNull BrewingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            int i = buf.readVarInt();
            NonNullList<Ingredient> nonNullList = NonNullList.create();
            for (int k = 0; k < i; k++) nonNullList.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            ItemStackTemplate itemStack = ItemStackTemplate.STREAM_CODEC.decode(buf);
            BrewMaterial material = buf.readEnum(BrewMaterial.class);
            return new BrewingRecipe(nonNullList, itemStack, material);
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, BrewingRecipe recipe) {
            buf.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }

            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.output);
            buf.writeEnum(recipe.material);
        }
    }
}
