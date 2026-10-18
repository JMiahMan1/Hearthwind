package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IChoppingItem {
   boolean canChop(Player var1, ItemStack var2, Level var3, BlockPos var4, BlockState var5);

   int getNumChops(ItemStack var1, BlockState var2);
}
