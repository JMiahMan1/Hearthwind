package ht.treechop.common.config;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface TreeLeavesBehavior {
   TreeLeavesBehavior DEFAULT = state -> !(Boolean)ConfigHandler.COMMON.ignorePersistentLeaves.get()
      || !state.hasProperty(LeavesBlock.PERSISTENT)
      || !(Boolean)state.getValue(LeavesBlock.PERSISTENT);
   TreeLeavesBehavior PROBLEMATIC = state -> true;

   boolean isLeaves(BlockState var1);
}
