package com.chailotl.particular.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class WaterRippleParticle extends SingleQuadParticle {
   protected final SpriteSet provider;

   protected WaterRippleParticle(ClientLevel world, double x, double y, double z, SpriteSet provider) {
      super(world, x, y, z, provider.get(world.getRandom()));
      this.lifetime = 7;
      this.alpha = 0.2F;
      this.quadSize = 0.25F;
      this.provider = provider;
      this.setSpriteFromAge(provider);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.setSpriteFromAge(this.provider);
      }
   }

   public SingleQuadParticle.Layer getLayer() {
      return SingleQuadParticle.Layer.TRANSLUCENT;
   }

   public void extract(QuadParticleRenderState state, Camera camera, float tickDelta) {
      Vec3 vec3d = camera.position();
      float f = (float)(Mth.lerp(tickDelta, this.xo, this.x) - vec3d.x());
      float g = (float)(Mth.lerp(tickDelta, this.yo, this.y) - vec3d.y());
      float h = (float)(Mth.lerp(tickDelta, this.zo, this.z) - vec3d.z());
      Quaternionf quaternionf = new Quaternionf().rotateX((float)Math.PI / 2.0F);
      state.add(
         this.getLayer(),
         f,
         g,
         h,
         quaternionf.x,
         quaternionf.y,
         quaternionf.z,
         quaternionf.w,
         this.getQuadSize(tickDelta),
         this.getU0(),
         this.getU1(),
         this.getV0(),
         this.getV1(),
         ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol),
         this.getLightCoords(tickDelta)
      );
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
      private final SpriteSet provider;

      public Factory(SpriteSet provider) {
         this.provider = provider;
      }

      public Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double velX, double velY, double velZ, net.minecraft.util.RandomSource random) {
         return new WaterRippleParticle(world, x, y, z, this.provider);
      }
   }
}
