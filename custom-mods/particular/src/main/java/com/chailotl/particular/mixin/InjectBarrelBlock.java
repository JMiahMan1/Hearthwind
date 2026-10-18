package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BarrelBlock.class})
public class InjectBarrelBlock {
   @Inject(
      method = {"onUse"},
      at = {@At("HEAD")}
   )
   private void releaseBubbles(
      net.minecraft.world.level.block.state.BlockState state,
      net.minecraft.world.level.Level world,
      net.minecraft.core.BlockPos pos,
      net.minecraft.world.entity.player.Player player,
      net.minecraft.world.phys.BlockHitResult hit,
      CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir
   ) {
      if (Main.CONFIG.barrelBubbles()) {
         Direction direction = (Direction)state.getValue(BlockStateProperties.FACING);
         if (direction != Direction.DOWN && world.getFluidState(pos.offset(direction.getUnitVec3i())).is(FluidTags.WATER)) {
            for (int i = 0; i < 10; i++) {
               ParticleUtils.spawnParticleOnFace(world, pos, direction, ParticleTypes.BUBBLE_COLUMN_UP, Vec3.ZERO, 0.55);
            }
         }
      }
   }
}
