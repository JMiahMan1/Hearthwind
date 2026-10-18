package com.chailotl.particular.particles.splashes;

import com.chailotl.particular.Particles;
import com.chailotl.particular.mixin.AccessorBillboardParticle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import org.jetbrains.annotations.Nullable;

public class WaterSplashEmitterParticle extends NoRenderParticle {
   private final float speed;
   private final float width;
   private final float height;

   WaterSplashEmitterParticle(ClientLevel clientWorld, double x, double y, double z, float width, float speed) {
      super(clientWorld, x, y, z);
      speed = Math.min(2.0F, speed);
      this.gravity = 0.0F;
      this.lifetime = 24;
      this.speed = speed;
      this.width = width;
      this.height = speed / 2.0F + width / 3.0F;
      clientWorld.addParticle(Particles.WATER_SPLASH, true, false, x, y, z, width, this.height, 0.0);
      clientWorld.addParticle(Particles.WATER_SPLASH_FOAM, true, false, x, y, z, width, this.height, 0.0);
      clientWorld.addParticle(Particles.WATER_SPLASH_RING, true, false, x, y, z, width, 0.0, 0.0);
      if (speed > 0.5) {
         this.splash(width, 0.1875F + speed * 1.0F / 8.0F + width / 6.0F, 0.15F);
      } else {
         this.remove();
      }
   }

   public void tick() {
      super.tick();
      if (this.age == 8) {
         this.level
            .addParticle(Particles.WATER_SPLASH, true, false, this.x, this.y, this.z, this.width * 0.66F, this.height * 2.0F, 0.0);
         this.level
            .addParticle(Particles.WATER_SPLASH_FOAM, true, false, this.x, this.y, this.z, this.width * 0.66F, this.height * 2.0F, 0.0);
         this.level.addParticle(Particles.WATER_SPLASH_RING, true, false, this.x, this.y, this.z, this.width * 0.66F, 0.0, 0.0);
         this.splash(this.width * 0.66F, 0.375F + this.speed * 1.0F / 8.0F + this.width / 6.0F, 0.05F);
      }

      if (!this.level.getFluidState(BlockPos.containing(this.x, this.y, this.z)).is(FluidTags.WATER)) {
         this.remove();
      }
   }

   private void splash(float width, float speed, float spread) {
      for (int i = 0; i < width * 20.0F; i++) {
         double var10003 = this.y + 0.0625;
         Particle droplet = Minecraft.getInstance()
            .particleEngine
            .createParticle(ParticleTypes.FALLING_WATER, this.x, var10003, this.z, 0.0, 0.0, 0.0);
         if (droplet != null) {
            double xVel = this.random.triangle(0.0, spread);
            double yVel = speed * this.random.triangle(1.0, 0.25);
            double zVel = this.random.triangle(0.0, spread);
            droplet.setPos(this.x + xVel / spread * width, this.y + 0.0625, this.z + zVel / spread * width);
            droplet.setParticleSpeed(xVel, yVel, zVel);
            ((net.minecraft.client.particle.SingleQuadParticle) droplet).setColor(1.0F, 1.0F, 1.0F);
            ((AccessorBillboardParticle)droplet).setScale(0.125F);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
      public Factory(SpriteSet provider) {
      }

      @Nullable
      public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double x, double y, double z, double g, double h, double i, net.minecraft.util.RandomSource random) {
         return new WaterSplashEmitterParticle(clientWorld, x, y, z, (float)g, (float)h);
      }
   }
}
