package firenh.profundis.features.features.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.util.valueproviders.IntProviders;

public record ShelfFungiFeatureConfig(IntProvider radius, IntProvider iterations, BlockState upperState, BlockState underState, boolean glowing) implements FeatureConfiguration {
    public static final Codec<ShelfFungiFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
       IntProviders.CODEC.fieldOf("radius").forGetter(ShelfFungiFeatureConfig::radius),
       IntProviders.CODEC.fieldOf("iterations").forGetter(ShelfFungiFeatureConfig::iterations),
       BlockState.CODEC.fieldOf("upperState").forGetter(ShelfFungiFeatureConfig::upperState),
       BlockState.CODEC.fieldOf("underState").forGetter(ShelfFungiFeatureConfig::underState),
       Codec.BOOL.fieldOf("glowing").forGetter(ShelfFungiFeatureConfig::glowing)
    ).apply(instance, instance.stable(ShelfFungiFeatureConfig::new)));
}
