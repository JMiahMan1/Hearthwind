package firenh.profundis.features.features;

import com.mojang.serialization.Codec;

import firenh.profundis.features.features.config.IcicleFeatureConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class IcicleFeature extends Feature<IcicleFeatureConfig> {
    public IcicleFeature(Codec<IcicleFeatureConfig> configCodec) {
        super(configCodec);
    }

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    @Override
    public boolean place(FeaturePlaceContext<IcicleFeatureConfig> context) {
        BlockPos origin = context.origin();
        WorldGenLevel world = context.level();
        RandomSource random = context.random();

        IcicleFeatureConfig config = context.config();
        int minSize = config.size().minInclusive();
        int size = config.size().sample(random);
        int amount = config.amount().sample(random);
        int spread = config.spread().sample(random) / 2;
        BlockState innerState = config.innerState();
        BlockState outerState = config.outerState();

        spread = spread < 16 ? spread : 16;

        if (!world.isEmptyBlock(origin) || world.isEmptyBlock(origin.above())) {
            return false;
        }

        makeOneIcicle(world, origin, size, innerState, outerState, random);
        amount -= 1;

        while (amount > 0) {
            amount -= 1;
            BlockPos.MutableBlockPos mutable = origin.mutable();
            mutable = mutable.offset(random.nextInt(1 + spread * 2) - spread, 0, random.nextInt(1 + spread * 2) - spread).mutable();

            boolean bl = false;
            
            if (world.isEmptyBlock(mutable) && world.isEmptyBlock(mutable.above())) {
                for (int i = 0; i < 3 && !bl; i += 1) {
                    mutable = mutable.offset(0, 1, 0).mutable();

                    if (world.isEmptyBlock(mutable) && !world.isEmptyBlock(mutable.above()) && !world.isEmptyBlock(mutable.above(2))) {
                        bl = true;
                    }
                }
            } else if (!world.isEmptyBlock(mutable)) {
                for (int i = 0; i < 3 && !bl; i += 1) {
                    mutable = mutable.offset(0, -1, 0).mutable();

                    if (world.isEmptyBlock(mutable) && !world.isEmptyBlock(mutable.above()) && !world.isEmptyBlock(mutable.above(2))) {
                        bl = true;
                    }
                }
            }

            int nextSize = random.nextInt(size) + 1;
            nextSize = nextSize > minSize ? nextSize : minSize;

            if (!world.isEmptyBlock(mutable.above()) && !world.isEmptyBlock(mutable.above(2))) makeOneIcicle(world, mutable, nextSize, innerState, outerState, random);
        }
        
        return true;
    }

    public static void makeOneIcicle(WorldGenLevel world, BlockPos origin, int size, BlockState innerState, BlockState outerState, RandomSource random) {
        BlockPos.MutableBlockPos cursor = origin.mutable();

        for (int i = 0; i < -1 + (2 * size / 3); i += 1) {
            if (world.isEmptyBlock(cursor)) {
                world.setBlock(cursor, innerState, 0);
            } else {
                break;
            }

            cursor = cursor.below().mutable();
        }

        for (int i = 0; i < 1 + (size / 3); i += 1) {
            if (world.isEmptyBlock(cursor)) {
                world.setBlock(cursor, outerState, 0);
            } else {
                break;
            }

            cursor = cursor.below().mutable();
        }

        if (size > 3) {
            for (Direction d : DIRECTIONS) {
                if (world.isEmptyBlock(origin.relative(d)) && !world.isEmptyBlock(origin.relative(d).above())) makeOneIcicle(world, origin.relative(d), random.nextInt(2 + (size / 3)), innerState, outerState, random);
            }
        }
    }
    
}
