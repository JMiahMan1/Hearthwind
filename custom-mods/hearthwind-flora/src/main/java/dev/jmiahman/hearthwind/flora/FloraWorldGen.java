package dev.jmiahman.hearthwind.flora;

import java.util.function.Predicate;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Worldgen injection for wild flora, herbs, alpine flowers, and wild crops across suitable Overworld biomes.
 */
public final class FloraWorldGen {

    private FloraWorldGen() {}

    public static void init() {
        for (String feature : new String[] {"wild_flowers", "wild_herbs", "wild_crops", "apple_tree", "dark_cherry_tree"}) {
            ResourceKey<PlacedFeature> placed = ResourceKey.create(Registries.PLACED_FEATURE,
                    Identifier.fromNamespaceAndPath(HearthwindFlora.MOD_ID, feature));
            BiomeModifications.addFeature(
                    BiomeSelectors.foundInOverworld(),
                    GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                    placed);
        }
        HearthwindFlora.LOGGER.info("Hearthwind Flora: wild flowers, herbs, and wild crops registered across all Overworld biomes");
    }
}
