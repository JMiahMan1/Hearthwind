package ht.treechop.common.chop;

import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

public class ChopTreeResult implements ChopResult {
   private final Level level;
   private final Collection<Chop> chops;

   public ChopTreeResult(net.minecraft.world.level.Level level, Collection<Chop> chops) {
      this.level = level;
      this.chops = chops;
   }

   @Override
   public void apply(BlockPos targetPos, ServerPlayer agent, ItemStack tool) {
      GameType gameType = agent.gameMode.getGameModeForPlayer();
      if (!this.level.getBlockState(targetPos).isAir() && !agent.blockActionRestricted(this.level, targetPos, gameType)) {
         this.chops.forEach(chop -> chop.apply(this.level, agent, tool, false));
      }
   }
}
