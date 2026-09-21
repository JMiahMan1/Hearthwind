package net.satisfy.herbalbrews.core.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.satisfy.herbalbrews.core.registry.RecipeTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class CauldronRecipe implements Recipe<RecipeInput> {

    private final NonNullList<net.minecraft.world.item.crafting.Ingredient> inputs;
    private final net.minecraft.world.item.ItemStackTemplate output;

    public CauldronRecipe(NonNullList<net.minecraft.world.item.crafting.Ingredient> inputs, net.minecraft.world.item.ItemStackTemplate output) {
        this.inputs = inputs;
        this.output = output;
    }

    public @NotNull NonNullList<net.minecraft.world.item.crafting.Ingredient> getIngredients() {
        return this.inputs;
    }


    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput) {
        return this.output.create();
    }

    public @NotNull ItemStack getResultItem() {
        return this.output.create();
    }

    public net.minecraft.world.item.ItemStackTemplate getOutput() {
        return this.output;
    }

    public @NotNull Identifier getId() {
        return RecipeTypeRegistry.CAULDRON_RECIPE_TYPE.getId();
    }

    @Override
    public @NotNull RecipeSerializer<CauldronRecipe> getSerializer() {
        return RecipeTypeRegistry.CAULDRON_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<CauldronRecipe> getType() {
        return RecipeTypeRegistry.CAULDRON_RECIPE_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull net.minecraft.world.item.crafting.PlacementInfo placementInfo() {
        return net.minecraft.world.item.crafting.PlacementInfo.create(getIngredients());
    }

    @Override
    public @NotNull java.util.List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
        java.util.List<net.minecraft.world.item.crafting.display.SlotDisplay> ings = new java.util.ArrayList<>();
        for (net.minecraft.world.item.crafting.Ingredient ing : getIngredients()) ings.add(ing.display());
        return java.util.List.of(new net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay(
                ings,
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(this.output),
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(
                        new net.minecraft.world.item.ItemStackTemplate(net.minecraft.world.item.Items.CRAFTING_TABLE))));
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

    public static class Serializer {

        public static final StreamCodec<RegistryFriendlyByteBuf, CauldronRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        public static final MapCodec<CauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap(list -> {
                    // NOTE: never call Ingredient.isEmpty() here - tag
                    // holders are unbound during datapack parsing and
                    // it throws. Empty checks belong in matches().
                    if (list.isEmpty()) {
                        return DataResult.error(() -> "No ingredients for Cauldron recipe");
                    } else if (list.size() > 3) {
                        return DataResult.error(() -> "Too many ingredients for Cauldron recipe");
                    } else {
                        NonNullList<Ingredient> merged = NonNullList.create();
                        merged.addAll(list);
                        return DataResult.success(merged);
                    }
                }, DataResult::success).forGetter(cauldronRecipe -> {
                    NonNullList<Ingredient> merged = NonNullList.create();
                    merged.addAll(cauldronRecipe.inputs);
                    return merged;
                }),
                net.minecraft.world.item.ItemStackTemplate.CODEC.fieldOf("result").forGetter(CauldronRecipe::getOutput)
                ).apply(instance, CauldronRecipe::new)
        );

        public static @NotNull CauldronRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            int i = registryFriendlyByteBuf.readVarInt();
            NonNullList<Ingredient> nonNullList = NonNullList.create();
            for (int j = 0; j < i; j++) {
                nonNullList.add(Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf));
            }
            net.minecraft.world.item.ItemStackTemplate output = net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.decode(registryFriendlyByteBuf);
            return new CauldronRecipe(nonNullList, output);
        }

        public static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, CauldronRecipe recipe) {
            registryFriendlyByteBuf.writeVarInt(recipe.getIngredients().size());

            for (Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, ingredient);
            }

            net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.encode(registryFriendlyByteBuf, recipe.output);
        }

        public static final RecipeSerializer<CauldronRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
