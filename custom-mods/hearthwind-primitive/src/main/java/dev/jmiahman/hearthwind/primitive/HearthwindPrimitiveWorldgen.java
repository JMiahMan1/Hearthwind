package dev.jmiahman.hearthwind.primitive;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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

    public static void init() {
        for (String feature : new String[] {"rock", "flint"}) {
            ResourceKey<PlacedFeature> placed = ResourceKey.create(Registries.PLACED_FEATURE,
                    Identifier.fromNamespaceAndPath("earlystage", feature));
            BiomeModifications.addFeature(
                    BiomeSelectors.foundInOverworld(),
                    GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                    placed);
        }
        HearthwindPrimitive.LOGGER.info("aged-primitive: surface rock + flint features injected into all Overworld biomes");
    }
}
