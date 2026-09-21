package dev.jmiahman.hearthwind.world.pine;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class PineCoarseDirtLedges {
    private static final ResourceKey<PlacedFeature> PINE_LEDGE =
            ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath("hearthwind", "coarse_dirt_ledge_pine"));

    private PineCoarseDirtLedges() {}

    public static void register() {
        var selector = BiomeSelectors.includeByKey(
                        ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("minecraft", "old_growth_pine_taiga")))
                .or(BiomeSelectors.includeByKey(
                        ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("natures_spirit", "alpine_clearings"))))
                .or(BiomeSelectors.includeByKey(
                        ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("natures_spirit", "alpine_highlands"))))
                .or(BiomeSelectors.includeByKey(
                        ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("minecraft", "grove"))));
        BiomeModifications.addFeature(selector, GenerationStep.Decoration.UNDERGROUND_ORES, PINE_LEDGE);
    }
}
