package ht.treechop.common.util;

import ht.treechop.TreeChop;
import ht.treechop.api.IChoppableBlock;
import ht.treechop.api.IChoppingItem;
import ht.treechop.api.ICylinderBlock;
import ht.treechop.api.IFellableBlock;
import ht.treechop.api.ILeaveslikeBlock;
import ht.treechop.api.IStrippableBlock;
import ht.treechop.api.IThwackableBlock;
import ht.treechop.api.ITreeBlock;
import ht.treechop.common.config.ConfigHandler;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClassUtil {
   @Nullable
   public static IChoppableBlock getChoppableBlock(BlockGetter level, BlockPos blockPos, BlockState blockState) {
      IChoppableBlock choppableBlock = getChoppableBlockUnchecked(blockState.getBlock());
      return choppableBlock != null && choppableBlock.isChoppable(level, blockPos, blockState) ? choppableBlock : null;
   }

   @Nullable
   public static IChoppableBlock getChoppableBlockUnchecked(Block block) {
      if (block instanceof IChoppableBlock choppableBlock) {
         return choppableBlock;
      } else if (TreeChop.api.getRegisteredChoppableBlockBehavior(block) instanceof IChoppableBlock choppableBlock) {
         return choppableBlock;
      } else {
         return ConfigHandler.COMMON.choppableBlocks.get().contains(block) ? (IChoppableBlock)TreeChop.platform.getChoppedLogBlock() : null;
      }
   }

   @Nullable
   public static IFellableBlock getFellableBlock(Block block) {
      if (block instanceof IFellableBlock fellableBlock) {
         return fellableBlock;
      } else {
         return TreeChop.api.getRegisteredChoppableBlockBehavior(block) instanceof IFellableBlock fellableBlock ? fellableBlock : null;
      }
   }

   @Nullable
   public static ICylinderBlock getCylinderBlock(Block block) {
      if (block instanceof ICylinderBlock cylinderBlock) {
         return cylinderBlock;
      } else {
         return TreeChop.api.getRegisteredChoppableBlockBehavior(block) instanceof ICylinderBlock cylinderBlock ? cylinderBlock : null;
      }
   }

   @Nullable
   public static IStrippableBlock getStrippableBlock(Block block) {
      if (block instanceof IStrippableBlock strippableBlock) {
         return strippableBlock;
      } else {
         return TreeChop.api.getRegisteredChoppableBlockBehavior(block) instanceof IStrippableBlock strippableBlock ? strippableBlock : null;
      }
   }

   @Nullable
   public static IThwackableBlock getThwackableBlock(Block block) {
      if (block instanceof IThwackableBlock thwackableBlock) {
         return thwackableBlock;
      } else {
         return TreeChop.api.getRegisteredChoppableBlockBehavior(block) instanceof IThwackableBlock thwackableBlock ? thwackableBlock : null;
      }
   }

   @Nullable
   public static ITreeBlock getTreeBlock(Block block) {
      if (block instanceof ITreeBlock treeBlock) {
         return treeBlock;
      } else {
         return TreeChop.api.getRegisteredChoppableBlockBehavior(block) instanceof ITreeBlock treeBlock ? treeBlock : null;
      }
   }

   @Nullable
   public static ILeaveslikeBlock getLeaveslikeBlock(Block block) {
      if (block instanceof ILeaveslikeBlock leaveslikeBlock) {
         return leaveslikeBlock;
      } else {
         return TreeChop.api.getRegisteredBlockBehavior(block) instanceof ILeaveslikeBlock leaveslikeBlock ? leaveslikeBlock : null;
      }
   }

   public static IChoppingItem getChoppingItem(Item item) {
      return item instanceof IChoppingItem choppingItem ? choppingItem : TreeChop.api.getRegisteredChoppingItemBehavior(item);
   }
}
