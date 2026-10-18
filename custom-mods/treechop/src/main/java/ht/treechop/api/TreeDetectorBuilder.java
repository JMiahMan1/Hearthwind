package ht.treechop.api;

import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.chop.LazyTreeData;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.BlockNeighbors;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class TreeDetectorBuilder {
   private BlockMatcher logMatcher = ChopUtil::isBlockALog;
   private BlockMatcher leavesMatcher = ChopUtil::isBlockLeaves;
   private BiFunction<net.minecraft.world.level.Level, net.minecraft.core.BlockPos, Stream<net.minecraft.core.BlockPos>> treeScanner = (level, pos) -> BlockNeighbors.HORIZONTAL_AND_ABOVE
      .asStream(pos);
   private BiFunction<net.minecraft.world.level.Level, net.minecraft.core.BlockPos, Stream<net.minecraft.core.BlockPos>> leavesScanner = (level, pos) -> BlockNeighbors.ADJACENTS
      .asStream(pos);
   private int maxLogs;
   private int maxLeavesDistance;
   private TreeDetectorBuilder.LeavesStrategy leavesStrategy = TreeDetectorBuilder.LeavesStrategy.SMART;

   public TreeDetectorBuilder() {
      this.maxLogs = (Integer)ConfigHandler.COMMON.maxNumTreeBlocks.get();
      this.maxLeavesDistance = (Integer)ConfigHandler.COMMON.maxBreakLeavesDistance.get();
   }

   public TreeDetectorBuilder logs(BlockMatcher logMatcher) {
      this.logMatcher = logMatcher;
      return this;
   }

   public TreeDetectorBuilder leaves(BlockMatcher leavesMatcher) {
      this.leavesMatcher = leavesMatcher;
      return this;
   }

   public TreeDetectorBuilder treeScanner(
      BiFunction<net.minecraft.world.level.Level, net.minecraft.core.BlockPos, Stream<net.minecraft.core.BlockPos>> scanner
   ) {
      this.treeScanner = scanner;
      return this;
   }

   public TreeDetectorBuilder leavesScanner(
      BiFunction<net.minecraft.world.level.Level, net.minecraft.core.BlockPos, Stream<net.minecraft.core.BlockPos>> scanner
   ) {
      this.leavesScanner = scanner;
      return this;
   }

   public TreeDetectorBuilder maxLogCount(int maxLogs) {
      this.maxLogs = maxLogs;
      return this;
   }

   public TreeDetectorBuilder maxLeavesDistance(int maxLeavesDistance) {
      this.maxLeavesDistance = maxLeavesDistance;
      return this;
   }

   public TreeDetectorBuilder leavesStrategy(TreeDetectorBuilder.LeavesStrategy strategy) {
      this.leavesStrategy = strategy;
      return this;
   }

   public ITreeBlock build() {
      return (level, origin) -> new LazyTreeData(
         level,
         origin,
         pos -> this.treeScanner.apply(level, pos),
         pos -> this.leavesScanner.apply(level, pos),
         pos -> this.logMatcher.matches(level, pos, ChopUtil.getLogState(level, pos)),
         pos -> this.leavesMatcher.matches(level, pos, level.getBlockState(pos)),
         this.maxLogs,
         this.maxLeavesDistance,
         this.leavesStrategy == TreeDetectorBuilder.LeavesStrategy.SMART
      );
   }

   public static enum LeavesStrategy {
      SMART,
      SHORTEST_PATH;
   }
}
