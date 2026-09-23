package firenh.profundis.features.features;

import com.mojang.serialization.Codec;

import firenh.profundis.Profundis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class LavaFixerFeature extends Feature<NoneFeatureConfiguration> {
    public LavaFixerFeature(Codec<NoneFeatureConfiguration> configCodec) {
        super(configCodec);
    }

    private Direction[] directionsToCheck = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel world = context.level();
        final int chunkSnappedX = chunkSnap(origin.getX());
        final int chunkSnappedZ = chunkSnap(origin.getZ());
        
        BlockPos cursor = new BlockPos(chunkSnappedX, 120, chunkSnappedZ);

        // Profundis.LOGGER.info("lava fixer is doing stuff pt 1");

        while (cursor.getY() >= 0) {
            // Profundis.LOGGER.info("lava fixer is doing stuff pt 2");

            if (world.getBlockState(cursor).is(Blocks.LAVA)) {
                boolean fixed = false;
                BlockState setState = cursor.getY() > 0 ? Blocks.STONE.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();

                for (Direction d : directionsToCheck) {
                    BlockPos checkingCursor = cursor.relative(d);
                    if (!world.isEmptyBlock(checkingCursor) 
                            && !(world.getBlockState(checkingCursor).is(Blocks.LAVA))
                            && !(world.getBlockState(checkingCursor).blocksMotion())
                            && !(world.getBlockState(checkingCursor).canOcclude())
                        ) {
                        // Profundis.LOGGER.info("fixed lava at " + cursor.getX() + "x, " + cursor.getY() + "y, " + cursor.getZ() + "z");
                        world.setBlock(cursor, setState, 0);
                        fixed = true;
                        break;
                    }
                }

                if (fixed == false && world.isEmptyBlock(cursor.below())) {
                    Profundis.LOGGER.info("fixed lava at " + cursor.getX() + "x, " + cursor.getY() + "y, " + cursor.getZ() + "z");
                    world.setBlock(cursor, setState, 0);
                    fixed = true;
                }
            }

            if (cursor.getX() == chunkSnappedX + 15 && cursor.getZ() == chunkSnappedZ + 15) {
                cursor = new BlockPos(chunkSnappedX, cursor.getY() - 1, chunkSnappedZ);
            } else if (cursor.getX() == chunkSnappedX + 15) {
                cursor = new BlockPos(chunkSnappedX, cursor.getY(), cursor.getZ() + 1);
            } else {
                cursor = new BlockPos(cursor.getX() + 1, cursor.getY(), cursor.getZ());
            }
        }

        return true;
    }
    
    private int chunkSnap(int input) {
        return (input / 16) * 16;
    }
}
