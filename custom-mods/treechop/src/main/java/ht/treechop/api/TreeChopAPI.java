package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface TreeChopAPI {
   void overrideChoppableBlock(Block var1, boolean var2);

   void overrideLeavesBlock(Block var1, boolean var2);

   void overrideChoppingItem(Item var1, boolean var2);

   void registerBlockBehavior(Block var1, ITreeChopBlockBehavior var2);

   boolean deregisterBlockBehavior(Block var1);

   ITreeChopBlockBehavior getRegisteredBlockBehavior(Block var1);

   void registerChoppingItemBehavior(Item var1, IChoppingItem var2);

   boolean deregisterChoppingItemBehavior(Item var1);

   IChoppingItem getRegisteredChoppingItemBehavior(Item var1);

   boolean isBlockLeaves(Level var1, BlockPos var2, BlockState var3);

   boolean isBlockChoppable(Level var1, BlockPos var2, BlockState var3);

   boolean canChopWithItem(Player var1, ItemStack var2, Level var3, BlockPos var4, BlockState var5);

   TreeData getTree(Level var1, BlockPos var2);

   @Deprecated
   void registerChoppableBlockBehavior(Block var1, ITreeChopBlockBehavior var2);

   @Deprecated
   boolean deregisterChoppableBlockBehavior(Block var1);

   @Deprecated
   ITreeChopBlockBehavior getRegisteredChoppableBlockBehavior(Block var1);
}
