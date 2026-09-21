package net.satisfy.meadow.fabric.core.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import net.satisfy.meadow.core.compat.rei.MeadowReiClientPlugin;

public class MeadowReiClientPluginFabric implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        MeadowReiClientPlugin.registerCategories(registry);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        MeadowReiClientPlugin.registerDisplays(registry);
    }
}
