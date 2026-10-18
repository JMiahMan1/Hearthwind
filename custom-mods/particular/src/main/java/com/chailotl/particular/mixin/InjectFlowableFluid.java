package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({FlowingFluid.class})
public class InjectFlowableFluid {
   @Inject(
      method = {"onScheduledTick"},
      at = {@At("TAIL")}
   )
   protected void spawnCascades(Level world, BlockPos pos, FluidState state, CallbackInfo ci) {
      if (Main.CONFIG.cascades()) {
         Main.updateCascade(world, pos, state);
      }
   }
}
