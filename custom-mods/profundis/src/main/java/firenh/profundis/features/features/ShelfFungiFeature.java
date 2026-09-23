package firenh.profundis.features.features;

import java.util.Iterator;

import com.mojang.serialization.Codec;

import firenh.profundis.features.features.config.ShelfFungiFeatureConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class ShelfFungiFeature extends Feature<ShelfFungiFeatureConfig> {
    public ShelfFungiFeature(Codec<ShelfFungiFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ShelfFungiFeatureConfig> context) {
        BlockPos origin = context.origin();
        ShelfFungiFeatureConfig config = context.config();
        RandomSource random = context.random();
        WorldGenLevel world = context.level();

        BlockState upperState = config.upperState();
        BlockState underState = config.underState();
        boolean glowing = config.glowing();
        int radius = config.radius().sample(random);
        int iterations = config.iterations().sample(random);
        int radiusExt = (int)(radius * 1.5);

        // world.setBlock(origin, upperState, 3);

        boolean hasPlaced = false;

        Iterator<BlockPos> iter = BlockPos.betweenClosed(origin.offset(-(radiusExt), -(0), -(radiusExt)), origin.offset((radiusExt), (0), (radiusExt))).iterator();

        while (iter.hasNext()) {
            BlockPos next = iter.next();
            // world.setBlock(next, upperState, 3);

            if (isValidLocation(origin, next, iterations, radius, random, world)) {
                world.setBlock(next, upperState, 3);
                hasPlaced = true;
                // Profundis.LOGGER.info("placements: " + placements);
            }
        }

        if (!hasPlaced) return false;

        if (radius < 6) return hasPlaced;

        Iterator<BlockPos> iter2 = BlockPos.betweenClosed(origin.below().offset(-(radius), -(0), -(radius)), origin.below().offset((radius), (0), (radius))).iterator();

        while (iter2.hasNext()) {
            BlockPos next = iter2.next();
            if (isValidLocation(origin.below(), next, iterations, radius * 3 / 4, random, world)) {
                if (glowing && random.nextFloat() < 0.05) {
                    world.setBlock(next, Blocks.SHROOMLIGHT.defaultBlockState(), 3);

                } else {
                    world.setBlock(next, underState, 3);
                }
            }
        }

        if (radius < 8) return hasPlaced;

        Iterator<BlockPos> iter3 = BlockPos.betweenClosed(origin.above().offset(-(radius), -(0), -(radius)), origin.above().offset((radius), (0), (radius))).iterator();

        while (iter3.hasNext()) {
            BlockPos next = iter3.next();
            if (isValidLocation(origin.above(), next, iterations, radius * 2 / 3, random, world)) {
                world.setBlock(next, upperState, 3);
            }
        }

        return hasPlaced;
    }

    public boolean isValidLocation(BlockPos origin, BlockPos toPlaceAt, int iterations, int radius, RandomSource random, WorldGenLevel world) {
        BlockState placeAtState = world.getBlockState(toPlaceAt);
        
        if (placeAtState.canOcclude()) {
            if (placeAtState.is(BlockTags.MOSS_REPLACEABLE)) {
                if (
                    world.getBlockState(toPlaceAt.above()).canOcclude()
                    && world.getBlockState(toPlaceAt.below()).canOcclude()
                ) {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        // Profundis.LOGGER.info("radius: " + radius);

        double rad = 0;
        double theta = getAngle((toPlaceAt.getZ() - origin.getZ()), (toPlaceAt.getX() - origin.getX()));
        // Profundis.LOGGER.info("theta: " + theta);
        
        for (int i = 1; i <= 1; i += 1) {
            // Profundis.LOGGER.info("rad: " + rad);

            rad += (
                0.1 * random.nextDouble() * Math.sin((2 * Math.PI * random.nextDouble()) + (theta * Math.ceil(0.5 / (random.nextDouble()))))
            );
        }

        double r = (1 + rad) * radius;
        return origin.distSqr(toPlaceAt) <= r * r;
    }

    double getAngle(double height, double base) {
        if (base == 0) {
            return height >= 0 ? Math.PI : Math.PI * 3;
        }

        return Math.atan(height / base);
    }
}
