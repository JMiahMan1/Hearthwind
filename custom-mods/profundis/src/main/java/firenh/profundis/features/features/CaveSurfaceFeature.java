package firenh.profundis.features.features;

import java.util.Iterator;

import com.mojang.serialization.Codec;

import firenh.profundis.features.features.config.CaveSurfaceFeatureConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class CaveSurfaceFeature extends Feature<CaveSurfaceFeatureConfig> {
    public CaveSurfaceFeature(Codec<CaveSurfaceFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CaveSurfaceFeatureConfig> context) {
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        WorldGenLevel world = context.level();
        CaveSurfaceFeatureConfig config = context.config();

        int size = config.size().sample(random);
        int offset = config.offset();
        BlockState blockState = config.blockState();
        float chance = config.chance();
        boolean onSpecificBlockState = config.onSpecificBlockState();
        int amountOfCircles = config.amountOfCircles().sample(random);
        RuleTest targetBlock = config.targetBlock();
        boolean onlyOnBaseStoneOverworld = config.onlyOnBaseStoneOverworld();
        boolean generateManyCircles = config.generateManyCircles();

        if (generateManyCircles) {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (int i = 0; i < amountOfCircles; i += 1) {
                mutable.set(randomInRange(size, random) + origin.getX(), randomInRange(size, random) + origin.getY(), randomInRange(size, random) + origin.getZ());
                if (origin.distSqr(mutable) <= (double)(size) * (double)(size)) {
                    makeOneCircle(origin, mutable, random, world, size / 2, offset, blockState, chance, onSpecificBlockState, targetBlock, onlyOnBaseStoneOverworld);
                } else {
                    i -= 1;
                }
            }
        } else {
            makeOneCircle(origin, origin, random, world, size / 2, offset, blockState, chance, onSpecificBlockState, targetBlock, onlyOnBaseStoneOverworld);
        }

        return true;
    }

    private void makeOneCircle(BlockPos originalOrigin, BlockPos origin, RandomSource random, WorldGenLevel world, int size, int offset,
            BlockState blockState, float chance, boolean onSpecificBlockState, RuleTest targetBlock, boolean onlyOnBaseStoneOverworld) {
        Iterator<BlockPos> iterator = BlockPos.betweenClosed(origin.offset(-(size), -(size), -(size)), origin.offset((size), (size), (size))).iterator();

        while (iterator.hasNext()) {
            BlockPos.MutableBlockPos mutable = iterator.next().mutable();
            BlockState onState = world.getBlockState(mutable);

            if (originalOrigin.distSqr(origin) <= (double)(size) * (double)(size)) {
                if (!onSpecificBlockState) {
                    if (onState.canOcclude() && world.isEmptyBlock(mutable.above()) && !onState.equals(blockState) && useBaseStoneOverworld(onlyOnBaseStoneOverworld, onState) && mutable.distSqr(origin) <= (double)(size) * (double)(size)) {
                        if (random.nextFloat() <= chance) world.setBlock(mutable.above(offset), blockState, 3);
                    }
                } else {
                    if ((targetBlock.test(blockState, random)) && world.isEmptyBlock(mutable.above()) && mutable.distSqr(origin) <= (double)(size) * (double)(size)) {
                        if (random.nextFloat() <= chance) world.setBlock(mutable.above(offset), blockState, 3);
                    }
                }
            }
        }
    }

    private int randomInRange(int size, RandomSource random) {
        return random.nextInt(size * 2 + 1) - size;
    }

    private boolean useBaseStoneOverworld(boolean onlyOnBaseStoneOverworld, BlockState blockState) {
        if (onlyOnBaseStoneOverworld == false) {
            return true;
        }

        if (blockState.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return true;
        }

        return false;
    } 
}
