package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ILeaveslikeBlock extends ITreeChopBlockBehavior {
   void fell(Player var1, Level var2, BlockPos var3, BlockState var4);
}
