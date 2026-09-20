package net.dungeonz.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PropertyUtil {

    public static int getHorizontalFacing(BlockState blockState) {
        if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).get2DDataValue();
        }
        return 0;
    }

    public static int getBlockFacing(BlockState blockState) {
        if (blockState.hasProperty(BlockStateProperties.ATTACH_FACE)) {
            return switch (blockState.getValue(BlockStateProperties.ATTACH_FACE)) {
                case FLOOR -> 1;
                case WALL -> 2;
                case CEILING -> 3;
                default -> 0;
            };
        }
        return 0;
    }

    public static AttachFace getBlockFacing(int blockFacing) {
        return switch (blockFacing) {
            case 1 -> AttachFace.FLOOR;
            case 2 -> AttachFace.WALL;
            case 3 -> AttachFace.CEILING;
            default -> AttachFace.FLOOR;
        };
    }
}
