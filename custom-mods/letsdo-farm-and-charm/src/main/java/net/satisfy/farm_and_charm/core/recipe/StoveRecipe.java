package net.satisfy.farm_and_charm.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.core.registry.RecipeTypeRegistry;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;


public class StoveRecipe implements Recipe<RecipeInput> {
    protected final NonNullList<Ingredient> inputs;
    protected final ItemStackTemplate output;
    protected final float experience;
    private final boolean requiresLearning;

    public StoveRecipe(NonNullList<Ingredient> inputs, ItemStackTemplate output, float experience, boolean requiresLearning) {
        this.inputs = inputs;
        this.output = output;
        this.experience = experience;
        this.requiresLearning = requiresLearning;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        return GeneralUtil.matchesRecipe(recipeInput, inputs, 1, 3);
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput) {
        return ItemStack.EMPTY;
    }


    public @NotNull ItemStack getResultItem() {
        return this.output.create();
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    public @NotNull Identifier getId() {
        return RecipeTypeRegistry.STOVE_RECIPE_TYPE.getId();
    }

    public float getExperience() {
        return experience;
    }

    @Override
    public @NotNull RecipeSerializer<StoveRecipe> getSerializer() {
        return RecipeTypeRegistry.STOVE_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<StoveRecipe> getType() {
        return RecipeTypeRegistry.STOVE_RECIPE_TYPE.get();
    }


    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(this.inputs);
    }

    @Override
    public @NotNull java.util.List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
        java.util.List<net.minecraft.world.item.crafting.display.SlotDisplay> _ings = new java.util.ArrayList<>();
        for (Ingredient _ing : this.inputs) _ings.add(_ing.display());
        return java.util.List.of(new net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay(
                _ings,
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(this.output),
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(new net.minecraft.world.item.ItemStackTemplate(net.minecraft.world.item.Items.CRAFTING_TABLE))));
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

    public boolean requiresLearning() {
        return requiresLearning;
    }

    private static final MapCodec<StoveRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap(list -> {
                Ingredient[] ingredients = list.toArray(Ingredient[]::new);
                if (ingredients.length == 0) {
                    return DataResult.error(() -> "No ingredients for shapeless recipe");
                }
                NonNullList<Ingredient> merged = NonNullList.create();
                        java.util.Collections.addAll(merged, ingredients);
                        return DataResult.success(merged);
            }, DataResult::success).forGetter(StoveRecipe::getIngredients),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
            Codec.FLOAT.fieldOf("experience").forGetter(StoveRecipe::getExperience),
            Codec.BOOL.fieldOf("requiresLearning").forGetter(StoveRecipe::requiresLearning)
            ).apply(instance, StoveRecipe::new)
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, StoveRecipe> STREAM_CODEC =
            StreamCodec.of(StoveRecipe::toNetwork, StoveRecipe::fromNetwork);
    public static final RecipeSerializer<StoveRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public static @NotNull StoveRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        int i = registryFriendlyByteBuf.readVarInt();
        NonNullList<Ingredient> nonNullList = NonNullList.create();
        for (int _k = 0; _k < i; _k++) nonNullList.add(Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf));
        ItemStackTemplate itemStack = ItemStackTemplate.STREAM_CODEC.decode(registryFriendlyByteBuf);
        float experience = registryFriendlyByteBuf.readFloat();
        boolean requiresLearning = registryFriendlyByteBuf.readBoolean();
        return new StoveRecipe(nonNullList, itemStack, experience, requiresLearning);
    }

    public static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, StoveRecipe recipe) {
        registryFriendlyByteBuf.writeVarInt(recipe.getIngredients().size());

        for (Ingredient ingredient : recipe.getIngredients()) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, ingredient);
        }
        ItemStackTemplate.STREAM_CODEC.encode(registryFriendlyByteBuf, recipe.output);
        registryFriendlyByteBuf.writeFloat(recipe.experience);
        registryFriendlyByteBuf.writeBoolean(recipe.requiresLearning);
    }
}