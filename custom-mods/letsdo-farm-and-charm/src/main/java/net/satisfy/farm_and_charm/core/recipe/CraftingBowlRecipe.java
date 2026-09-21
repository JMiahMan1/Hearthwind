package net.satisfy.farm_and_charm.core.recipe;

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
import net.satisfy.farm_and_charm.core.util.StreamCodecUtil;
import org.jetbrains.annotations.NotNull;

public class CraftingBowlRecipe implements Recipe<RecipeInput> {
    private final NonNullList<Ingredient> inputs;
    private final ItemStackTemplate output;
    private final int outputCount;

    public CraftingBowlRecipe(NonNullList<Ingredient> inputs, ItemStackTemplate output) {
        this.inputs = inputs;
        this.output = output;
        this.outputCount = output.count();
    }

    public int getOutputCount() {
        return outputCount;
    }


    @Override
    public boolean matches(RecipeInput inventory, Level world) {
        int nonEmptySlots = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                nonEmptySlots++;
            }
        }
        return nonEmptySlots >= 1 && nonEmptySlots <= inputs.size() && GeneralUtil.matchesRecipe(inventory, inputs, 0, 3);
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput) {
        return ItemStack.EMPTY;
    }


    public @NotNull ItemStack getResultItem() {
        return this.output.withCount(this.outputCount).create();
    }

    public @NotNull Identifier getId() {
        return RecipeTypeRegistry.CRAFTING_BOWL_RECIPE_TYPE.getId();
    }

    @Override
    public @NotNull RecipeSerializer<CraftingBowlRecipe> getSerializer() {
        return RecipeTypeRegistry.CRAFTING_BOWL_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<CraftingBowlRecipe> getType() {
        return RecipeTypeRegistry.CRAFTING_BOWL_RECIPE_TYPE.get();
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
        java.util.List<net.minecraft.world.item.crafting.display.SlotDisplay> _ings = new java.util.ArrayList<>();
        for (Ingredient _ing : getIngredients()) _ings.add(_ing.display());
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

    private static final MapCodec<CraftingBowlRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap(list -> {
                        Ingredient[] ingredients = list.toArray(Ingredient[]::new);
                        if (ingredients.length == 0) {
                            return DataResult.error(() -> "No ingredients for shapeless recipe");
                        }
                        // 26.2: must not touch ingredient contents here (tags bind
                        // later); keep the decoded list verbatim like vanilla.
                        NonNullList<Ingredient> merged = NonNullList.create();
                        java.util.Collections.addAll(merged, ingredients);
                        return DataResult.success(merged);
                    }, DataResult::success).forGetter(CraftingBowlRecipe::getIngredients),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.output)
            ).apply(instance, CraftingBowlRecipe::new)
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, CraftingBowlRecipe> STREAM_CODEC = StreamCodec.composite(
            StreamCodecUtil.nonNullList(Ingredient.CONTENTS_STREAM_CODEC), CraftingBowlRecipe::getIngredients,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
            CraftingBowlRecipe::new
    );
    public static final RecipeSerializer<CraftingBowlRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
