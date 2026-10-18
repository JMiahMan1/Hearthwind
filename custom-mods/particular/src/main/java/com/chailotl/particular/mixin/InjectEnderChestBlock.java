package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import com.chailotl.particular.Particles;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EnderChestBlock.class})
public class InjectEnderChestBlock {
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
      if (Main.CONFIG.chestBubbles()) {
         if ((Boolean)state.getValue(BlockStateProperties.WATERLOGGED) && !world.getBlockState(pos.above()).isRedstoneConductor(world, pos.above())) {
            Main.spawnChestBubbles(Particles.ENDER_BUBBLE, world, pos);
         }
      }
   }
}
