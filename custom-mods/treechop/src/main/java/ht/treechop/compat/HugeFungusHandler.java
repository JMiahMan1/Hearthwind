package ht.treechop.compat;

import ht.treechop.api.ITreeBlock;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.api.TreeData;
import ht.treechop.api.TreeDetectorBuilder;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.Lazy;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class HugeFungusHandler implements ITreeBlock {
   private static ITreeBlock detectionHandler;
   private static final Lazy<Set<net.minecraft.world.level.block.Block>> logs = new Lazy<>(
      ConfigHandler.RELOAD,
      () -> ConfigHandler.getIdentifiedBlocks((List<? extends String>)HugeFungusHandler.MyConfigHandler.instance.logIds.get()).collect(Collectors.toSet())
   );
   private static final Lazy<Set<net.minecraft.world.level.block.Block>> leaves = new Lazy<>(
      ConfigHandler.RELOAD,
      () -> ConfigHandler.getIdentifiedBlocks((List<? extends String>)HugeFungusHandler.MyConfigHandler.instance.leavesIds.get()).collect(Collectors.toSet())
   );

   public static void register(TreeChopAPI api) {
      detectionHandler = new TreeDetectorBuilder().logs(HugeFungusHandler::isLog).leaves(HugeFungusHandler::isLeaves).maxLeavesDistance(6).build();
      HugeFungusHandler handler = new HugeFungusHandler();
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
      private static HugeFungusHandler.MyConfigHandler instance;
      protected final ConfigValue<List<? extends String>> logIds;
      protected final ConfigValue<List<? extends String>> leavesIds;

      public MyConfigHandler(Builder builder) {
         builder.push("hugeFungi");
         this.logIds = builder.defineListAllowEmpty("logs", List.of("#minecraft:crimson_stems", "#minecraft:warped_stems"), always -> true);
         this.leavesIds = builder.defineListAllowEmpty("leaves", List.of("#minecraft:wart_blocks", "minecraft:shroomlight"), always -> true);
         builder.pop();
      }

      public static void init(Builder builder) {
         instance = new HugeFungusHandler.MyConfigHandler(builder);
      }
   }
}
