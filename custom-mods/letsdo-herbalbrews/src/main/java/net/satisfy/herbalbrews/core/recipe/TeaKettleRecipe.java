package net.satisfy.herbalbrews.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.satisfy.herbalbrews.core.registry.RecipeTypeRegistry;
import net.satisfy.herbalbrews.core.util.HerbalBrewsUtil;
import org.jetbrains.annotations.NotNull;

public class TeaKettleRecipe implements Recipe<RecipeInput> {
    private final NonNullList<Ingredient> inputs;
    private final net.minecraft.world.item.ItemStackTemplate output;
    private final Identifier effect;
    private final int effectDuration;
    private final int requiredWater;
    private final int requiredHeat;
    private final int requiredDuration;
    private final float experience;

    public TeaKettleRecipe(NonNullList<Ingredient> inputs, net.minecraft.world.item.ItemStackTemplate output, Identifier effect, int effectDuration, int requiredWater, int requiredHeat, int requiredDuration, float experience) {
        this.inputs = inputs;
        this.output = output;
        this.effect = effect;
        this.effectDuration = effectDuration;
        this.requiredWater = requiredWater;
        this.requiredHeat = requiredHeat;
        this.requiredDuration = requiredDuration;
        this.experience = experience;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        return HerbalBrewsUtil.matchesRecipe(recipeInput, inputs, 0, 5) /*&& waterLevelSufficient(recipeInput) && heatLevelSufficient(recipeInput) TODO fixme*/;
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput) {
        return this.output.create();
    }

    public ItemStack assemble() {
        return this.output.create();
    }

    public @NotNull ItemStack getResultItem() {
        return this.output.create();
    }

    public net.minecraft.world.item.ItemStackTemplate getOutput() {
        return this.output;
    }

    public Identifier getEffect() {
        return this.effect;
    }

    public int getEffectDuration() {
        return this.effectDuration;
    }

    public int getRequiredWater() {
        return this.requiredWater;
    }

    public int getRequiredHeat() {
        return this.requiredHeat;
    }

    public int getRequiredDuration() {
        return this.requiredDuration;
    }

    public float getExperience() {
        return experience;
    }

    public @NotNull Identifier getId() {
        return RecipeTypeRegistry.TEA_KETTLE_RECIPE_TYPE.getId();
    }

    @Override
    public @NotNull RecipeSerializer<TeaKettleRecipe> getSerializer() {
        return RecipeTypeRegistry.TEA_KETTLE_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<TeaKettleRecipe> getType() {
        return RecipeTypeRegistry.TEA_KETTLE_RECIPE_TYPE.get();
    }

    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
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

        public static final MapCodec<TeaKettleRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap(list -> {
                            // NOTE: never call Ingredient.isEmpty() here - tag
                            // holders are unbound during datapack parsing and
                            // it throws. Empty checks belong in matches().
                            if (list.isEmpty()) {
                                return DataResult.error(() -> "No ingredients for Tea Kettle recipe");
                            } else if (list.size() > 6) {
                                return DataResult.error(() -> "Too many ingredients for Tea Kettle recipe");
                            } else {
                                NonNullList<Ingredient> merged = NonNullList.create();
                                merged.addAll(list);
                                return DataResult.success(merged);
                            }
                        }, DataResult::success).forGetter(teaKettleRecipe -> {
                            NonNullList<Ingredient> merged = NonNullList.create();
                            merged.addAll(teaKettleRecipe.inputs);
                            return merged;
                        }),
                        net.minecraft.world.item.ItemStackTemplate.CODEC.fieldOf("result").forGetter(TeaKettleRecipe::getOutput),
                        Identifier.CODEC.fieldOf("effect").forGetter(TeaKettleRecipe::getEffect),
                        Codec.INT.fieldOf("effect_duration").forGetter(TeaKettleRecipe::getEffectDuration),
                        Codec.INT.fieldOf("fluid_amount").forGetter(TeaKettleRecipe::getRequiredWater),
                        Codec.INT.fieldOf("heat_amount").forGetter(TeaKettleRecipe::getRequiredHeat),
                        Codec.INT.fieldOf("crafting_duration").forGetter(TeaKettleRecipe::getRequiredDuration),
                        Codec.FLOAT.fieldOf("experience").forGetter(TeaKettleRecipe::getExperience)
                ).apply(instance, TeaKettleRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, TeaKettleRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        public static final RecipeSerializer<TeaKettleRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        public static @NotNull TeaKettleRecipe fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            int i = registryFriendlyByteBuf.readVarInt();
            NonNullList<Ingredient> nonNullList = NonNullList.create();
            for (int j = 0; j < i; j++) {
                nonNullList.add(Ingredient.CONTENTS_STREAM_CODEC.decode(registryFriendlyByteBuf));
            }
            net.minecraft.world.item.ItemStackTemplate output = net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.decode(registryFriendlyByteBuf);

            Identifier effect = null;
            int effectDuration = 0;
            boolean hasEffect = registryFriendlyByteBuf.readBoolean();
            if (hasEffect) {
                effect = registryFriendlyByteBuf.readIdentifier();
                effectDuration = registryFriendlyByteBuf.readInt();
            }
            int requiredWater = registryFriendlyByteBuf.readInt();
            int requiredHeat = registryFriendlyByteBuf.readInt();
            int requiredDuration = registryFriendlyByteBuf.readInt();
            float experience = registryFriendlyByteBuf.readFloat();
            return new TeaKettleRecipe(nonNullList, output, effect, effectDuration, requiredWater, requiredHeat, requiredDuration, experience);
        }

        public static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, TeaKettleRecipe recipe) {
            registryFriendlyByteBuf.writeVarInt(recipe.getIngredients().size());

            for (Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(registryFriendlyByteBuf, ingredient);
            }

            net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.encode(registryFriendlyByteBuf, recipe.output);

            if (recipe.effect != null) {
                registryFriendlyByteBuf.writeBoolean(true);
                registryFriendlyByteBuf.writeIdentifier(recipe.effect);
                registryFriendlyByteBuf.writeInt(recipe.effectDuration);
            } else {
                registryFriendlyByteBuf.writeBoolean(false);
            }
            registryFriendlyByteBuf.writeInt(recipe.requiredWater);
            registryFriendlyByteBuf.writeInt(recipe.requiredHeat);
            registryFriendlyByteBuf.writeInt(recipe.requiredDuration);
            registryFriendlyByteBuf.writeFloat(recipe.experience);
        }
    }

    public static class Type implements RecipeType<TeaKettleRecipe> {
        private Type() {
        }
    }
}
