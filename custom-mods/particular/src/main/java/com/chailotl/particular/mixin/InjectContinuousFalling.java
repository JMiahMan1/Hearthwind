package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import com.chailotl.particular.Particles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.DripParticle.FallAndLandParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({FallAndLandParticle.class})
public abstract class InjectContinuousFalling extends SingleQuadParticle {
   @Shadow
   @Final
   protected ParticleOptions landParticle;

   protected InjectContinuousFalling(ClientLevel clientWorld, double d, double e, double f) {
      super(clientWorld, d, e, f, (net.minecraft.client.renderer.texture.TextureAtlasSprite) null);
   }

   @Inject(
      method = {"postMoveUpdate"},
      at = {@At("TAIL")}
   )
   private void addRipples(CallbackInfo ci) {
      if (Main.CONFIG.waterDripRipples()) {
         if (this.landParticle == ParticleTypes.SPLASH) {
            BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
            FluidState fluidState = this.level.getFluidState(pos);
            if (fluidState.is(FluidTags.WATER)) {
               float yWater = pos.getY() + fluidState.getHeight(this.level, pos);
               if (this.y < yWater) {
                  this.level.addParticle(Particles.WATER_RIPPLE, this.x, yWater, this.z, 0.0, 0.0, 0.0);
               }
            }
         }
      }
   }
}
