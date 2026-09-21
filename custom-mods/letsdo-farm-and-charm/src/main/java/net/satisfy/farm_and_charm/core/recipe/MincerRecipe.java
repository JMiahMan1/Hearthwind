package net.satisfy.farm_and_charm.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.core.registry.RecipeTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class MincerRecipe implements Recipe<RecipeInput> {

    private final String recipe_type;
    private final Ingredient input;
    private final ItemStackTemplate output;

    public MincerRecipe(String type, Ingredient input, ItemStackTemplate output) {
        this.recipe_type = type;
        this.input = input;
        this.output = output;
    }

    public Ingredient getInput() {
        return this.input;
    }

    public ItemStack getOutput() {
        return this.output.create();
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> nonNullList = NonNullList.create();
        nonNullList.add(this.input);
        return nonNullList;
    }

    public String getRecipeType() {
        return this.recipe_type;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        return input.test(recipeInput.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput) {
        return output.create();
    }


    public @NotNull ItemStack getResultItem() {
        return output.create();
    }

    public @NotNull Identifier getId() {
        return RecipeTypeRegistry.MINCER_RECIPE_TYPE.getId();
    }

    @Override
    public @NotNull RecipeSerializer<MincerRecipe> getSerializer() {
        return RecipeTypeRegistry.MINCER_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<MincerRecipe> getType() {
        return RecipeTypeRegistry.MINCER_RECIPE_TYPE.get();
    }


    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(this.input);
    }

    @Override
    public @NotNull java.util.List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
        return java.util.List.of(new net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay(
                this.input.display(), net.minecraft.world.item.crafting.display.SlotDisplay.Empty.INSTANCE,
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(this.output),
                net.minecraft.world.item.crafting.display.SlotDisplay.Empty.INSTANCE, 0, 0.0F));
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

    private static final MapCodec<MincerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("recipe_type").forGetter(MincerRecipe::getRecipeType),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MincerRecipe::getInput),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.output)
            ).apply(instance, MincerRecipe::new)
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, MincerRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Codec.STRING), MincerRecipe::getRecipeType,
            Ingredient.CONTENTS_STREAM_CODEC, MincerRecipe::getInput,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
            MincerRecipe::new
    );
    public static final RecipeSerializer<MincerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
