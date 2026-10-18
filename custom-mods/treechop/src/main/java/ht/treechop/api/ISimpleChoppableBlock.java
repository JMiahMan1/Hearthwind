package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ISimpleChoppableBlock extends IChoppableBlock, ICylinderBlock {
   @Override
   default int getRadius(BlockGetter level, BlockPos blockPos, BlockState blockState) {
      return 8;
   }

   @Override
   default void chop(Player player, ItemStack tool, Level level, BlockPos pos, BlockState blockState, int numChops, boolean felling) {
      Identifier CHOPPED_LOG = Identifier.fromNamespaceAndPath("treechop", "chopped_log");
      Block choppedLog = (Block)BuiltInRegistries.BLOCK.getValue(CHOPPED_LOG);
      if (choppedLog instanceof IChoppableBlock choppableBlock) {
         choppableBlock.chop(player, tool, level, pos, blockState, numChops, felling);
      }
   }

   @Override
   default int getNumChops(BlockGetter level, BlockPos pos, BlockState blockState) {
      return 0;
   }

   @Override
   default int getMaxNumChops(BlockGetter level, BlockPos pos, BlockState blockState) {
      return this.getRadius(level, pos, blockState) - 1;
   }

   @Override
   default double getSupportFactor(BlockGetter level, BlockPos pos, BlockState blockState) {
      return this.getRadius(level, pos, blockState) / 8.0;
   }
}
