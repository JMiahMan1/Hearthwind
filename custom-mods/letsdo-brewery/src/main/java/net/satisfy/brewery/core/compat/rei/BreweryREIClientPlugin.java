package net.satisfy.brewery.core.compat.rei;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.satisfy.brewery.core.compat.rei.category.BrewingStationCategory;
import net.satisfy.brewery.core.compat.rei.display.BrewingStationDisplay;
import net.satisfy.brewery.core.recipe.BrewingRecipe;
import net.satisfy.brewery.core.registry.ObjectRegistry;

public class BreweryREIClientPlugin {

    public static void registerCategories(CategoryRegistry registry) {
        registry.add(new BrewingStationCategory());

        registry.addWorkstations(BrewingStationCategory.BREWING_STATION_DISPLAY, EntryStacks.of(ObjectRegistry.WOODEN_BREWINGSTATION.get()));
        registry.addWorkstations(BrewingStationCategory.BREWING_STATION_DISPLAY, EntryStacks.of(ObjectRegistry.COPPER_BREWINGSTATION.get()));
        registry.addWorkstations(BrewingStationCategory.BREWING_STATION_DISPLAY, EntryStacks.of(ObjectRegistry.NETHERITE_BREWINGSTATION.get()));
    }


    public static void registerDisplays(DisplayRegistry registry) {
        registry.beginFiller(net.minecraft.world.item.crafting.RecipeHolder.class)
                .filter(holder -> holder.value() instanceof BrewingRecipe)
                .fill(holder -> new BrewingStationDisplay((BrewingRecipe) holder.value()));
    }
}