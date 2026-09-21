package net.satisfy.vinery.core.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.recipe.input.ApplePressFermentingRecipeInput;
import net.satisfy.vinery.core.registry.RecipeTypesRegistry;
import org.jetbrains.annotations.NotNull;

public class ApplePressFermentingRecipe implements Recipe<ApplePressFermentingRecipeInput> {
    public final Ingredient input;
    private final net.minecraft.world.item.ItemStackTemplate output;
    private final boolean requiresBottle;
    public static RecipeType<ApplePressFermentingRecipe> Type = RecipeTypesRegistry.APPLE_PRESS_FERMENTING_RECIPE_TYPE.get();

    public ApplePressFermentingRecipe(Ingredient input, net.minecraft.world.item.ItemStackTemplate output, boolean requiresBottle) {
        this.input = input;
        this.output = output;
        this.requiresBottle = requiresBottle;
    }

    public boolean requiresBottle() {
        return requiresBottle;
    }

    @Override
    public boolean matches(ApplePressFermentingRecipeInput inventory, Level world) {
        return input.test(inventory.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(ApplePressFermentingRecipeInput container) {
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

    public Ingredient getInput() {
        return input;
    }

    public net.minecraft.world.item.ItemStackTemplate getOutput() {
        return output;
    }

    public boolean isRequiresBottle() {
        return requiresBottle;
    }

    @Override
    public @NotNull RecipeSerializer<ApplePressFermentingRecipe> getSerializer() {
        return RecipeTypesRegistry.APPLE_PRESS_FERMENTING_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<ApplePressFermentingRecipe> getType() {
        return RecipeTypesRegistry.APPLE_PRESS_FERMENTING_RECIPE_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
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

    public static class Serializer {

        private static final MapCodec<Boolean> WINE_BOTTLE_CODEC_INNER = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        Codec.BOOL.fieldOf("required").forGetter(b -> b)
                ).apply(inst, b -> b)
        );
        public static final MapCodec<ApplePressFermentingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("input").forGetter(ApplePressFermentingRecipe::getInput),
                net.minecraft.world.item.ItemStackTemplate.CODEC.fieldOf("output").forGetter(ApplePressFermentingRecipe::getOutput),
                WINE_BOTTLE_CODEC_INNER.fieldOf("wine_bottle").forGetter(ApplePressFermentingRecipe::isRequiresBottle)
        ).apply(inst, ApplePressFermentingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ApplePressFermentingRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getInput());
                    net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getOutput());
                    buf.writeBoolean(recipe.isRequiresBottle());
                },
                buf -> new ApplePressFermentingRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buf), net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.decode(buf), buf.readBoolean()));
        public static final RecipeSerializer<ApplePressFermentingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
