package firenh.profundis.features.features.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.util.valueproviders.IntProviders;

public record CavePillarFeatureConfig(BlockState innerState, BlockState outerState, IntProvider sizeOfEnds, IntProvider lengthOfEnds, Holder<PlacedFeature> bottomFeature) implements FeatureConfiguration {
    public static final Codec<CavePillarFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
       BlockState.CODEC.fieldOf("innerState").forGetter(CavePillarFeatureConfig::innerState),
       BlockState.CODEC.fieldOf("outerState").forGetter(CavePillarFeatureConfig::outerState),
       IntProviders.CODEC.fieldOf("sizeOfEnds").forGetter(CavePillarFeatureConfig::sizeOfEnds),
       IntProviders.CODEC.fieldOf("lengthOfEnds").forGetter(CavePillarFeatureConfig::lengthOfEnds),
       PlacedFeature.CODEC.fieldOf("bottom_feature").forGetter(CavePillarFeatureConfig::bottomFeature)
    ).apply(instance, instance.stable(CavePillarFeatureConfig::new)));
}