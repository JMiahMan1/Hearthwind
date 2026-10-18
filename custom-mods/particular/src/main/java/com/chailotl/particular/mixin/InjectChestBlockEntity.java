package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChestBlockEntity.class})
public abstract class InjectChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
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

   public InjectChestBlockEntity(
      net.minecraft.world.level.block.entity.BlockEntityType<?> type,
      net.minecraft.core.BlockPos pos,
      net.minecraft.world.level.block.state.BlockState state
   ) {
      super(type, pos, state);
   }

   @Unique
   private static boolean getSoulSand(Level world, BlockPos pos, BlockState state) {
      pos = pos.below();
      if (world.getBlockState(pos).getBlock() == Blocks.SOUL_SAND) {
         return true;
      } else if (state.getValue(BlockStateProperties.CHEST_TYPE) == ChestType.RIGHT) {
         BlockPos pos2 = pos.offset(((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)).getCounterClockWise(Axis.Y).getUnitVec3i());
         return world.getBlockState(pos2).getBlock() == Blocks.SOUL_SAND;
      } else {
         return false;
      }
   }

   @Inject(
      method = {"clientTick"},
      at = {@At("TAIL")}
   )
   private static void randomlyOpen(Level world, BlockPos pos, BlockState state, InjectChestBlockEntity blockEntity, CallbackInfo ci) {
      if (Main.CONFIG.soulSandBubbles()) {
         if ((Boolean)state.getValue(BlockStateProperties.WATERLOGGED)
            && state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.LEFT
            && getSoulSand(world, pos, state)) {
            if (--blockEntity.ticksUntilNextSwitch <= 0) {
               ContainerOpenersCounter manager = ((AccessorChestBlockEntity)blockEntity).getStateManager();
               if (blockEntity.isOpen) {
                  blockEntity.isOpen = false;
                  blockEntity.ticksUntilNextSwitch = world.getRandom().nextIntBetweenInclusive(160, 480);
                  ((AccessorChestBlockEntity)blockEntity).getLidAnimator().shouldBeOpen(false);
                  ((InvokerViewerCountManager)manager).invokeOnContainerClose(world, pos, blockEntity.getBlockState());
               } else {
                  blockEntity.isOpen = true;
                  blockEntity.ticksUntilNextSwitch = world.getRandom().nextIntBetweenInclusive(40, 60);
                  ((AccessorChestBlockEntity)blockEntity).getLidAnimator().shouldBeOpen(true);
                  ((InvokerViewerCountManager)manager).invokeOnContainerOpen(world, pos, blockEntity.getBlockState());
                  world.playLocalSound(
                     pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.AMBIENT, 1.0F, 1.0F, true
                  );
               }
            }

            if (blockEntity.isOpen && blockEntity.ticksUntilNextSwitch > 10 && blockEntity.ticksUntilNextSwitch % 2 == 0) {
               if (state.getValue(BlockStateProperties.CHEST_TYPE) == ChestType.SINGLE) {
                  Main.spawnBubble(ParticleTypes.BUBBLE_COLUMN_UP, world, pos);
               } else {
                  Main.spawnDoubleBubbles(ParticleTypes.BUBBLE_COLUMN_UP, world, pos, state);
               }
            }
         }
      }
   }
}
