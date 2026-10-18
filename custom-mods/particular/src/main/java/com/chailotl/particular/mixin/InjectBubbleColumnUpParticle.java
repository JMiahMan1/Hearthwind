package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BubbleColumnUpParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BubbleColumnUpParticle.class})
public abstract class InjectBubbleColumnUpParticle extends SingleQuadParticle {
   protected InjectBubbleColumnUpParticle(ClientLevel clientWorld, double d, double e, double f) {
      super(clientWorld, d, e, f, (net.minecraft.client.renderer.texture.TextureAtlasSprite) null);
   }

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void releaseBubbles(CallbackInfo ci) {
      if (Main.CONFIG.poppingBubbles()) {
         if (this.removed) {
            Particle bubble = Minecraft.getInstance()
               .particleEngine
               .createParticle(ParticleTypes.BUBBLE_POP, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            if (bubble != null) {
               ((AccessorBillboardParticle)bubble).setScale(this.quadSize * 2.0F);
            }
         }
      }
   }
}
