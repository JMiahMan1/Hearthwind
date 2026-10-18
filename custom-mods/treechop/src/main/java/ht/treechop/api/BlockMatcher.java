package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BlockMatcher {
   boolean matches(Level var1, BlockPos var2, BlockState var3);
}
