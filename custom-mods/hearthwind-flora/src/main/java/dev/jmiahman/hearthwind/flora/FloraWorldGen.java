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
        Predicate<BiomeSelectionContext> terrestrialSelector = BiomeSelectors.tag(BiomeTags.IS_FOREST)
                .or(BiomeSelectors.tag(BiomeTags.IS_TAIGA))
                .or(BiomeSelectors.tag(BiomeTags.IS_JUNGLE))
                .or(BiomeSelectors.tag(BiomeTags.IS_SAVANNA))
                .or(BiomeSelectors.tag(BiomeTags.IS_HILL))
                .or(BiomeSelectors.includeByKey(
                        ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("minecraft", "plains")),
                        ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("minecraft", "sunflower_plains")),
                        ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("minecraft", "meadow")),
                        ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("minecraft", "flower_forest"))
                ));

        for (String feature : new String[] {"wild_flowers", "wild_herbs", "wild_crops"}) {
            ResourceKey<PlacedFeature> placed = ResourceKey.create(Registries.PLACED_FEATURE,
                    Identifier.fromNamespaceAndPath(HearthwindFlora.MOD_ID, feature));
            BiomeModifications.addFeature(
                    terrestrialSelector,
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    placed);
        }
        HearthwindFlora.LOGGER.info("Hearthwind Flora: wild flowers, herbs, and wild crops registered with terrestrial biome filters");
    }
}
