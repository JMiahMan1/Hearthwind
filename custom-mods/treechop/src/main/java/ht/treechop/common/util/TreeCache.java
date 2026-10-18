package ht.treechop.common.util;

import ht.treechop.api.TreeData;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TreeCache {
   private final TreeCache.SingleBlockCache<TreeData> singleBlockCache = new TreeCache.SingleBlockCache<>();

   public void invalidate() {
      this.singleBlockCache.invalidate();
   }

   public TreeData getTree(Level level, BlockPos pos) {
      TreeData tree = this.singleBlockCache.get(level, pos);
      if (tree == null) {
         tree = ChopUtil.getTree(level, pos);
         this.singleBlockCache.put(level, pos, tree);
      }

      return tree;
   }

   private static class SingleBlockCache<T> {
      private T value;
      private BlockGetter level;
      private BlockState blockState;
      private BlockPos pos;
      private BlockEntity entity;
      private int numChops;

      public T get(net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
         return level == this.level
               && pos.equals(this.pos)
               && Objects.equals(level.getBlockState(pos), this.blockState)
               && Objects.equals(level.getBlockEntity(pos), this.entity)
               && this.entity instanceof ChoppedLogBlock.MyEntity choppedEntity
               && choppedEntity.getChops() == this.numChops
            ? this.value
            : null;
      }

      public void put(net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, T value) {
         this.value = value;
         this.level = level;
         this.pos = pos;
         this.blockState = level.getBlockState(pos);
         this.entity = level.getBlockEntity(pos);
         this.numChops = this.entity instanceof ChoppedLogBlock.MyEntity choppedEntity ? choppedEntity.getChops() : 0;
      }

      public void invalidate() {
         this.level = null;
         this.value = null;
         this.blockState = Blocks.AIR.defaultBlockState();
         this.entity = null;
      }
   }
}
