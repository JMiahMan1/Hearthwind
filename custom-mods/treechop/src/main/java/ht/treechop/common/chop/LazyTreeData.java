package ht.treechop.common.chop;

import ht.treechop.api.AbstractTreeData;
import ht.treechop.api.IChoppableBlock;
import ht.treechop.common.util.BlockNeighbors;
import ht.treechop.common.util.ClassUtil;
import ht.tuber.graph.DirectedGraph;
import ht.tuber.graph.FloodFill;
import ht.tuber.graph.FloodFillImpl;
import ht.tuber.graph.GraphUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.RandomUtils;

public class LazyTreeData extends AbstractTreeData {
   private final Level level;
   private final int chops;
   private final boolean smartDetection;
   private final int maxLeavesDistance;
   private double mass = 0.0;
   private boolean overrideLeaves = false;
   private Set<net.minecraft.core.BlockPos> logs = new HashSet<net.minecraft.core.BlockPos>() {
      public boolean add(BlockPos blockPos) {
         if (super.add(blockPos)) {
            LazyTreeData.this.mass = LazyTreeData.this.mass + ChopUtil.getSupportFactor(LazyTreeData.this.level, blockPos);
            return true;
         } else {
            return false;
         }
      }
   };
   private final Set<net.minecraft.core.BlockPos> leaves = new HashSet<net.minecraft.core.BlockPos>() {
      public boolean add(BlockPos blockPos) {
         return super.add(blockPos);
      }
   };
   private LazyTreeData.LogFinder logFinder;
   private Set<net.minecraft.core.BlockPos> base;
   private final DirectedGraph<net.minecraft.core.BlockPos> logsWorld;
   private final DirectedGraph<net.minecraft.core.BlockPos> leavesWorld;

   public LazyTreeData(
      net.minecraft.world.level.Level level,
      net.minecraft.core.BlockPos origin,
      DirectedGraph<net.minecraft.core.BlockPos> logGraph,
      DirectedGraph<net.minecraft.core.BlockPos> leavesGraph,
      Predicate<net.minecraft.core.BlockPos> logFilter,
      Predicate<net.minecraft.core.BlockPos> leavesFilter,
      int maxNumLogs,
      int maxLeavesDistance,
      boolean smartDetection
   ) {
      this.level = level;
      this.smartDetection = smartDetection;
      this.maxLeavesDistance = maxLeavesDistance;
      this.logsWorld = GraphUtil.filter(logGraph, this::gatherLog, pos -> this.check(pos, logFilter, leavesFilter));
      this.leavesWorld = GraphUtil.filterNeighbors(leavesGraph, leavesFilter);
      this.makeTreeBase(level, origin);
      this.logs.addAll(this.base);
      this.chops = this.base.stream().map(pos -> ChopUtil.getNumChops(level, pos)).reduce(Integer::sum).orElse(0);
      this.logFinder = new LazyTreeData.LogFinder(this.logsWorld, this.base, maxNumLogs);
   }

   private boolean gatherLog(BlockPos pos) {
      this.logs.add(pos);
      return true;
   }

   private boolean check(
      net.minecraft.core.BlockPos pos, Predicate<net.minecraft.core.BlockPos> logFilter, Predicate<net.minecraft.core.BlockPos> leavesFilter
   ) {
      if (leavesFilter.test(pos)) {
         this.leaves.add(pos);
      }

      return logFilter.test(pos);
   }

   @Override
   public boolean hasLeaves() {
      return !this.overrideLeaves && this.leaves.isEmpty() ? this.logFinder.find().anyMatch(p -> !this.leaves.isEmpty() || this.overrideLeaves) : true;
   }

   @Override
   public void setLogBlocks(Set<net.minecraft.core.BlockPos> logBlocks) {
      this.logs = logBlocks;
      this.mass = ChopUtil.getSupportFactor(this.level, this.logs.stream()).orElse(1.0);
      this.logFinder = new LazyTreeData.LogFinder(a -> Stream.empty(), Collections.emptySet(), 0);
   }

   @Override
   public void setLeaves(boolean hasLeaves) {
      this.overrideLeaves = hasLeaves;
   }

   @Override
   public Stream<net.minecraft.core.BlockPos> streamLogs() {
      return Stream.concat(this.logs.stream(), this.logFinder.find());
   }

   @Override
   public Stream<net.minecraft.core.BlockPos> streamLeaves() {
      List<net.minecraft.core.BlockPos> allLeaves = new LinkedList<>();
      this.forEachLeaves(this.leaves, allLeaves::add);
      return allLeaves.stream();
   }

   private void completeTree() {
      this.logFinder.find().count();
   }

   @Override
   public void forEachLeaves(Consumer<net.minecraft.core.BlockPos> consumer) {
      this.forEachLeaves(this.leaves, consumer);
   }

   private void forEachLeaves(Collection<net.minecraft.core.BlockPos> firstLeaves, Consumer<net.minecraft.core.BlockPos> forEach) {
      this.completeTree();
      if (this.smartDetection) {
         this.forEachLeavesSmart(firstLeaves, forEach);
      } else {
         this.forEachLeavesDumb(firstLeaves, forEach);
      }
   }

