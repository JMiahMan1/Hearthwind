package firenh.profundis.features.features;

import com.mojang.serialization.Codec;

import firenh.profundis.features.features.config.CavePillarFeatureConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Holder;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class CavePillarFeature extends Feature<CavePillarFeatureConfig> {
    public CavePillarFeature(Codec<CavePillarFeatureConfig> configCodec) {
        super(configCodec);
    }

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    @Override
    public boolean place(FeaturePlaceContext<CavePillarFeatureConfig> context) {
        BlockPos origin = context.origin();
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        CavePillarFeatureConfig config = context.config();
        
        BlockState innerState = config.innerState();
        BlockState outerState = config.outerState();
        IntProvider lengthOfEnds = config.lengthOfEnds();
        Holder<PlacedFeature> bottomFeature = config.bottomFeature();

        if (!world.isEmptyBlock(origin) || world.isEmptyBlock(origin.above())) {
            return false;
        }

        BlockPos.MutableBlockPos cursor = origin.mutable();

        while (world.isEmptyBlock(cursor)) {
            world.setBlock(cursor, innerState, 0);
            cursor = cursor.below().mutable();
        }

        for (Direction d : DIRECTIONS) {
            addEnds(world, origin.relative(d), cursor.relative(d), lengthOfEnds, random, innerState, outerState);
        }

        bottomFeature.value().place(world, context.chunkGenerator(), random, cursor);

        return true;
    }

    private void addEnds(WorldGenLevel world, BlockPos top, BlockPos bottom, IntProvider length, RandomSource random, BlockState innerState, BlockState outerState) {
        int lengthTop = length.sample(random);
        BlockPos.MutableBlockPos cursor = top.mutable();

        if (world.isEmptyBlock(cursor.above())) for (int i = 0; i < 4; i += 1) {
            cursor = cursor.above().mutable();
            if (world.getBlockState(cursor.above()).canOcclude()) break;
        }

        for (int i = 0; i < lengthTop; i += 1) {
            BlockState place = i + 1 < 2 * lengthTop / 3 ? innerState : outerState;
            world.setBlock(cursor, place, 0);
            cursor = cursor.below().mutable();
        }

        int lengthBottom = length.sample(random);
        cursor = bottom.mutable();

        if (world.isEmptyBlock(cursor.below())) for (int i = 0; i < 4; i += 1) {
            cursor = cursor.below().mutable();
            if (world.getBlockState(cursor.below()).canOcclude()) break;
        }

        for (int i = 0; i < lengthBottom; i += 1) {
            BlockState place = i + 1 < 2 * lengthBottom / 3 ? innerState : outerState;
            world.setBlock(cursor, place, 0);
            cursor = cursor.above().mutable();
        }
    }
}
