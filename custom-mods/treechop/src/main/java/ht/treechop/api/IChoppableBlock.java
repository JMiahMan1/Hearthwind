package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IChoppableBlock extends IFellableBlock {
   void chop(Player var1, ItemStack var2, Level var3, BlockPos var4, BlockState var5, int var6, boolean var7);

   int getNumChops(BlockGetter var1, BlockPos var2, BlockState var3);

   int getMaxNumChops(BlockGetter var1, BlockPos var2, BlockState var3);

   default boolean isChoppable(BlockGetter level, BlockPos blockPos, BlockState blockState) {
      return true;
   }

   @Override
   default double getSupportFactor(BlockGetter level, BlockPos pos, BlockState blockState) {
      return 1.0;
   }
}
