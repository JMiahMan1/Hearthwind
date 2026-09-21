package net.satisfy.brewery.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;
import net.satisfy.brewery.Brewery;
import net.satisfy.brewery.fabric.core.world.BreweryBiomeModification;

import java.util.Optional;

public class BreweryFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Brewery.init();
        BreweryBiomeModification.init();

        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(Brewery.MOD_ID);
        modContainer.ifPresent(container -> {
            // Sobriety: the alcohol datapack (beer/whiskey brewing recipes)
            // only exists when alcohol is enabled.
            if (!dev.jmiahman.hearthwind.survival.Sobriety.alcoholRemoved()) {
                ResourceLoader.registerBuiltinPack(
                        Identifier.fromNamespaceAndPath(Brewery.MOD_ID, "brewery_alcohol"),
                        container,
                        PackActivationType.ALWAYS_ENABLED);
            }
        });
    }
}
