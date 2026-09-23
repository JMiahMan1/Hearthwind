package firenh.profundis.features;

import firenh.profundis.Profundis;
import firenh.profundis.features.features.AmethystVeinFeature;
import firenh.profundis.features.features.CavePillarFeature;
import firenh.profundis.features.features.CaveSurfaceFeature;
import firenh.profundis.features.features.FrozenWaterFeature;
import firenh.profundis.features.features.IcicleFeature;
import firenh.profundis.features.features.LargeOreFeature;
import firenh.profundis.features.features.LavaFixerFeature;
import firenh.profundis.features.features.NetherrackBaseFeature;
import firenh.profundis.features.features.NotStupidDeltaFeature;
import firenh.profundis.features.features.PaintedCavesLargeOreFeature;
import firenh.profundis.features.features.ShelfFungiFeature;
import firenh.profundis.features.features.TerracottaBandsLargeOreFeature;
import firenh.profundis.features.features.config.AmethystVeinFeatureConfig;
import firenh.profundis.features.features.config.CavePillarFeatureConfig;
import firenh.profundis.features.features.config.CaveSurfaceFeatureConfig;
import firenh.profundis.features.features.config.IcicleFeatureConfig;
import firenh.profundis.features.features.config.LargeOreFeatureConfig;
import firenh.profundis.features.features.config.NetherrackBaseFeatureConfig;
import firenh.profundis.features.features.config.ShelfFungiFeatureConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ProfundisFeatures {
    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String id, F feature) {
        return (F)Registry.register(BuiltInRegistries.FEATURE, Profundis.id(id), feature);
    }

    public final static Feature<IcicleFeatureConfig> ICICLE_FEATURE;
    public final static Feature<CaveSurfaceFeatureConfig> CAVE_SURFACE_FEATURE;
    public final static Feature<CavePillarFeatureConfig> CAVE_PILLAR_FEATURE;
    public final static Feature<NoneFeatureConfiguration> LAVA_FIXER_FEATURE;
    public final static Feature<DeltaFeatureConfiguration> NOT_STUPID_DELTA_FEATURE;
    public final static Feature<NetherrackBaseFeatureConfig> NETHERRACK_BASE_FEATURE;
    public final static Feature<ShelfFungiFeatureConfig> SHELF_FUNGI_FEATURE;
    public final static Feature<AmethystVeinFeatureConfig> AMETHYST_VEIN_FEATURE;
    public final static Feature<LargeOreFeatureConfig> LARGE_ORE_FEATURE;
    public final static Feature<LargeOreFeatureConfig> TERACOTTA_BANDS_LARGE_ORE_FEATURE;
    public final static Feature<NoneFeatureConfiguration> FROZEN_WATER_FEATURE;
    public final static Feature<LargeOreFeatureConfig> PAINTED_CAVES_LARGE_ORE_FEATURE;

    static {
        ICICLE_FEATURE = register("icicle", new IcicleFeature(IcicleFeatureConfig.CODEC));
        CAVE_SURFACE_FEATURE = register("cave_surface_feature", new CaveSurfaceFeature(CaveSurfaceFeatureConfig.CODEC));
        CAVE_PILLAR_FEATURE = register("cave_pillar_feature", new CavePillarFeature(CavePillarFeatureConfig.CODEC));
        LAVA_FIXER_FEATURE = register("lava_fixer_feature", new LavaFixerFeature(NoneFeatureConfiguration.CODEC));
        NOT_STUPID_DELTA_FEATURE = register("not_stupid_delta_feature", new NotStupidDeltaFeature(DeltaFeatureConfiguration.CODEC));
        NETHERRACK_BASE_FEATURE = register("netherrack_base_feature", new NetherrackBaseFeature(NetherrackBaseFeatureConfig.CODEC));
        SHELF_FUNGI_FEATURE = register("shelf_fungi_feature", new ShelfFungiFeature(ShelfFungiFeatureConfig.CODEC));
        AMETHYST_VEIN_FEATURE = register("amethyst_vein_feature", new AmethystVeinFeature(AmethystVeinFeatureConfig.CODEC));
        LARGE_ORE_FEATURE = register("large_ore_feature", new LargeOreFeature(LargeOreFeatureConfig.CODEC)); 
        TERACOTTA_BANDS_LARGE_ORE_FEATURE = register("terracotta_bands_large_ore_feature", new TerracottaBandsLargeOreFeature(LargeOreFeatureConfig.CODEC));  
        FROZEN_WATER_FEATURE = register("frozen_water_feature", new FrozenWaterFeature(NoneFeatureConfiguration.CODEC));
        PAINTED_CAVES_LARGE_ORE_FEATURE = register("painted_caves_large_ore_feature", new PaintedCavesLargeOreFeature(LargeOreFeatureConfig.CODEC));
    }

    public static void init() {
        new ProfundisFeatures();
    }

}
