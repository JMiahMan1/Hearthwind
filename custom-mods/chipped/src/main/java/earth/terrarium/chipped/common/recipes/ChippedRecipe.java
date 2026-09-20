package earth.terrarium.chipped.common.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.recipe.CodecRecipe;
import earth.terrarium.chipped.common.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record ChippedRecipe(
    List<Ingredient> ingredients,
    Serializer serializer
) implements CodecRecipe<RecipeInput> {

    public static final MapCodec<ChippedRecipe> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(ChippedRecipe::ingredients)
        ).apply(instance, ingredients -> new ChippedRecipe(ingredients, null)));

    public static final ByteCodec<ChippedRecipe> NETWORK_CODEC = ObjectByteCodec.create(
        ExtraByteCodecs.INGREDIENT.listOf().fieldOf(ChippedRecipe::ingredients),
        ingredients -> new ChippedRecipe(ingredients, null)
    );

    public ChippedRecipe(List<Ingredient> ingredients) {
        this(ingredients, null);
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        ItemStack stack = recipeInput.getItem(0);
        return !stack.isEmpty() && this.ingredients.stream().anyMatch(ingredient -> ingredient.test(stack));
    }

    public Stream<ItemStack> getResults(ItemStack stack) {
        if (stack.isEmpty()) return Stream.empty();
        Item input = stack.getItem();
        return this.ingredients.stream()
            .filter(ingredient -> ingredient.test(stack))
            .flatMap(Ingredient::items)
            .map(holder -> holder.value())
            .filter(item -> item != input)
            .map(ItemStack::new);
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
    public @NotNull ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return ModRecipeSerializers.forType(getType());
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return serializer == null ? null : serializer.type().get();
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return new RecipeBookCategory();
    }

    public record Serializer(Supplier<RecipeType<ChippedRecipe>> type, Supplier<Block> block) {
        public MapCodec<ChippedRecipe> codec() {
            return CODEC.xmap(
                base -> new ChippedRecipe(base.ingredients(), this),
                recipe -> recipe);
        }

        public StreamCodec<RegistryFriendlyByteBuf, ChippedRecipe> streamCodec() {
            return StreamCodec.of(
                (RegistryFriendlyByteBuf buf, ChippedRecipe recipe) -> NETWORK_CODEC.encode(recipe, buf),
                buf -> {
                    ChippedRecipe base = NETWORK_CODEC.decode(buf);
                    return new ChippedRecipe(base.ingredients(), this);
                });
        }

        public RecipeSerializer<ChippedRecipe> toRecipeSerializer() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }
    }
}
