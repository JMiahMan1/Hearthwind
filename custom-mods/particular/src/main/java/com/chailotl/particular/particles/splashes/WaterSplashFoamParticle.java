package com.chailotl.particular.particles.splashes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class WaterSplashFoamParticle extends WaterSplashParticle {
   WaterSplashFoamParticle(ClientLevel clientWorld, double x, double y, double z, float width, float height, SpriteSet provider) {
      super(clientWorld, x, y, z, width, height, provider);
      this.colored = false;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
      private final SpriteSet provider;

      public Factory(SpriteSet provider) {
         this.provider = provider;
      }

      @Nullable
      public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double x, double y, double z, double g, double h, double i, net.minecraft.util.RandomSource random) {
         return new WaterSplashFoamParticle(clientWorld, x, y, z, (float)g, (float)h, this.provider);
      }
   }
}
