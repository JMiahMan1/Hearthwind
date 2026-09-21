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
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.block.entity.FermentationBarrelBlockEntity;
import net.satisfy.vinery.core.recipe.input.FermentationBarrelRecipeInput;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.registry.RecipeTypesRegistry;
import org.jetbrains.annotations.NotNull;

public class FermentationBarrelRecipe implements Recipe<FermentationBarrelRecipeInput> {
    private final NonNullList<Ingredient> inputs;
    private final net.minecraft.world.item.ItemStackTemplate output;
    private final FermentationBarrelRecipeInput.JuiceData juiceData;
    private final boolean wineBottleRequired;
    public static RecipeType<FermentationBarrelRecipe> Type = RecipeTypesRegistry.FERMENTATION_BARREL_RECIPE_TYPE.get();

    public FermentationBarrelRecipe(NonNullList<Ingredient> inputs, FermentationBarrelRecipeInput.JuiceData data, net.minecraft.world.item.ItemStackTemplate output, boolean wineBottleRequired) {
        this.inputs = inputs;
        this.juiceData = data;
        this.output = output;

        this.wineBottleRequired = wineBottleRequired;
    }

    public FermentationBarrelRecipeInput.JuiceData getJuiceData()
    {
        return this.juiceData;
    }

    public boolean isWineBottleRequired() {
        return wineBottleRequired;
    }

    @Override
    public boolean matches(FermentationBarrelRecipeInput input, Level world) {
        if (this.juiceData.amount() > 0) {
            if (input.data().amount() < this.juiceData.amount()) return false;
            if (!this.juiceData.type().equals(input.data().type())) return false;
        }

        if (this.wineBottleRequired) {
            ItemStack wineBottle = input.getItem(FermentationBarrelRecipeInput.WINE_BOTTLE_SLOT);
            if (wineBottle.isEmpty() || !wineBottle.is(ObjectRegistry.WINE_BOTTLE.get())) return false;
        }

        java.util.List<ItemStack> provided = new java.util.ArrayList<>();
        for (ItemStack itemStack : input.getIngredientSlots()) {
            if (!itemStack.isEmpty()) provided.add(itemStack);
        }
        if (provided.size() != this.inputs.size()) return false;
        java.util.List<Ingredient> unmatched = new java.util.ArrayList<>(this.inputs);
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
    public @NotNull ItemStack assemble(FermentationBarrelRecipeInput input) {
        return this.output.create();
    }

    public @NotNull ItemStack getResultItem() {
        return this.output.create();
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    public NonNullList<Ingredient> getInputs() {
        return inputs;
    }

    @Override
    public @NotNull RecipeSerializer<FermentationBarrelRecipe> getSerializer() {
        return RecipeTypesRegistry.FERMENTATION_BARREL_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<FermentationBarrelRecipe> getType() {
        return RecipeTypesRegistry.FERMENTATION_BARREL_RECIPE_TYPE.get();
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

    public static class Serializer {
        private static final MapCodec<Boolean> WINE_BOTTLE_CODEC_INNER = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        Codec.BOOL.fieldOf("required").forGetter(b -> b)
                ).apply(inst, b -> b)
        );
        public static final MapCodec<FermentationBarrelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients")
                        .flatXmap(list -> {
                            java.util.List<Ingredient> filtered = list.stream().filter(i -> !i.isEmpty()).toList();
                            if (filtered.isEmpty()) return com.mojang.serialization.DataResult.error(() -> "No ingredients");
                            NonNullList<Ingredient> merged = NonNullList.create();
                            merged.addAll(filtered);
                            return com.mojang.serialization.DataResult.success(merged);
                        }, com.mojang.serialization.DataResult::success)
                        .forGetter(FermentationBarrelRecipe::getInputs),
                FermentationBarrelRecipeInput.JuiceData.CODEC.fieldOf("juice").forGetter(FermentationBarrelRecipe::getJuiceData),
                net.minecraft.world.item.ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.output),
                WINE_BOTTLE_CODEC_INNER.fieldOf("wine_bottle").forGetter(FermentationBarrelRecipe::isWineBottleRequired)
        ).apply(instance, FermentationBarrelRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, FermentationBarrelRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetworkInner, Serializer::fromNetworkInner);
        public static final RecipeSerializer<FermentationBarrelRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
        public static void toNetworkInner(RegistryFriendlyByteBuf buf, FermentationBarrelRecipe recipe) {
            buf.writeVarInt(recipe.inputs.size());
            for (Ingredient ingredient : recipe.inputs) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }
            FermentationBarrelRecipeInput.JuiceData.STREAM_CODEC.encode(buf, recipe.getJuiceData());
            net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.output);
            buf.writeBoolean(recipe.wineBottleRequired);
        }

        public static FermentationBarrelRecipe fromNetworkInner(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            NonNullList<Ingredient> inputs = NonNullList.create();
            for (int i = 0; i < size; i++) {
                inputs.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            }
            FermentationBarrelRecipeInput.JuiceData juiceData =
                    FermentationBarrelRecipeInput.JuiceData.STREAM_CODEC.decode(buf);
            net.minecraft.world.item.ItemStackTemplate output = net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.decode(buf);
            boolean wineBottleRequired = buf.readBoolean();
            return new FermentationBarrelRecipe(inputs, juiceData, output, wineBottleRequired);
        }
    }
}
