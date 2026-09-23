package firenh.profundis.features.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;

import firenh.profundis.features.features.config.LargeOreFeatureConfig;
import firenh.profundis.util.ProfundisTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;

public class PaintedCavesLargeOreFeature extends LargeOreFeature {
    private static final List<BlockState> TERRACOTTA_BLOCKS = List.of(
        Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.MAGENTA).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.PINK).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE).defaultBlockState(),
        Blocks.DYED_TERRACOTTA.pick(DyeColor.MAGENTA).defaultBlockState()
    );

    
    private final float SCALE = 2;
    private final long seed = -224983484687588995L;
    private final NormalNoise.NoiseParameters noiseParameters = new NormalNoise.NoiseParameters(-4, 1);
    private final NoiseProvider STATE_PROVIDER = new NoiseProvider(seed, noiseParameters, SCALE, TERRACOTTA_BLOCKS);

    public PaintedCavesLargeOreFeature(Codec<LargeOreFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    protected Optional<BlockState> getBlockState(WorldGenLevel world, BlockPos pos, BlockState currentState, RandomSource random, List<OreConfiguration.TargetBlockState> targets) {
        try {
            if (currentState.is(BlockTags.BASE_STONE_OVERWORLD)) {
                return Optional.of(
                    STATE_PROVIDER.getState(world, random, pos)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    // private double getNoiseValue(BlockPos pos, double scale) {
    //     return this.noiseSampler.sample((double)pos.getX() * scale, (double)pos.getY() * scale, (double)pos.getZ() * scale);
    // }

    
}
