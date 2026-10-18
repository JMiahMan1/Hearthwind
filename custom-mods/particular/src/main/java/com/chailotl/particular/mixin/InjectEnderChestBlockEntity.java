package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import com.chailotl.particular.Particles;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EnderChestBlockEntity.class})
public abstract class InjectEnderChestBlockEntity extends BlockEntity implements LidBlockEntity {
   @Unique
   private int ticksUntilNextSwitch = 20;
   @Unique
   private boolean isOpen = false;
   @Unique
   private static final int minClosedTime = 160;
   @Unique
   private static final int maxClosedTime = 480;
   @Unique
   private static final int minOpenTime = 40;
   @Unique
   private static final int maxOpenTime = 60;

   public InjectEnderChestBlockEntity(
      net.minecraft.world.level.block.entity.BlockEntityType<?> type,
      net.minecraft.core.BlockPos pos,
      net.minecraft.world.level.block.state.BlockState state
   ) {
      super(type, pos, state);
   }

   @Inject(
      method = {"clientTick"},
      at = {@At("TAIL")}
   )
   private static void randomlyOpen(Level world, BlockPos pos, BlockState state, InjectEnderChestBlockEntity blockEntity, CallbackInfo ci) {
      if (Main.CONFIG.soulSandBubbles()) {
         if ((Boolean)state.getValue(BlockStateProperties.WATERLOGGED)
            && world.getBlockState(pos.below()).getBlock() == Blocks.SOUL_SAND
            && !world.getBlockState(pos.above()).isRedstoneConductor(world, pos.above())) {
            if (--blockEntity.ticksUntilNextSwitch <= 0) {
               ContainerOpenersCounter manager = ((AccessorEnderChestBlockEntity)blockEntity).getStateManager();
               if (blockEntity.isOpen) {
                  blockEntity.isOpen = false;
                  blockEntity.ticksUntilNextSwitch = world.getRandom().nextIntBetweenInclusive(160, 480);
                  ((AccessorEnderChestBlockEntity)blockEntity).getLidAnimator().shouldBeOpen(false);
                  ((InvokerViewerCountManager)manager).invokeOnContainerClose(world, pos, blockEntity.getBlockState());
               } else {
                  blockEntity.isOpen = true;
                  blockEntity.ticksUntilNextSwitch = world.getRandom().nextIntBetweenInclusive(40, 60);
                  ((AccessorEnderChestBlockEntity)blockEntity).getLidAnimator().shouldBeOpen(true);
                  ((InvokerViewerCountManager)manager).invokeOnContainerOpen(world, pos, blockEntity.getBlockState());
                  world.playLocalSound(
                     pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.AMBIENT, 1.0F, 1.0F, true
                  );
               }
            }

            if (blockEntity.isOpen && blockEntity.ticksUntilNextSwitch > 10 && blockEntity.ticksUntilNextSwitch % 2 == 0) {
               Main.spawnBubble(Particles.ENDER_BUBBLE, world, pos);
            }
         }
      }
   }
}
