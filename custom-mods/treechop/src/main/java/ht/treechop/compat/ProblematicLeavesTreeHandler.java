package ht.treechop.compat;

import ht.treechop.api.ITreeBlock;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.api.TreeData;
import ht.treechop.api.TreeDetectorBuilder;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.Lazy;
import ht.treechop.common.util.BlockNeighbors;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class ProblematicLeavesTreeHandler implements ITreeBlock {
   private static ITreeBlock detectionHandler;
   private static final Lazy<Set<net.minecraft.world.level.block.Block>> logs = new Lazy<>(
      ConfigHandler.RELOAD,
      () -> ConfigHandler.getIdentifiedBlocks((List<? extends String>)ProblematicLeavesTreeHandler.MyConfigHandler.instance.logIds.get())
         .collect(Collectors.toSet())
   );
   private static final Lazy<Set<net.minecraft.world.level.block.Block>> leaves = new Lazy<>(
      ConfigHandler.RELOAD,
      () -> ConfigHandler.getIdentifiedBlocks((List<? extends String>)ProblematicLeavesTreeHandler.MyConfigHandler.instance.leavesIds.get())
         .collect(Collectors.toSet())
   );

   public static void register(TreeChopAPI api) {
      detectionHandler = new TreeDetectorBuilder()
         .logs(ProblematicLeavesTreeHandler::isLog)
         .leaves(ProblematicLeavesTreeHandler::isLeaves)
         .maxLeavesDistance(7)
         .leavesScanner((level, pos) -> BlockNeighbors.ADJACENTS_AND_DIAGONALS.asStream(pos))
         .leavesStrategy(TreeDetectorBuilder.LeavesStrategy.SHORTEST_PATH)
         .build();
      ProblematicLeavesTreeHandler handler = new ProblematicLeavesTreeHandler();
      logs.get().forEach(block -> {
         api.overrideChoppableBlock(block, true);
         api.registerBlockBehavior(block, handler);
      });
      leaves.get().forEach(block -> api.overrideLeavesBlock(block, true));
   }

   @Override
   public TreeData getTree(Level level, BlockPos origin) {
      return detectionHandler.getTree(level, origin);
   }

   public static boolean isLog(Level level, BlockPos pos, BlockState state) {
      return logs.get().contains(state.getBlock());
   }

   public static boolean isLeaves(Level level, BlockPos pos, BlockState state) {
      return leaves.get().contains(state.getBlock());
   }

   public static class MyConfigHandler {
      private static ProblematicLeavesTreeHandler.MyConfigHandler instance;
      protected final ConfigValue<List<? extends String>> logIds;
      protected final ConfigValue<List<? extends String>> leavesIds;

      public MyConfigHandler(Builder builder) {
         builder.push("problematicLeavesTrees");
         this.logIds = builder.defineListAllowEmpty(
            "logs",
            List.of(
               "tropicraft:.*_log(_.*)?", "mysticbiomes:.*_log", "betternether:.*_bark", "betternether:.*_log", "alexscaves:.*_log", "alexscaves:pewen_wood"
            ),
            always -> true
         );
         this.leavesIds = builder.defineListAllowEmpty(
            "leaves",
            List.of(
               "tropicraft:.*_leaves(_.*)?", "betternether:.*_leaves", "regions_unexplored:brimwood_leaves", "alexscaves:.*_branch", "alexscaves:pewen_pines"
            ),
            always -> true
         );
         builder.pop();
      }

      public static void init(Builder builder) {
         instance = new ProblematicLeavesTreeHandler.MyConfigHandler(builder);
      }
   }
}
