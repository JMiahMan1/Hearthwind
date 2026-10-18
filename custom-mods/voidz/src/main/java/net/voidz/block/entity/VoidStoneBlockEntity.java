package net.voidz.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.voidz.block.VoidStoneBlock;
import net.voidz.init.BlockInit;

public class VoidStoneBlockEntity extends BlockEntity {
   private int ticker;

   public VoidStoneBlockEntity(BlockPos pos, BlockState state) {
      super(BlockInit.VOID_BLOCK_ENTITY, pos, state);
   }

   public void tick() {
      if (!this.level.isClientSide() && (Boolean)this.getBlockState().getValue(VoidStoneBlock.ACTIVATED)) {
         this.ticker++;
         if (this.ticker % 10 == 0) {
            if ((Integer)this.getBlockState().getValue(VoidStoneBlock.DESTROYTIME) == 7) {
               this.level.destroyBlock(this.worldPosition, false);
            }

            if ((Integer)this.getBlockState().getValue(VoidStoneBlock.DESTROYTIME) < 7) {
               this.level
                  .setBlockAndUpdate(
                     this.worldPosition,
                     (BlockState)((BlockState)this.getBlockState().setValue(VoidStoneBlock.ACTIVATED, true))
                        .setValue(VoidStoneBlock.DESTROYTIME, (Integer)this.getBlockState().getValue(VoidStoneBlock.DESTROYTIME) + 1)
                  );
            }
         }
      }
   }
}
