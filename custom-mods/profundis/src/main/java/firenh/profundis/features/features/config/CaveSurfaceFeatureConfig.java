package firenh.profundis.features.features.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.util.valueproviders.IntProviders;

public record CaveSurfaceFeatureConfig(IntProvider size, int offset, BlockState blockState, float chance, boolean onSpecificBlockState, IntProvider amountOfCircles, RuleTest targetBlock, boolean onlyOnBaseStoneOverworld, boolean generateManyCircles) implements FeatureConfiguration {
    public static final Codec<CaveSurfaceFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
       IntProviders.CODEC.fieldOf("size").forGetter(CaveSurfaceFeatureConfig::size),
       Codec.INT.fieldOf("offset").forGetter(CaveSurfaceFeatureConfig::offset),
       BlockState.CODEC.fieldOf("blockState").forGetter(CaveSurfaceFeatureConfig::blockState),
       Codec.FLOAT.fieldOf("chance").forGetter(CaveSurfaceFeatureConfig::chance),
       Codec.BOOL.fieldOf("onSpecificBlock").forGetter(CaveSurfaceFeatureConfig::onSpecificBlockState),
       IntProviders.CODEC.fieldOf("amountOfCircles").forGetter(CaveSurfaceFeatureConfig::amountOfCircles),
       RuleTest.CODEC.fieldOf("targetBlock").forGetter(CaveSurfaceFeatureConfig::targetBlock),
       Codec.BOOL.fieldOf("onlyOnBaseStoneOverworld").forGetter(CaveSurfaceFeatureConfig::onlyOnBaseStoneOverworld),
       Codec.BOOL.fieldOf("generateManyCircles").forGetter(CaveSurfaceFeatureConfig::generateManyCircles)
    ).apply(instance, instance.stable(CaveSurfaceFeatureConfig::new)));
}
