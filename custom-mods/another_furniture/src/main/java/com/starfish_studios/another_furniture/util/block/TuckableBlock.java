package com.starfish_studios.another_furniture.util.block;

import com.starfish_studios.another_furniture.block.ChairBlock;
import com.starfish_studios.another_furniture.block.ShelfBlock;
import com.starfish_studios.another_furniture.block.properties.HorizontalConnectionType;
import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import com.starfish_studios.another_furniture.entity.SeatEntity;
import com.starfish_studios.another_furniture.registry.AFBlockTags;
import com.starfish_studios.another_furniture.registry.AFSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.AABB;

public interface TuckableBlock {
   BooleanProperty TUCKED = ModBlockStateProperties.TUCKED;

   static boolean tryTuck(BlockState state, Level level, BlockPos pos, Player player) {
      boolean tucked = (Boolean)state.getValue(TUCKED);
      if (!player.isCrouching() || !canTuckUnderBlockInfront(state, level, pos)) {
         return false;
      } else if (tucked) {
         level.setBlockAndUpdate(pos, (BlockState)state.setValue(TUCKED, false));
         level.playSound(null, pos, (SoundEvent)AFSoundEvents.CHAIR_UNTUCK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
         return true;
      } else if (!isBlockedFromTucking(state, level, pos)) {
         level.setBlockAndUpdate(pos, (BlockState)state.setValue(TUCKED, true));
         level.playSound(null, pos, (SoundEvent)AFSoundEvents.CHAIR_TUCK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }

   static boolean canTuckUnderBlockInfront(BlockState state, Level level, BlockPos pos) {
      Direction forward = (Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING);
      BlockState forwardState = level.getBlockState(pos.relative(forward));
      Block forwardBlock = forwardState.getBlock();
      if (forwardBlock instanceof ShelfBlock) {
         return forwardState.getValue(ShelfBlock.TYPE) == HorizontalConnectionType.MIDDLE
            || forwardState.getValue(BlockStateProperties.HORIZONTAL_FACING) != forward;
      } else if (forwardBlock instanceof SlabBlock) {
         return forwardState.getValue(SlabBlock.TYPE) == SlabType.TOP;
      } else if (!(forwardBlock instanceof StairBlock)) {
         return forwardState.is(AFBlockTags.CHAIRS_TUCKABLE_UNDER);
      } else {
         StairsShape shape = (StairsShape)forwardState.getValue(StairBlock.SHAPE);
         Direction facing_dir = (Direction)forwardState.getValue(BlockStateProperties.HORIZONTAL_FACING);
         return forwardState.getValue(StairBlock.HALF) == Half.TOP
            && (
               forward == facing_dir
                  || shape == StairsShape.OUTER_LEFT && forward == facing_dir.getCounterClockWise()
                  || shape == StairsShape.OUTER_RIGHT && forward == facing_dir.getClockWise()
            );
      }
   }

   static boolean isBlockedFromTucking(BlockState state, Level level, BlockPos pos) {
      if (!level.getEntitiesOfClass(SeatEntity.class, new AABB(pos)).isEmpty()) {
         return true;
      } else {
         Direction facing = (Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING);
         BlockState left = level.getBlockState(pos.relative(facing).relative(facing.getCounterClockWise()));
         BlockState right = level.getBlockState(pos.relative(facing).relative(facing.getClockWise()));
         return left.getBlock() instanceof ChairBlock
               && left.getValue(TUCKED)
               && left.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing.getClockWise()
            ? true
            : right.getBlock() instanceof ChairBlock
               && (Boolean)right.getValue(TUCKED)
               && right.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing.getCounterClockWise();
      }
   }
}
