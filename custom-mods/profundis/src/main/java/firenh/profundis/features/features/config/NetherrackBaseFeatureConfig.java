package firenh.profundis.features.features.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.util.valueproviders.IntProviders;

public record NetherrackBaseFeatureConfig(
    IntProvider radius, IntProvider height, BlockState topState, BlockState underState, 
    RuleTest canPlaceThrough, IntProvider count, boolean sometimesUseUnderAsTop, boolean contained

) implements FeatureConfiguration {

    public static final Codec<NetherrackBaseFeatureConfig> CODEC = RecordCodecBuilder
        .create(instance -> instance.group(
            IntProviders.CODEC.fieldOf("radius").orElse(UniformInt.of(4, 5))
                .forGetter(config -> config.radius),
            IntProviders.CODEC.fieldOf("height").orElse(UniformInt.of(3, 4))
                .forGetter(config -> config.height),
            BlockState.CODEC.fieldOf("top_state")
                .forGetter(config -> config.topState),
            BlockState.CODEC.fieldOf("under_state")
                .forGetter(config -> config.underState),
            RuleTest.CODEC.fieldOf("can_be_placed_on")
                .forGetter(config -> config.canPlaceThrough),
            IntProviders.CODEC.fieldOf("count").orElse(UniformInt.of(4, 5))
                .forGetter(config -> config.count),
            Codec.BOOL.fieldOf("sometimes_use_under_as_top").orElse(false)
                .forGetter(config -> config.sometimesUseUnderAsTop),
            Codec.BOOL.fieldOf("contained").orElse(false)
                .forGetter(config -> config.contained)
        ).apply(instance, NetherrackBaseFeatureConfig::new)
    );
}
