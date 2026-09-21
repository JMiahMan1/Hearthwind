package net.satisfy.vinery.core.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.recipe.input.*;
import net.satisfy.vinery.core.registry.RecipeTypesRegistry;
import org.jetbrains.annotations.NotNull;

public class ApplePressMashingRecipe implements Recipe<ApplePressMashingRecipeInput> {
    public final Ingredient input;
    private final net.minecraft.world.item.ItemStackTemplate output;
    public static RecipeType<ApplePressMashingRecipe> Type = RecipeTypesRegistry.APPLE_PRESS_MASHING_RECIPE_TYPE.get();

    public ApplePressMashingRecipe(Ingredient input, net.minecraft.world.item.ItemStackTemplate output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(ApplePressMashingRecipeInput inventory, Level world) {
        return input.test(inventory.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(ApplePressMashingRecipeInput container) {
        return this.output.create();
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(input);
        return list;
    }

    public @NotNull ItemStack getResultItem() {
        return this.output.create();
    }

    @Override
    public @NotNull RecipeSerializer<ApplePressMashingRecipe> getSerializer() {
        return RecipeTypesRegistry.APPLE_PRESS_MASHING_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<ApplePressMashingRecipe> getType() {
        return RecipeTypesRegistry.APPLE_PRESS_MASHING_RECIPE_TYPE.get();
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(getIngredients());
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

    @Override
    public boolean isSpecial() {
        return true;
    }

    public Ingredient getInput() {
        return input;
    }

    public net.minecraft.world.item.ItemStackTemplate getOutput() {
        return output;
    }

    public static class Serializer {
        public static final MapCodec<ApplePressMashingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("input").forGetter(ApplePressMashingRecipe::getInput),
                net.minecraft.world.item.ItemStackTemplate.CODEC.fieldOf("output").forGetter(ApplePressMashingRecipe::getOutput)
        ).apply(inst, ApplePressMashingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ApplePressMashingRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getInput());
                    net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getOutput());
                },
                buf -> new ApplePressMashingRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buf), net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.decode(buf)));
        public static final RecipeSerializer<ApplePressMashingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }


}
