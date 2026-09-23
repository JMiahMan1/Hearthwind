package firenh.profundis.features.features;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;

import firenh.profundis.features.features.config.LargeOreFeatureConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class GradientLargeOreFeature extends LargeOreFeature {

    public GradientLargeOreFeature(Codec<LargeOreFeatureConfig> configCodec) {
        super(configCodec);
    }

    protected Optional<BlockState> getBlockState(WorldGenLevel world, BlockPos pos, BlockState currentState, RandomSource random, List<OreConfiguration.TargetBlockState> targets) {
        for (OreConfiguration.TargetBlockState t : targets) {
            RuleTest rule = t.target;
            
            if (rule.test(currentState, random)) {
                return Optional.of(t.state);
            }
        }

        return Optional.empty();
    }
}