package net.satisfy.meadow.core.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.satisfy.meadow.core.registry.RecipeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CookingCauldronRecipe implements Recipe<CraftingInput> {
    private final NonNullList<Ingredient> inputs;
    private final ItemStackTemplate output;
    private final int fluidAmount;
    private final int craftingDuration;

    public CookingCauldronRecipe(NonNullList<Ingredient> inputs, ItemStackTemplate output, int fluidAmount, int craftingDuration) {
        this.inputs = inputs;
        this.output = output;
        this.fluidAmount = fluidAmount;
        this.craftingDuration = craftingDuration;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level level) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack s = inventory.getItem(i);
            if (!s.isEmpty()) items.add(s);
        }
        boolean[] used = new boolean[items.size()];
        for (Ingredient ing : inputs) {
            boolean ok = false;
            for (int i = 0; i < items.size(); i++) {
                if (!used[i] && ing.test(items.get(i))) {
                    used[i] = true;
                    ok = true;
                    break;
                }
            }
            if (!ok) return false;
        }
        return true;
    }

    public ItemStack assemble() {
        return output.create();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input) {
        return output.create();
    }

    public ItemStack getResultItem() {
        return output.create();
    }

    public int getFluidAmount() {
        return fluidAmount;
    }

    public int getCraftingDuration() {
        return craftingDuration;
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(getIngredients());
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

    @Override
    public @NotNull RecipeSerializer<CookingCauldronRecipe> getSerializer() {
        return RecipeRegistry.COOKING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<CookingCauldronRecipe> getType() {
        return RecipeRegistry.COOKING.get();
    }

    public static class Serializer {
        public static final MapCodec<CookingCauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").xmap(list -> {
                    NonNullList<Ingredient> nl = NonNullList.create();
                    nl.addAll(list);
                    return nl;
                }, List::copyOf).forGetter(r -> r.inputs),
                ItemStackTemplate.MAP_CODEC.fieldOf("result").forGetter(r -> r.output),
                Codec.INT.optionalFieldOf("fluid_amount", 0).forGetter(r -> r.fluidAmount),
                Codec.INT.optionalFieldOf("crafting_duration", 10).forGetter(r -> r.craftingDuration)
        ).apply(i, CookingCauldronRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CookingCauldronRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC), r -> r.inputs,
                ItemStackTemplate.STREAM_CODEC, r -> r.output,
                ByteBufCodecs.VAR_INT, r -> r.fluidAmount,
                ByteBufCodecs.VAR_INT, r -> r.craftingDuration,
                CookingCauldronRecipe::new
        );

        public static final RecipeSerializer<CookingCauldronRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
