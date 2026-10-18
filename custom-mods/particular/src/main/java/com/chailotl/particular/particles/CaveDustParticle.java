package com.chailotl.particular.particles;

import com.chailotl.particular.Main;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class CaveDustParticle extends BaseAshSmokeParticle {
   protected CaveDustParticle(
      ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteSet spriteProvider
   ) {
      super(
         world,
         x,
         y,
         z,
         0.0F,
         0.0F,
         0.0F,
         velocityX,
         velocityY,
         velocityZ,
         scaleMultiplier,
         spriteProvider,
         0.0F,
         Main.CONFIG.caveDustSettings.baseMaxAge(),
         0.0F,
         true
      );
      Color color = Main.CONFIG.caveDustSettings.color();
      this.rCol = color.red();
      this.gCol = color.green();
      this.bCol = color.blue();
      this.gravity = (this.random.nextFloat() - 0.5F) * Main.CONFIG.caveDustSettings.maxAcceleration();
   }

   public void tick() {
      super.tick();
      if (this.random.nextInt(Main.CONFIG.caveDustSettings.accelChangeChance()) == 0) {
         this.gravity = (this.random.nextFloat() - 0.5F) * Main.CONFIG.caveDustSettings.maxAcceleration();
      }

      int fadeDuration = Main.CONFIG.caveDustSettings.fadeDuration();
      if (this.age <= fadeDuration) {
         this.alpha = (float)this.age / fadeDuration;
      } else if (this.age > this.lifetime - fadeDuration) {
         this.alpha = (float)(this.lifetime - this.age) / fadeDuration;
      }
   }

   public SingleQuadParticle.Layer getLayer() {
      return SingleQuadParticle.Layer.TRANSLUCENT;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
      private final SpriteSet provider;

      public Factory(SpriteSet provider) {
         this.provider = provider;
      }

      public Particle createParticle(
         SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double x, double y, double z, double velX, double velY, double velZ
      , net.minecraft.util.RandomSource random) {
         return new CaveDustParticle(clientWorld, x, y, z, 0.0, 0.0, 0.0, 1.0F, this.provider);
      }
   }
}
