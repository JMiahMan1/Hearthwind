package com.chailotl.particular.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

public class CascadeParticle extends SingleQuadParticle {
   protected final SpriteSet provider;

   protected CascadeParticle(ClientLevel clientWorld, double x, double y, double z, SpriteSet provider) {
      super(clientWorld, x, y, z, provider.get(clientWorld.getRandom()));
      this.provider = provider;
      this.lifetime = 9;
      this.quadSize = 1.0F;
      this.gravity = 0.4F;
      this.setParticleSpeed(this.random.nextDouble() * 0.25 - 0.125, 0.0, this.random.nextDouble() * 0.25 - 0.125);
      this.setSpriteFromAge(provider);
      this.removeIfInsideSolidBlock();
   }

   public void tick() {
      super.tick();
      this.removeIfInsideSolidBlock();
      this.setSpriteFromAge(this.provider);
   }

   private void removeIfInsideSolidBlock() {
      BlockPos pos = BlockPos.containing(new Vec3(this.x, this.y, this.z));
      if (this.level.getBlockState(pos).isRedstoneConductor(this.level, pos)) {
         this.alpha = 0.0F;
         this.remove();
      }
   }

   public SingleQuadParticle.Layer getLayer() {
      return SingleQuadParticle.Layer.OPAQUE;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
      private final SpriteSet provider;

      public Factory(SpriteSet provider) {
         this.provider = provider;
      }

      public Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double velX, double velY, double velZ, net.minecraft.util.RandomSource random) {
         return new CascadeParticle(world, x, y, z, this.provider);
      }
   }
}
