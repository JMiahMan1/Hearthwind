package dev.jmiahman.hearthwind.primitive.extra;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Earlystage "extra blasting": 3 iron ingots + 1 coal -> 1 steel ingot in a
 * blast furnace. The blast furnace gains a 4th slot (see mixins) which holds
 * the extra ingredient while the input slot holds the main ingredient stack.
 *
 * Registered under the original earlystage: namespace for datapack parity.
 */
public class ExtraBlastingRecipe extends AbstractCookingRecipe {

    private final Ingredient extraIngredient;
    private final int inputCount;
    private final int extraCount;

    public ExtraBlastingRecipe(Ingredient input, int inputCount, Ingredient extraIngredient, int extraCount,
            ItemStackTemplate result, float experience, int cookingTime) {
        super(new Recipe.CommonInfo(true),
                new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, ""),
                input, result, experience, cookingTime);
        this.extraIngredient = extraIngredient;
        this.inputCount = inputCount;
        this.extraCount = extraCount;
    }

    public int inputCount() {
        return inputCount;
    }

    public int extraCount() {
        return extraCount;
    }

    public Ingredient extraIngredient() {
        return extraIngredient;
    }

    /**
     * Direct two-slot match (main ingredient + extra ingredient). Deliberately
     * NOT an override of Recipe.matches: the erased generic signature collides
     * (Recipe.matches(SingleRecipeInput,...) vs (RecipeInput,...)). Matching is
     * driven by ExtraBlastingRecipes.findRecipe instead.
     */
    public boolean matchesExtra(ItemStack main, ItemStack extra) {
        return this.input().test(main) && main.getCount() >= inputCount
                && extraIngredient.test(extra) && extra.getCount() >= extraCount;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.BLAST_FURNACE_MISC;
    }

    @Override
    public RecipeSerializer<? extends AbstractCookingRecipe> getSerializer() {
        return ExtraBlastingRecipes.SERIALIZER;
    }

    @Override
    public RecipeType<? extends AbstractCookingRecipe> getType() {
        return ExtraBlastingRecipes.TYPE;
    }

    @Override
    protected net.minecraft.world.item.Item furnaceIcon() {
        return Items.BLAST_FURNACE;
    }

    /** Public bridge to the protected single-item result template. */
    public ItemStackTemplate resultStack() {
        return result();
    }

    /** Consumes the non-standard part: extra ingredient + main count beyond the 1 vanilla burn already shrank. */
    public void consumeInputs(NonNullList<ItemStack> items) {
        ItemStack main = items.get(0);
        int remain = inputCount - 1;
        if (remain > 0 && this.input().test(main)) {
            main.shrink(Math.min(remain, main.getCount()));
        }
        ItemStack extra = items.get(3);
        if (extraIngredient.test(extra)) {
            extra.shrink(Math.min(extraCount, extra.getCount()));
        }
    }

    /** RecipeInput view of the blast furnace: slot 0 = main ingredient, slot 1 = extra ingredient. */
    public record FurnaceExtraInput(ItemStack main, ItemStack extra) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return index == 0 ? main : extra;
        }

        @Override
        public int size() {
            return 2;
        }
    }

    public static final MapCodec<ExtraBlastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(ExtraBlastingRecipe::input),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ExtraBlastingRecipe::inputCount),
            Ingredient.CODEC.fieldOf("extraingredient").forGetter(ExtraBlastingRecipe::extraIngredient),
            Codec.INT.optionalFieldOf("extra_count", 1).forGetter(ExtraBlastingRecipe::extraCount),
            ItemStackTemplate.MAP_CODEC.fieldOf("result").forGetter(ExtraBlastingRecipe::resultStack),
            Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(AbstractCookingRecipe::experience),
            Codec.INT.optionalFieldOf("cookingtime", 100).forGetter(AbstractCookingRecipe::cookingTime))
            .apply(instance, ExtraBlastingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExtraBlastingRecipe> STREAM_CODEC = StreamCodec
            .ofMember(ExtraBlastingRecipe::encode, ExtraBlastingRecipe::decode);

    private static void encode(ExtraBlastingRecipe recipe, RegistryFriendlyByteBuf buf) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input());
        ByteBufCodecs.VAR_INT.encode(buf, recipe.inputCount);
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.extraIngredient);
        ByteBufCodecs.VAR_INT.encode(buf, recipe.extraCount);
        ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.resultStack());
        buf.writeFloat(recipe.experience());
        ByteBufCodecs.VAR_INT.encode(buf, recipe.cookingTime());
    }

    private static ExtraBlastingRecipe decode(RegistryFriendlyByteBuf buf) {
        Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        int inputCount = ByteBufCodecs.VAR_INT.decode(buf);
        Ingredient extra = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        int extraCount = ByteBufCodecs.VAR_INT.decode(buf);
        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
        float experience = buf.readFloat();
        int cookingTime = ByteBufCodecs.VAR_INT.decode(buf);
        return new ExtraBlastingRecipe(input, inputCount, extra, extraCount, result, experience, cookingTime);
    }
}
