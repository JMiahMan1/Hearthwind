package net.satisfy.nethervinery.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.vinery.core.block.GrapeBush;
import net.satisfy.vinery.core.util.GrapeType;

public class CrimsonGrapeBush extends GrapeBush {
    public CrimsonGrapeBush(Properties settings, GrapeType type) {
        super(settings, type);

    }

    @Override
    public boolean canGrowPlace(LevelReader world, BlockPos blockPos, BlockState blockState) {
        return world.getRawBrightness(blockPos, 0) >= 2;
    }

    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.is(Blocks.CRIMSON_NYLIUM);
    }
}
