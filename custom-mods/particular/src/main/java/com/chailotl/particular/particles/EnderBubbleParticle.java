package com.chailotl.particular.particles;

import com.chailotl.particular.Main;
import com.chailotl.particular.Particles;
import com.chailotl.particular.mixin.AccessorBillboardParticle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BubbleColumnUpParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class EnderBubbleParticle extends BubbleColumnUpParticle {
   protected EnderBubbleParticle(ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite) {
      super(clientWorld, d, e, f, g, h, i, sprite);
   }

   public void tick() {
      super.tick();
      if (Main.CONFIG.poppingBubbles()) {
         if (this.removed) {
            Particle bubble = Minecraft.getInstance()
               .particleEngine
               .createParticle(Particles.ENDER_BUBBLE_POP, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            if (bubble != null) {
               ((AccessorBillboardParticle)bubble).setScale(this.quadSize * 2.0F);
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
      private final SpriteSet spriteProvider;

      public Factory(SpriteSet spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
         return new EnderBubbleParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider.get(clientWorld.getRandom()));
      }
   }
}
