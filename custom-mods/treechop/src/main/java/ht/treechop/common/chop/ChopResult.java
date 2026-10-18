package ht.treechop.common.chop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface ChopResult {
   ChopResult IGNORED = (targetPos, agent, tool) -> {};

   void apply(BlockPos var1, ServerPlayer var2, ItemStack var3);
}
