package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface IFellableBlock extends ITreeChopBlockBehavior {
   double getSupportFactor(BlockGetter var1, BlockPos var2, BlockState var3);
}