   private void forEachLeavesSmart(Collection<net.minecraft.core.BlockPos> firstLeaves, Consumer<net.minecraft.core.BlockPos> forEach) {
      this.leaves.stream().filter(pos -> this.leavesHasExactDistance(this.level.getBlockState(pos), 1)).forEach(forEach);
      AtomicInteger highestDistance = new AtomicInteger(this.maxLeavesDistance);
      AtomicInteger distance = new AtomicInteger();
      DirectedGraph<net.minecraft.core.BlockPos> distancedLeavesGraph = GraphUtil.filterNeighbors(this.leavesWorld, pos -> {
         BlockState state = this.level.getBlockState(pos);
         state.getOptionalValue(LeavesBlock.DISTANCE).ifPresent(d -> {
            if (d > highestDistance.get()) {
               highestDistance.set(d);
            }
         });
         return this.leavesHasAtLeastDistance(state, distance.get());
      });
      FloodFillImpl<net.minecraft.core.BlockPos> flood = new FloodFillImpl<>(firstLeaves, distancedLeavesGraph, a -> 0);

      for (int i = 2; i <= highestDistance.get(); i++) {
         distance.set(i);
         flood.fillOnce(forEach);
      }
   }

   private void forEachLeavesDumb(Collection<net.minecraft.core.BlockPos> firstLeaves, Consumer<net.minecraft.core.BlockPos> forEach) {
      FloodFillImpl<net.minecraft.core.BlockPos> flood = new FloodFillImpl<>(firstLeaves, this.leavesWorld, a -> 0);

      for (int i = 0; i < this.maxLeavesDistance; i++) {
         flood.fillOnce(forEach);
      }
   }

   @Override
   public boolean readyToFell(int numChops) {
      return !ChopUtil.enoughChopsToFell(numChops, this.mass)
         ? false
         : this.logFinder.find().allMatch(ignored -> ChopUtil.enoughChopsToFell(numChops, this.mass));
   }

   @Override
   public int numChopsNeededToFell() {
      this.completeTree();
      return ChopUtil.numChopsToFell(this.mass);
   }

   @Override
   public int getChops() {
      return this.chops;
   }

   public Level getLevel() {
      return this.level;
   }

   private boolean leavesHasExactDistance(BlockState state, int distance) {
      return state.getOptionalValue(LeavesBlock.DISTANCE).orElse(distance) == distance || state.getOptionalValue(LeavesBlock.PERSISTENT).orElse(false);
   }

   private boolean leavesHasAtLeastDistance(BlockState state, int distance) {
      return state.getOptionalValue(LeavesBlock.DISTANCE).orElse(distance) >= distance || state.getOptionalValue(LeavesBlock.PERSISTENT).orElse(false);
   }

   private void makeTreeBase(Level level, BlockPos origin) {
      this.base = new HashSet<>();
      if (ChopUtil.isBlockChoppable(level, origin)) {
         DirectedGraph<net.minecraft.core.BlockPos> adjacentWorld = BlockNeighbors.ADJACENTS_AND_DIAGONALS::asStream;
         this.base.add(origin);
         GraphUtil.flood(GraphUtil.filterNeighbors(adjacentWorld, pos -> ChopUtil.getNumChops(level, pos) > 0), origin, Vec3i::getY)
            .fill()
            .forEach(this.base::add);
      }
   }

   @Override
   public Collection<Chop> chop(net.minecraft.core.BlockPos target, int numChops) {
      List<Chop> chops = new Stack<>();
      AtomicInteger chopsLeft = new AtomicInteger(numChops);
      if (chopsLeft.get() > 0) {
         GraphUtil.flood(this.logsWorld, this.base, a -> ChopUtil.blockDistance(target, a) * 32 + RandomUtils.nextInt(0, 32)).fill().takeWhile(pos -> {
            chopsLeft.set(gatherChopAndGetNumChopsRemaining(this.level, (net.minecraft.core.BlockPos)pos, chopsLeft.get(), chops));
            return chopsLeft.get() > 0;
         }).count();
      }

      return chops;
   }

   private static int gatherChopAndGetNumChopsRemaining(
      net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, int numChops, List<Chop> chops
   ) {
      BlockState blockStateBeforeChopping = level.getBlockState(pos);
      if (!(blockStateBeforeChopping.getBlock() instanceof IChoppableBlock) && isBlockSurrounded(level, pos)) {
         return numChops;
      } else {
         int adjustedNumChops = adjustNumChops(level, pos, blockStateBeforeChopping, numChops);
         if (adjustedNumChops > 0) {
            chops.add(new Chop(pos, adjustedNumChops));
         }

         return numChops - adjustedNumChops;
      }
   }

   private static int adjustNumChops(Level level, BlockPos blockPos, BlockState blockState, int numChops) {
      IChoppableBlock choppableBlock = ClassUtil.getChoppableBlock(level, blockPos, blockState);
      if (choppableBlock != null) {
         int currentNumChops = choppableBlock.getNumChops(level, blockPos, blockState);
         int maxNondestructiveChops = choppableBlock.getMaxNumChops(level, blockPos, blockState) - currentNumChops;
         return Math.min(maxNondestructiveChops, numChops);
      } else {
         return 0;
      }
   }

   private static boolean isBlockSurrounded(Level level, BlockPos pos) {
      return Stream.of(pos.west(), pos.north(), pos.east(), pos.south())
         .allMatch(neighborPos -> ChopUtil.isBlockALog(level, neighborPos));
   }

   private static class LogFinder {
      private int size = 0;
      private final int maxSize;
      FloodFill<net.minecraft.core.BlockPos> flood;

      public LogFinder(DirectedGraph<net.minecraft.core.BlockPos> logsWorld, Set<net.minecraft.core.BlockPos> base, int maxSize) {
         this.flood = GraphUtil.flood(logsWorld, base, v -> -v.getY());
         this.maxSize = maxSize;
      }

      public Stream<net.minecraft.core.BlockPos> find() {
         int n = this.size;
         return this.flood.fill().peek(a -> this.size++).limit(this.maxSize - n);
      }
   }
}
