package ht.treechop.api;

import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.FellLeavesStrategy;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractTreeData implements TreeData {
   @Override
   public boolean isAProperTree(boolean mustHaveLeaves) {
      boolean canBeLil = this.hasLeaves() && ConfigHandler.COMMON.fellLeavesStrategy.get() != FellLeavesStrategy.IGNORE;
      long lowerboundSize = this.streamLogs().limit(2L).count();
      return (this.hasLeaves() || !mustHaveLeaves) && lowerboundSize >= (canBeLil ? 1 : 2);
   }

   @Override
   public Optional<Set<net.minecraft.core.BlockPos>> getLogBlocks() {
      return Optional.of(this.getLogBlocksOrEmpty());
   }

   @Override
   public Set<net.minecraft.core.BlockPos> getLogBlocksOrEmpty() {
      return this.streamLogs().collect(Collectors.toSet());
   }

   @Override
   public void forEachLeaves(Consumer<net.minecraft.core.BlockPos> consumer) {
      this.streamLeaves().forEach(consumer);
   }
}
