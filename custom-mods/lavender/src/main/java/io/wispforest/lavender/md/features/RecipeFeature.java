package io.wispforest.lavender.md.features;

import io.wispforest.lavender.LavenderClientRecipeCache;
import io.wispforest.lavender.md.ItemListComponent;
import io.wispforest.lavender.md.compiler.BookCompiler;
import io.wispforest.lavendermd.Lexer;
import io.wispforest.lavendermd.MarkdownFeature;
import io.wispforest.lavendermd.Parser;
import io.wispforest.lavendermd.compiler.MarkdownCompiler;
import io.wispforest.lavendermd.compiler.OwoUICompiler;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeFeature implements MarkdownFeature {

    private final BookCompiler.ComponentSource bookComponentSource;
    private final Map<RecipeType<?>, RecipePreviewBuilder<?>> previewBuilders;

    public static final RecipePreviewBuilder<CraftingRecipe> CRAFTING_PREVIEW_BUILDER = new RecipePreviewBuilder<>() {
        @Override
        public @NotNull UIComponent buildRecipePreview(BookCompiler.ComponentSource componentSource, net.minecraft.util.context.ContextMap slotContext, RecipeHolder<CraftingRecipe> recipeEntry) {
            var recipeComponent = componentSource.builtinTemplate(ParentUIComponent.class, "crafting-recipe");
            var value = recipeEntry.value();
            var grid = recipeComponent.childById(ParentUIComponent.class, "input-grid");

            // 26.2 exposes the authoritative grid through the recipe display:
            // width/height are the pattern's trimmed dimensions and the
            // ingredients are row-major over that width (Empty for blank
            // cells). Reading placementInfo() instead wrapped the row-major
            // slot list at the COLUMN count of the entry, which dropped a
            // trimmed 2-wide pattern (e.g. the flint axe, 2x3) into the wrong
            // cells of the 3-wide widget grid.
            var shaped = value.display().stream()
                    .filter(net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay.class::isInstance)
                    .map(net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay.class::cast)
                    .findFirst().orElse(null);
            if (shaped != null) {
                this.populateDisplayGrid(grid, shaped.width(), shaped.height(), shaped.ingredients());
                recipeComponent.childById(ItemComponent.class, "output").stack(shaped.result().resolveForFirstStack(slotContext));
                this.applyStation(recipeComponent, shaped.craftingStation(), slotContext, shaped.width() <= 2 && shaped.height() <= 2);
                return recipeComponent;
            }

            var shapeless = value.display().stream()
                    .filter(net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay.class::isInstance)
                    .map(net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay.class::cast)
                    .findFirst().orElse(null);
            if (shapeless != null) {
                this.populateDisplayGrid(grid, shapeless.ingredients().size(), 1, shapeless.ingredients());
                recipeComponent.childById(ItemComponent.class, "output").stack(shapeless.result().resolveForFirstStack(slotContext));
                this.applyStation(recipeComponent, shapeless.craftingStation(), slotContext, shapeless.ingredients().size() <= 4);
                return recipeComponent;
            }

            this.populateIngredientsGrid(recipeEntry, grid, 3, 3);
            recipeComponent.childById(ItemComponent.class, "output").stack(value.display().stream().findFirst().map(d -> d.result()).map(r -> r.resolveForFirstStack(slotContext)).orElse(ItemStack.EMPTY));

            return recipeComponent;
        }
    };

    public static final RecipePreviewBuilder<AbstractCookingRecipe> SMELTING_PREVIEW_BUILDER = (componentSource, slotContext, recipeEntry) -> {
        var recipe = recipeEntry.value();
        var recipeComponent = componentSource.builtinTemplate(ParentUIComponent.class, "smelting-recipe");

        recipeComponent.childById(ItemListComponent.class, "input").ingredient(recipe.input());
        recipeComponent.childById(ItemComponent.class, "output").stack(recipe.display().stream().findFirst().map(d -> d.result()).map(r -> r.resolveForFirstStack(slotContext)).orElse(ItemStack.EMPTY));

        var workstation = ItemStack.EMPTY;
        if (recipe instanceof SmeltingRecipe) workstation = Items.FURNACE.getDefaultInstance();
        if (recipe instanceof BlastingRecipe) workstation = Items.BLAST_FURNACE.getDefaultInstance();
        if (recipe instanceof SmokingRecipe) workstation = Items.SMOKER.getDefaultInstance();
        if (recipe instanceof CampfireCookingRecipe) workstation = Items.CAMPFIRE.getDefaultInstance();
        recipeComponent.childById(ItemComponent.class, "workstation").stack(workstation);

        return recipeComponent;
    };

    public static final RecipePreviewBuilder<SmithingRecipe> SMITHING_PREVIEW_BUILDER = (componentSource, slotContext, recipeEntry) -> {
        var recipe = recipeEntry.value();
        var recipeComponent = componentSource.builtinTemplate(ParentUIComponent.class, "smithing-recipe");

        recipe.templateIngredient().ifPresent(ingredient -> recipeComponent.childById(ItemListComponent.class, "input-1").ingredient(ingredient));
        recipeComponent.childById(ItemListComponent.class, "input-2").ingredient(recipe.baseIngredient());
        recipe.additionIngredient().ifPresent(ingredient -> recipeComponent.childById(ItemListComponent.class, "input-3").ingredient(ingredient));

        recipeComponent.childById(ItemComponent.class, "output").stack(recipe.display().stream().findFirst().map(d -> d.result()).map(r -> r.resolveForFirstStack(slotContext)).orElse(ItemStack.EMPTY));

        return recipeComponent;
    };

    public static final RecipePreviewBuilder<StonecutterRecipe> STONECUTTING_PREVIEW_BUILDER = (componentSource, slotContext, recipeEntry) -> {
        var recipe = recipeEntry.value();
        var recipeComponent = componentSource.builtinTemplate(ParentUIComponent.class, "stonecutting-recipe");

        recipeComponent.childById(ItemListComponent.class, "input").ingredient(recipe.input());
        recipeComponent.childById(ItemComponent.class, "output").stack(recipe.display().stream().findFirst().map(d -> d.result()).map(r -> r.resolveForFirstStack(slotContext)).orElse(ItemStack.EMPTY));

        return recipeComponent;
    };

    public RecipeFeature(BookCompiler.ComponentSource bookComponentSource, @Nullable Map<RecipeType<?>, RecipePreviewBuilder<?>> previewBuilders) {
        this.bookComponentSource = bookComponentSource;

        this.previewBuilders = new HashMap<>(previewBuilders != null ? previewBuilders : Map.of());
        this.previewBuilders.putIfAbsent(RecipeType.CRAFTING, CRAFTING_PREVIEW_BUILDER);
        this.previewBuilders.putIfAbsent(RecipeType.SMELTING, SMELTING_PREVIEW_BUILDER);
        this.previewBuilders.putIfAbsent(RecipeType.BLASTING, SMELTING_PREVIEW_BUILDER);
        this.previewBuilders.putIfAbsent(RecipeType.SMOKING, SMELTING_PREVIEW_BUILDER);
        this.previewBuilders.putIfAbsent(RecipeType.CAMPFIRE_COOKING, SMELTING_PREVIEW_BUILDER);
        this.previewBuilders.putIfAbsent(RecipeType.SMITHING, SMITHING_PREVIEW_BUILDER);
        this.previewBuilders.putIfAbsent(RecipeType.STONECUTTING, STONECUTTING_PREVIEW_BUILDER);
    }

    @Override
    public String name() {
        return "recipes";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return compiler instanceof OwoUICompiler;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        registrar.registerToken((nibbler, tokens) -> {
            if (!nibbler.tryConsume("<recipe;")) return false;

            var recipeIdString = nibbler.consumeUntil('>');
            if (recipeIdString == null) return false;

            var recipeId = Identifier.tryParse(recipeIdString);
            if (recipeId == null) return false;

            var recipe = LavenderClientRecipeCache.getOrFetchRecipe(recipeId);
            if (recipe.isEmpty()) return false;

            //noinspection unchecked
            tokens.add(new RecipeToken(recipeIdString, (RecipeHolder<Recipe<?>>) recipe.get()));
            return true;
        }, '<');
    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        registrar.registerNode(
            (parser, recipeToken, tokens) -> new RecipeNode(recipeToken.recipe),
            (token, tokens) -> token instanceof RecipeToken recipe ? recipe : null
        );
    }

    private static class RecipeToken extends Lexer.Token {

        public final RecipeHolder<Recipe<?>> recipe;

        public RecipeToken(String content, RecipeHolder<Recipe<?>> recipe) {
            super(content);
            this.recipe = recipe;
        }
    }

    private class RecipeNode extends Parser.Node {

        private final RecipeHolder<Recipe<?>> recipe;

        public RecipeNode(RecipeHolder<Recipe<?>> recipe) {
            this.recipe = recipe;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected void visitStart(MarkdownCompiler<?> compiler) {
            var previewBuilder = (RecipePreviewBuilder) RecipeFeature.this.previewBuilders.get(this.recipe.value().getType());
            if (previewBuilder != null) {
                ((OwoUICompiler) compiler).visitComponent(previewBuilder.buildRecipePreview(RecipeFeature.this.bookComponentSource, SlotDisplayContext.fromLevel(Minecraft.getInstance().level), this.recipe));
            } else {
                ((OwoUICompiler) compiler).visitComponent(
                    UIContainers.verticalFlow(Sizing.fill(100), Sizing.content())
                        .child(UIComponents.label(Component.literal("No preview builder registered for recipe type '" + BuiltInRegistries.RECIPE_TYPE.getId(this.recipe.value().getType()) + "'")).horizontalSizing(Sizing.fill(100)))
                        .padding(Insets.of(10))
                        .surface(Surface.flat(0x77A00000).and(Surface.outline(0x77FF0000)))
                );
            }
        }

        @Override
        protected void visitEnd(MarkdownCompiler<?> compiler) {}
    }

    @FunctionalInterface
    public interface RecipePreviewBuilder<R extends Recipe<?>> {
        @NotNull
        UIComponent buildRecipePreview(BookCompiler.ComponentSource componentSource, net.minecraft.util.context.ContextMap slotContext, RecipeHolder<R> recipeEntry);

        default void populateIngredients(RecipeHolder<R> recipe, List<Ingredient> ingredients, ParentUIComponent componentContainer) {
            for (int i = 0; i < ingredients.size(); i++) {
                if (!(componentContainer.children().get(i) instanceof ItemListComponent ingredient)) continue;
                ingredient.ingredient(ingredients.get(i));
            }
        }

        default void populateIngredientsGrid(RecipeHolder<R> recipe, ParentUIComponent componentContainer, int gridWidth, int gridHeight) {
            var ingredients = recipe.value().placementInfo().ingredients();
            var slots = recipe.value().placementInfo().slotsToIngredientIndex();
            int max = Math.min(componentContainer.children().size(), gridWidth * gridHeight);
            for (int index = 0; index < max; index++) {
                if (!(componentContainer.children().get(index) instanceof ItemListComponent ingredient)) continue;
                int input = index < slots.size() ? slots.getInt(index) : -1;
                if (input >= 0 && input < ingredients.size()) {
                    ingredient.ingredient(ingredients.get(input));
                } else {
                    ingredient.clearItems();
                }
            }
        }

        /**
         * Lays a recipe display's row-major ingredient list into the widget's
         * 3-wide grid using the RECIPE's own width, so trimmed patterns (2x2,
         * 2x3, 1x3 picked columns) do not shift their rows.
         */
        default void populateDisplayGrid(ParentUIComponent componentContainer, int recipeWidth, int recipeHeight, List<net.minecraft.world.item.crafting.display.SlotDisplay> ingredients) {
            int gridWidth = 3;
            int max = Math.min(componentContainer.children().size(), gridWidth * 3);
            for (int index = 0; index < max; index++) {
                if (!(componentContainer.children().get(index) instanceof ItemListComponent cell)) continue;
                int row = index / gridWidth;
                int column = index % gridWidth;
                if (row >= recipeHeight || column >= recipeWidth) {
                    cell.clearItems();
                    continue;
                }
                int input = row * recipeWidth + column;
                if (input >= 0 && input < ingredients.size()) {
                    cell.slotDisplay(ingredients.get(input));
                } else {
                    cell.clearItems();
                }
            }
        }

        /**
         * A recipe that fits the 2x2 inventory grid needs no workstation icon:
         * showing the crafting table for e.g. the crafting rock told readers the
         * wrong thing. Larger grids keep the display's station item.
         */
        default void applyStation(ParentUIComponent componentContainer, net.minecraft.world.item.crafting.display.SlotDisplay station, net.minecraft.util.context.ContextMap slotContext, boolean inventoryCraftable) {
            var component = componentContainer.childById(ItemComponent.class, "workstation");
            if (component == null) return;
            component.stack(inventoryCraftable ? ItemStack.EMPTY : station.resolveForFirstStack(slotContext));
        }
    }
}
