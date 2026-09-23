package firenh.profundis.features.features;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.DeltaFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class NotStupidDeltaFeature extends DeltaFeature {
    public NotStupidDeltaFeature(Codec<DeltaFeatureConfiguration> codec) {
        super(codec);
    }

    private static final ImmutableList<Block> BLOCKS = ImmutableList.of(Blocks.BEDROCK, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);
    private static final Direction[] DIRECTIONS = Direction.values();

    @Override
    public boolean place(FeaturePlaceContext<DeltaFeatureConfiguration> context) {
        boolean bl = false;
        RandomSource random = context.random();
        LevelAccessor world = context.level();
        DeltaFeatureConfiguration config = context.config();
        BlockPos blockPos = context.origin();
        boolean bl2 = random.nextDouble() < 0.9;
        int i = bl2 ? config.rimSize().sample(random) : 0;
        int j = bl2 ? config.rimSize().sample(random) : 0;
        boolean bl3 = bl2 && i != 0 && j != 0;
        int k = config.size().sample(random);
        int l = config.size().sample(random);
        int m = Math.max(k, l);
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-k, 0, -l), blockPos.offset(k, 0, l))) {
            BlockPos blockPos3;
            if (blockPos2.distManhattan(blockPos) > m) break;
            if (!canPlace(world, blockPos2, config)) continue;
            if (bl3) {
                bl = true;
                this.setBlock(world, blockPos2, config.rim());
            }
            blockPos3 = blockPos2.offset(i, 0, j);
            if (!canPlace(world, blockPos3, config)) continue;
            bl = true;
            this.setBlock(world, blockPos3, config.contents());
        }
        return bl;
    }

    private boolean canPlace(LevelAccessor world, BlockPos pos, DeltaFeatureConfiguration config) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.is(config.contents().getBlock())) {
            return false;
        }
        if (world.getBlockState(pos.above()).is(Blocks.WATER)) {
            return false;
        }
        if (BLOCKS.contains(blockState.getBlock())) {
            return false;
        }

        for (Direction direction : DIRECTIONS) {
            BlockState replaceBlockstate = world.getBlockState(pos.relative(direction));
            boolean bl = replaceBlockstate.is(config.contents().getBlock()) || replaceBlockstate.blocksMotion();
            if ((bl || direction == Direction.UP) && (!bl || direction != Direction.UP)) continue;
            return false;
        }
        return true;
    }
}
