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

public class CookingPotRecipe implements Recipe<RecipeInput> {
    private final NonNullList<Ingredient> inputs;
    private final boolean containerRequired;
    private final ItemStackTemplate containerItem;
    private final ItemStackTemplate output;
    private final boolean requiresLearning;

    public CookingPotRecipe(NonNullList<Ingredient> inputs, boolean containerRequired, ItemStackTemplate containerItem, ItemStackTemplate output, boolean requiresLearning) {
        this.inputs = inputs;
        this.containerRequired = containerRequired;
        this.containerItem = containerItem;
        this.output = output;
        this.requiresLearning = requiresLearning;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        return GeneralUtil.matchesRecipe(recipeInput, inputs, 0, 5);
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput) {
        return ItemStack.EMPTY;
    }

    public Identifier getId() {
        return RecipeTypeRegistry.COOKING_POT_RECIPE_TYPE.getId();
    }


    public @NotNull ItemStack getResultItem() {
        return this.output.create();
    }

    @Override
    public @NotNull RecipeSerializer<CookingPotRecipe> getSerializer() {
        return RecipeTypeRegistry.COOKING_POT_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<CookingPotRecipe> getType() {
        return RecipeTypeRegistry.COOKING_POT_RECIPE_TYPE.get();
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    public boolean isContainerRequired() {
        return containerRequired;
    }

    public ItemStack getContainerItem() {
        return containerItem.create();
    }

    public static Identifier getIdStatic() {
        return RecipeTypeRegistry.COOKING_POT_RECIPE_TYPE.getId();
    }

    public boolean requiresLearning() {
        return requiresLearning;
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

    private static final MapCodec<CookingPotRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
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
                    }, DataResult::success).forGetter(CookingPotRecipe::getIngredients),
                    Codec.BOOL.fieldOf("requireContainer").forGetter(CookingPotRecipe::isContainerRequired),
                    ItemStackTemplate.CODEC.fieldOf("container").forGetter(recipe -> recipe.containerItem),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
                    Codec.BOOL.fieldOf("requiresLearning").forGetter(CookingPotRecipe::requiresLearning)
            ).apply(instance, CookingPotRecipe::new)
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, CookingPotRecipe> STREAM_CODEC =
            StreamCodec.of(CookingPotRecipe::toNetwork, CookingPotRecipe::fromNetwork);
    public static final RecipeSerializer<CookingPotRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public static @NotNull CookingPotRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        int i = registryFriendlyByteBuf.readVarInt();
        NonNullList<Ingredient> nonNullList = NonNullList.create();
        for (int _k = 0; _k < i; _k++) nonNullList.add(Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf));
        boolean containerRequired = registryFriendlyByteBuf.readBoolean();
        ItemStackTemplate containerItem = ItemStackTemplate.STREAM_CODEC.decode(registryFriendlyByteBuf);
        ItemStackTemplate itemStack = ItemStackTemplate.STREAM_CODEC.decode(registryFriendlyByteBuf);
        boolean requiresLearning = registryFriendlyByteBuf.readBoolean();
        return new CookingPotRecipe(nonNullList, containerRequired, containerItem, itemStack, requiresLearning);
    }

    public static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, CookingPotRecipe recipe) {
        registryFriendlyByteBuf.writeVarInt(recipe.getIngredients().size());

        for (Ingredient ingredient : recipe.getIngredients()) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, ingredient);
        }
        registryFriendlyByteBuf.writeBoolean(recipe.containerRequired);
        ItemStackTemplate.STREAM_CODEC.encode(registryFriendlyByteBuf, recipe.containerItem);
        ItemStackTemplate.STREAM_CODEC.encode(registryFriendlyByteBuf, recipe.output);
        registryFriendlyByteBuf.writeBoolean(recipe.requiresLearning);
    }

}