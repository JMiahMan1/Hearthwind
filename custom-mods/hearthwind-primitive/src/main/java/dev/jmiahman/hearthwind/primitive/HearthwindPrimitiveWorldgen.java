package dev.jmiahman.hearthwind.primitive;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Surface rock & flint worldgen: loose rocks and flints scattered on
 * the ground across all Overworld biomes (parity with earlystage / Aged),
 * enabling players to gather earlystage:rock and flint in Age 0 without tools.
 */
public final class HearthwindPrimitiveWorldgen {

    private HearthwindPrimitiveWorldgen() {
    }

    /**
     * Upstream-earlystage-exact predicate ( Globox1997/EarlyStage WorldInit ):
     * forest, hill, mountain, river biomes plus the rock_feature_biomes tag
     * (mushroom_fields + stony_shore). NOT all-overworld.
     */
    private static final TagKey<Biome> ROCK_FEATURE_BIOMES = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath("earlystage", "rock_feature_biomes"));

    public static void init() {
        java.util.function.Predicate<net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext> rockPredicate =
                context -> context.hasTag(BiomeTags.IS_FOREST) || context.hasTag(BiomeTags.IS_HILL)
                        || context.hasTag(BiomeTags.IS_MOUNTAIN) || context.hasTag(BiomeTags.IS_RIVER)
                        || context.hasTag(ROCK_FEATURE_BIOMES);
        for (String feature : new String[] {"rock", "flint"}) {
            ResourceKey<PlacedFeature> placed = ResourceKey.create(Registries.PLACED_FEATURE,
                    Identifier.fromNamespaceAndPath("earlystage", feature));
            BiomeModifications.addFeature(
                    rockPredicate,
                    GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                    placed);
        }
        HearthwindPrimitive.LOGGER.info("aged-primitive: surface rock + flint features injected (forest/hill/mountain/river + rock_feature_biomes)");
    }
}
