package firenh.profundis.features.features;

import java.util.Iterator;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class FrozenWaterFeature extends Feature<NoneFeatureConfiguration> {
    
    // private static final Direction[] DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public FrozenWaterFeature(Codec<NoneFeatureConfiguration> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        BlockPos pos = context.origin();
        int checks = 12;

        while (world.isEmptyBlock(pos) && checks > 0) {
            pos = pos.below();
            checks -= 1;
        }

        if (world.isWaterAt(pos)) {
            setIce(world, pos);
            return true;
        }

        return false;
    }
    
    private void setIce(WorldGenLevel world, BlockPos pos) {
        if (world.isWaterAt(pos) && !world.isWaterAt(pos.above()) && !world.getBlockState(pos.above()).canOcclude()) {
            world.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);

            Iterator<BlockPos> iter = BlockPos.betweenClosed(pos.offset(-16, 0, -16), pos.offset(16, 0, 16)).iterator();

            while (iter.hasNext()) {
                BlockPos next = iter.next();

                if (pos.distSqr(next) <= (double)(16) * (double)(16) && world.isWaterAt(next) && !world.isWaterAt(next.above()) && !world.getBlockState(next.above()).canOcclude()) {
                    world.setBlock(next, Blocks.ICE.defaultBlockState(), 3);
                }
            }
        }
    } 
}
