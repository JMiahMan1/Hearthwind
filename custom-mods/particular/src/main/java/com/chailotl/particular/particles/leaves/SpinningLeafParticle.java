package com.chailotl.particular.particles.leaves;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class SpinningLeafParticle extends LeafParticle {
   protected float angleFactor = (float)(Math.random() * Math.PI * 2.0);

   protected SpinningLeafParticle(ClientLevel world, double x, double y, double z, double r, double g, double b, SpriteSet provider) {
      super(world, x, y, z, r, g, b, provider);
      this.roll = this.getAngle();
   }

   @Override
   protected float getAngle() {
      int a = Math.max(0, this.age - 10);
      return (this.angleFactor + a / (this.rotateFactor + (this.lifetime - a) / 100.0F) / 2.0F) * this.flippedDirection;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
      private final SpriteSet provider;

      public Factory(SpriteSet provider) {
         this.provider = provider;
      }

      @Nullable
      public Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double velX, double velY, double velZ, net.minecraft.util.RandomSource random) {
         return new SpinningLeafParticle(world, x, y, z, velX, velY, velZ, this.provider);
      }
   }
}
