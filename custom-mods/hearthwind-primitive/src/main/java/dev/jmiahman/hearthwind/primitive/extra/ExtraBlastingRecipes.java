package dev.jmiahman.hearthwind.primitive.extra;

import java.util.Optional;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Registry home for the earlystage extra-blasting recipe type/serializer
 * (3 iron + 1 coal -> steel ingot, blast furnace 4th slot).
 */
public final class ExtraBlastingRecipes {

    public static final RecipeType<ExtraBlastingRecipe> TYPE = Registry.register(
            BuiltInRegistries.RECIPE_TYPE,
            ResourceKey.create(Registries.RECIPE_TYPE,
                    Identifier.fromNamespaceAndPath("earlystage", "blasting_extra")),
            new RecipeType<ExtraBlastingRecipe>() {
            });

    public static final RecipeSerializer<ExtraBlastingRecipe> SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            ResourceKey.create(Registries.RECIPE_SERIALIZER,
                    Identifier.fromNamespaceAndPath("earlystage", "blasting_extra")),
            new RecipeSerializer<>(ExtraBlastingRecipe.CODEC, ExtraBlastingRecipe.STREAM_CODEC));

    /**
     * Finds a matching extra-blasting recipe for the given main + extra
     * stacks. Deliberately bypasses RecipeManager.getRecipeFor: the generic
     * bound ties T to Recipe&lt;I&gt; while our recipe inherits
     * Recipe&lt;SingleRecipeInput&gt;, so we filter holders ourselves and
     * evaluate ExtraBlastingRecipe.matches(FurnaceExtraInput) directly.
     */
    public static Optional<RecipeHolder<ExtraBlastingRecipe>> findRecipe(ItemStack main, ItemStack extra,
            ServerLevel level) {
        for (RecipeHolder<?> holder : level.recipeAccess().getRecipes()) {
            if (holder.value() instanceof ExtraBlastingRecipe recipe && recipe.matchesExtra(main, extra)) {
                return Optional.of((RecipeHolder<ExtraBlastingRecipe>) holder);
            }
        }
        return Optional.empty();
    }

    /** Forces static registration during mod init. */
    public static void init() {
    }

    private ExtraBlastingRecipes() {
    }
}
