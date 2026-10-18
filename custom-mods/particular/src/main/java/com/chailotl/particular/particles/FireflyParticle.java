package com.chailotl.particular.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class FireflyParticle extends SingleQuadParticle {
   private static final int minOffTime = 40;
   private static final int maxOffTime = 80;
   private static final int minOnTime = 10;
   private static final int maxOnTime = 20;
   private final SimplexNoise noise;
   private int ageOffset = 0;
   private int ticksUntilNextSwitch = 40;
   private boolean isOn = false;

   protected FireflyParticle(ClientLevel world, double x, double y, double z, SpriteSet provider) {
      super(world, x, y, z, provider.get(world.getRandom()));
      this.gravity = 0.0F;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.alpha = 0.0F;
      this.rCol = 0.73333335F;
      this.gCol = 1.0F;
      this.bCol = 0.41960785F;
      if (world.getRandom().nextInt(10) == 1) {
         this.rCol = 0.41960785F;
         this.gCol = 0.98039216F;
         this.bCol = 1.0F;
         if (world.getRandom().nextInt(10) == 1) {
            this.rCol = 1.0F;
            this.gCol = 0.4862745F;
            this.bCol = 0.41960785F;
         }
      }

      this.lifetime = 200;
      this.quadSize = 0.25F;
      this.noise = new SimplexNoise(this.random);
   }

   public void tick() {
      super.tick();
      if (this.onGround) {
         this.ageOffset += 5;
      }

      if (--this.ticksUntilNextSwitch <= 0) {
         if (this.isOn) {
            this.isOn = false;
            this.ticksUntilNextSwitch = this.random.nextIntBetweenInclusive(40, 80);
         } else {
            this.isOn = true;
            this.ticksUntilNextSwitch = this.random.nextIntBetweenInclusive(10, 20);
         }
      }

      this.alpha = this.isOn && this.lifetime - this.age > 3 ? Math.min(1.0F, this.alpha + 0.33F) : Math.max(0.0F, this.alpha - 0.33F);
      float speedFactor = 0.1F;
      float noiseFactor = 0.0033333334F;
      this.xd = this.noise.getValue(this.age * noiseFactor, this.age * noiseFactor) * speedFactor;
      this.yd = this.noise
            .getValue((this.age + this.ageOffset) * noiseFactor - 50.0F, (this.age + this.ageOffset) * noiseFactor + 100.0F)
         * speedFactor
         * 0.5;
      this.zd = this.noise.getValue(this.age * noiseFactor + 100.0F, this.age * noiseFactor - 50.0F) * speedFactor;
      speedFactor = 1.0F / (10.0F + (float)Math.sin(Math.PI + this.age / 30.0F) * 2.0F);
      noiseFactor = 0.033333335F;
      this.xd = this.xd + this.noise.getValue(this.age * noiseFactor, this.age * noiseFactor) * speedFactor * 0.5;
      this.yd = this.yd
         + this.noise.getValue((this.age + this.ageOffset) * noiseFactor - 50.0F, (this.age + this.ageOffset) * noiseFactor + 100.0F)
            * speedFactor;
      this.zd = this.zd
         + this.noise.getValue(this.age * noiseFactor + 100.0F, this.age * noiseFactor - 50.0F) * speedFactor * 0.5;
   }

   protected int getLightCoords(float tint) {
      return 15728880;
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

      public Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double velX, double velY, double velZ, net.minecraft.util.RandomSource random) {
         return new FireflyParticle(world, x, y, z, this.provider);
      }
   }
}
