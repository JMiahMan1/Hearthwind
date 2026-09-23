package firenh.profundis.features.features.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.util.valueproviders.IntProviders;

public record IcicleFeatureConfig(IntProvider size, IntProvider amount, IntProvider spread, BlockState innerState,
BlockState outerState) implements FeatureConfiguration 
{
    public static final Codec<IcicleFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntProviders.CODEC.fieldOf("size").forGetter(IcicleFeatureConfig::size),
            IntProviders.CODEC.fieldOf("amount").forGetter(IcicleFeatureConfig::amount),
            IntProviders.CODEC.fieldOf("spread").forGetter(IcicleFeatureConfig::spread),
            BlockState.CODEC.fieldOf("inner_state").forGetter(IcicleFeatureConfig::innerState),
            BlockState.CODEC.fieldOf("outer_state").forGetter(IcicleFeatureConfig::outerState)
        ).apply(instance, instance.stable(IcicleFeatureConfig::new)));
}
