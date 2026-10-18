package com.starfish_studios.another_furniture.util.block;

import com.starfish_studios.another_furniture.registry.AFItemTags;
import com.starfish_studios.another_furniture.registry.AFSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface HammerableBlock {
   default boolean tryHammerBlock(
      net.minecraft.world.level.block.state.properties.Property<?> property,
      net.minecraft.world.item.ItemStack stack,
      net.minecraft.world.level.block.state.BlockState state,
      net.minecraft.world.level.LevelAccessor level,
      net.minecraft.core.BlockPos pos,
      net.minecraft.world.entity.player.Player player
   ) {
      if (property == null || !state.hasProperty(property)) {
         return false;
      } else if (!stack.is(AFItemTags.FURNITURE_HAMMER)) {
         return false;
      } else {
         state = (BlockState)state.cycle(property);
         state = this.updateAfterCycle(state, level, pos);
         level.setBlock(pos, state, 3);
         level.playSound(null, pos, this.getUseSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
         return true;
      }
   }

   default BlockState updateAfterCycle(BlockState state, LevelAccessor level, BlockPos pos) {
      return state;
   }

   default SoundEvent getUseSound() {
      return (SoundEvent)AFSoundEvents.HAMMER_USE.get();
   }
}
