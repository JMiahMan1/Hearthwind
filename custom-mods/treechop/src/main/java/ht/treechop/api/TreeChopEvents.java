package ht.treechop.api;

import ht.treechop.common.util.TreeDataImpl;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class TreeChopEvents {
   public static final Event<TreeChopEvents.BeforeChop> BEFORE_CHOP = EventFactory.createArrayBacked(
      TreeChopEvents.BeforeChop.class, listeners -> (world, player, pos, state, chopData) -> {
         for (TreeChopEvents.BeforeChop listener : listeners) {
            if (!listener.beforeChop(world, player, pos, state, chopData)) {
               return false;
            }
         }

         return true;
      }
   );
   public static final Event<TreeChopEvents.AfterChop> AFTER_CHOP = EventFactory.createArrayBacked(
      TreeChopEvents.AfterChop.class, listeners -> (world, player, pos, state, chopData, felled) -> {
         for (TreeChopEvents.AfterChop event : listeners) {
            event.afterChop(world, player, pos, state, chopData, felled);
         }
      }
   );
   public static final Event<TreeChopEvents.BeforeFell> BEFORE_FELL = EventFactory.createArrayBacked(
      TreeChopEvents.BeforeFell.class, listeners -> (world, player, pos, fellData) -> {
         for (TreeChopEvents.BeforeFell event : listeners) {
            if (!event.beforeFell(world, player, pos, fellData)) {
               return false;
            }
         }

         return true;
      }
   );
   public static final Event<TreeChopEvents.AfterFell> AFTER_FELL = EventFactory.createArrayBacked(
      TreeChopEvents.AfterFell.class, listeners -> (world, player, pos, fellData) -> {
         for (TreeChopEvents.AfterFell event : listeners) {
            if (!event.afterFell(world, player, pos, fellData)) {
               return false;
            }
         }

         return true;
      }
   );
   public static final Event<TreeChopEvents.DetectTree> DETECT_TREE = EventFactory.createArrayBacked(
      TreeChopEvents.DetectTree.class, listeners -> (world, player, pos, state, treeData) -> {
         for (TreeChopEvents.DetectTree listener : listeners) {
            treeData = listener.onDetectTree(world, player, pos, state, treeData);
            if (treeData == null) {
               return TreeDataImpl.empty(world);
            }
         }

         return treeData;
      }
   );

   private TreeChopEvents() {
   }

   @FunctionalInterface
   public interface AfterChop {
      void afterChop(Level var1, Player var2, BlockPos var3, BlockState var4, ChopDataImmutable var5, boolean var6);
   }

   @FunctionalInterface
   public interface AfterFell {
      boolean afterFell(Level var1, ServerPlayer var2, BlockPos var3, FellData var4);
   }

   @FunctionalInterface
   public interface BeforeChop {
      boolean beforeChop(Level var1, Player var2, BlockPos var3, BlockState var4, ChopData var5);
   }

   @FunctionalInterface
   public interface BeforeFell {
      boolean beforeFell(Level var1, ServerPlayer var2, BlockPos var3, FellData var4);
   }

   @FunctionalInterface
   public interface DetectTree {
      TreeData onDetectTree(Level var1, ServerPlayer var2, BlockPos var3, BlockState var4, TreeData var5);
   }
}
