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
        // 1. Alpine Wildflowers (Meadows, Peaks, Slopes, Alpine biomes)
        Predicate<BiomeSelectionContext> alpineSelector = ctx ->
                ctx.hasTag(BiomeTags.IS_MOUNTAIN) ||
                ctx.getBiomeKey().identifier().getPath().contains("meadow") ||
                ctx.getBiomeKey().identifier().getPath().contains("peak") ||
                ctx.getBiomeKey().identifier().getPath().contains("slope") ||
                ctx.getBiomeKey().identifier().getPath().contains("alpine") ||
                ctx.getBiomeKey().identifier().getPath().contains("hill");

        addFeature("wild_flowers", alpineSelector);

        // 2. Herbs & Teas (Forests, Jungles, Savannas, Taigas)
        Predicate<BiomeSelectionContext> herbSelector = ctx ->
                ctx.hasTag(BiomeTags.IS_FOREST) || ctx.hasTag(BiomeTags.IS_JUNGLE) ||
                ctx.hasTag(BiomeTags.IS_SAVANNA) || ctx.hasTag(BiomeTags.IS_TAIGA) ||
                ctx.getBiomeKey().identifier().getPath().contains("forest") ||
                ctx.getBiomeKey().identifier().getPath().contains("jungle") ||
                ctx.getBiomeKey().identifier().getPath().contains("savanna");

        addFeature("wild_herbs", herbSelector);

        // 3. Wild Crops (Plains, Meadows, River Valleys, Valleys)
        Predicate<BiomeSelectionContext> cropSelector = ctx ->
                ctx.hasTag(BiomeTags.IS_FOREST) || ctx.hasTag(BiomeTags.IS_RIVER) ||
                ctx.getBiomeKey().identifier().getPath().contains("plains") ||
                ctx.getBiomeKey().identifier().getPath().contains("meadow") ||
                ctx.getBiomeKey().identifier().getPath().contains("river") ||
                ctx.getBiomeKey().identifier().getPath().contains("valley");

        addFeature("wild_crops", cropSelector);

        // 4. Apple Trees & Dark Cherry Trees (Forests, Plains, Meadows)
        Predicate<BiomeSelectionContext> treeSelector = ctx ->
                ctx.hasTag(BiomeTags.IS_FOREST) ||
                ctx.getBiomeKey().identifier().getPath().contains("plains") ||
                ctx.getBiomeKey().identifier().getPath().contains("forest") ||
                ctx.getBiomeKey().identifier().getPath().contains("meadow");

        addFeature("apple_tree", treeSelector);
        addFeature("dark_cherry_tree", treeSelector);

        HearthwindFlora.LOGGER.info("Hearthwind Flora: Authentic biome & elevation worldgen registered!");
    }

    private static void addFeature(String featureName, Predicate<BiomeSelectionContext> selector) {
        ResourceKey<PlacedFeature> placed = ResourceKey.create(Registries.PLACED_FEATURE,
                Identifier.fromNamespaceAndPath(HearthwindFlora.MOD_ID, featureName));
        BiomeModifications.addFeature(selector, GenerationStep.Decoration.VEGETAL_DECORATION, placed);
    }
}
