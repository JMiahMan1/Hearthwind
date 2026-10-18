package ht.treechop.common.chop;

import ht.treechop.TreeChop;
import ht.treechop.api.ILeaveslikeBlock;
import ht.treechop.api.TreeData;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.FellCreditStrategy;
import ht.treechop.common.config.FellLeavesStrategy;
import ht.treechop.common.util.ClassUtil;
import ht.treechop.common.util.LevelUtil;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class FellTreeResult implements ChopResult {
   private final Level level;
   private final FellDataImpl fellData;
   private final Collection<Chop> chops;

   public FellTreeResult(net.minecraft.world.level.Level level, TreeData tree, boolean breakLeaves, Collection<Chop> chops) {
      this.level = level;
      this.fellData = new FellDataImpl(tree, breakLeaves);
      this.chops = chops;
   }

   @Override
   public void apply(BlockPos targetPos, ServerPlayer player, ItemStack tool) {
      GameType gameType = player.gameMode.getGameModeForPlayer();
      if (this.level instanceof ServerLevel serverLevel
         && !serverLevel.getBlockState(targetPos).isAir()
         && !player.blockActionRestricted(serverLevel, targetPos, gameType)) {
         boolean fell = TreeChop.platform.startFellTreeEvent(player, this.level, targetPos, this.fellData);
         this.chops.forEach(chop -> chop.apply(this.level, player, tool, fell));
         if (fell) {
            Consumer<net.minecraft.core.BlockPos> blockBreaker = makeBlockBreaker(player, serverLevel);
            breakLogs(player, serverLevel, this.fellData.getTree(), gameType, blockBreaker, targetPos);
            if (this.fellData.getBreakLeaves()) {
               breakLeaves(player, serverLevel, this.fellData.getTree(), gameType, blockBreaker);
            }

            TreeChop.platform.finishFellTreeEvent(player, this.level, targetPos, this.fellData);
         }
      }
   }

   @NotNull
   private static Consumer<net.minecraft.core.BlockPos> makeBlockBreaker(
      net.minecraft.server.level.ServerPlayer player, net.minecraft.server.level.ServerLevel level
   ) {
      if (player.isCreative()) {
         BlockState air = Blocks.AIR.defaultBlockState();
         return pos -> level.setBlockAndUpdate(pos, air);
      } else {
         ServerPlayer creditPlayer = ConfigHandler.COMMON.fellCreditStrategy.get() == FellCreditStrategy.NONE ? null : player;
         ItemStack creditTool = ConfigHandler.COMMON.fellCreditStrategy.get() == FellCreditStrategy.PLAYER_AND_TOOL
            ? player.getMainHandItem()
            : ItemStack.EMPTY;
         return pos -> LevelUtil.harvestBlock(creditPlayer, level, pos, creditTool, false);
      }
   }

   private static void breakLogs(
      net.minecraft.server.level.ServerPlayer player,
      net.minecraft.server.level.ServerLevel level,
      TreeData tree,
      net.minecraft.world.level.GameType gameType,
      Consumer<net.minecraft.core.BlockPos> blockBreaker,
      net.minecraft.core.BlockPos targetPos
   ) {
      long maxNumEffects = 4L;
      AtomicInteger i = new AtomicInteger(0);
      PriorityQueue<Pair<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState>> effects = new PriorityQueue<>(
         Comparator.comparing(pair -> ((BlockPos)pair.getLeft()).getY())
      );
      tree.streamLogs().filter(pos -> !pos.equals(targetPos) && !player.blockActionRestricted(level, targetPos, gameType)).forEach(pos -> {
         collectSomeBlocks(effects, (net.minecraft.core.BlockPos)pos, level.getBlockState(pos), i, 3);
         blockBreaker.accept((net.minecraft.core.BlockPos)pos);
      });
      effects.stream().limit(4L).forEach(posState -> playBlockBreakEffects(level, (BlockPos)posState.getLeft(), (BlockState)posState.getRight()));
   }

   private static void breakLeaves(
      net.minecraft.server.level.ServerPlayer player,
      net.minecraft.server.level.ServerLevel level,
      TreeData tree,
      net.minecraft.world.level.GameType gameType,
      Consumer<net.minecraft.core.BlockPos> blockBreaker
   ) {
      long maxNumEffects = 5L;
      AtomicInteger i = new AtomicInteger(0);
      PriorityQueue<Pair<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState>> effects = new PriorityQueue<>(
         Comparator.comparing(pair -> ((BlockPos)pair.getLeft()).getY())
      );
      boolean tryToDecay = ConfigHandler.COMMON.fellLeavesStrategy.get() == FellLeavesStrategy.DECAY;
      Consumer<net.minecraft.core.BlockPos> leavesBreaker = pos -> {
         if (!player.blockActionRestricted(level, pos, gameType)) {
            BlockState state = level.getBlockState(pos);
            ILeaveslikeBlock leavesLike = ClassUtil.getLeaveslikeBlock(state.getBlock());
            if (leavesLike != null) {
               leavesLike.fell(player, level, pos, state);
            } else if (tryToDecay && shouldDecayLeaves(state)) {
               decayLeavesInsteadOfBreaking(level, pos, state);
            } else {
               blockBreaker.accept(pos);
            }

            if (effects.isEmpty() || player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 9.0) {
               collectSomeBlocks(effects, pos, state, i, 8);
            }
         }
      };
      tree.forEachLeaves(leavesBreaker);
      effects.stream().limit(5L).forEach(posState -> playBlockBreakEffects(level, (BlockPos)posState.getLeft(), (BlockState)posState.getRight()));
   }

   private static void collectSomeBlocks(
      Queue<Pair<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState>> collection,
      net.minecraft.core.BlockPos pos,
      net.minecraft.world.level.block.state.BlockState state,
      AtomicInteger counter,
      int period
   ) {
      if (counter.getAndIncrement() % period == 0) {
         collection.add(Pair.of(pos, state));
      }
   }

   private static void playBlockBreakEffects(Level level, BlockPos pos, BlockState state) {
      level.levelEvent(2001, pos, Block.getId(state));
   }

   private static void decayLeavesInsteadOfBreaking(ServerLevel level, BlockPos pos, BlockState state) {
      BlockState decayingState = (BlockState)((BlockState)state.setValue(LeavesBlock.PERSISTENT, false)).setValue(LeavesBlock.DISTANCE, 7);
      decayingState.randomTick(level, pos, level.getRandom());
   }

   private static boolean shouldDecayLeaves(BlockState blockState) {
      return blockState.hasProperty(LeavesBlock.DISTANCE)
         && blockState.hasProperty(LeavesBlock.PERSISTENT)
         && ((BlockState)((BlockState)blockState.setValue(LeavesBlock.DISTANCE, 7)).setValue(LeavesBlock.PERSISTENT, false)).isRandomlyTicking();
   }
}
