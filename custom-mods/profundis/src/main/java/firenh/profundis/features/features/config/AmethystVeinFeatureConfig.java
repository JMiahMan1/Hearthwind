package firenh.profundis.features.features.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.util.valueproviders.IntProviders;

public record AmethystVeinFeatureConfig(boolean undergroundOnly, IntProvider radius, IntProvider range, IntProvider targetCount) implements FeatureConfiguration {
    public static final Codec<AmethystVeinFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
       Codec.BOOL.fieldOf("underground_only").forGetter(AmethystVeinFeatureConfig::undergroundOnly),
       IntProviders.NON_NEGATIVE_CODEC.fieldOf("radius").forGetter(AmethystVeinFeatureConfig::radius),
       IntProviders.POSITIVE_CODEC.fieldOf("range").forGetter(AmethystVeinFeatureConfig::range),
       IntProviders.POSITIVE_CODEC.fieldOf("target_count").forGetter(AmethystVeinFeatureConfig::targetCount)
    ).apply(instance, instance.stable(AmethystVeinFeatureConfig::new)));
}
