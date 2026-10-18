package ht.treechop.common.util;

import ht.treechop.api.AbstractTreeData;
import ht.treechop.common.chop.Chop;
import ht.treechop.common.chop.ChopUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.world.level.Level;

@Deprecated
public class TreeDataImpl extends AbstractTreeData {
   private final Level level;
   private int chopsToFell;
   private boolean hasLeaves;
   private Set<net.minecraft.core.BlockPos> logBlocks = new HashSet<>();

   public TreeDataImpl(Level level, boolean overrideLeaves) {
      this.level = level;
      this.hasLeaves = overrideLeaves;
   }

   public TreeDataImpl(Level level) {
      this(level, false);
   }

   public static TreeDataImpl empty(Level level) {
      return new TreeDataImpl(level);
   }

   @Override
   public void setLogBlocks(Set<net.minecraft.core.BlockPos> logBlocks) {
      this.logBlocks = logBlocks;
      this.chopsToFell = ChopUtil.numChopsToFell(this.level, logBlocks.stream());
   }

   @Override
   public int getChops() {
      return 0;
   }

   @Override
   public Stream<net.minecraft.core.BlockPos> streamLogs() {
      return this.logBlocks.stream();
   }

   @Override
   public Stream<net.minecraft.core.BlockPos> streamLeaves() {
      return Stream.empty();
   }

   @Override
   public boolean hasLeaves() {
      return this.hasLeaves;
   }

   @Override
   public boolean readyToFell(int numChops) {
      return ChopUtil.enoughChopsToFell(numChops, this.chopsToFell);
   }

   @Override
   public int numChopsNeededToFell() {
      return this.chopsToFell;
   }

   @Override
   public Collection<Chop> chop(net.minecraft.core.BlockPos target, int numChops) {
      return Collections.emptyList();
   }

   @Override
   public void setLeaves(boolean hasLeaves) {
      this.hasLeaves = hasLeaves;
   }
}
