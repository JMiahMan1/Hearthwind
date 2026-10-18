package com.chailotl.particular.particles.splashes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class WaterSplashRingParticle extends SingleQuadParticle {
   protected final SpriteSet provider;
   private final float width;

   WaterSplashRingParticle(ClientLevel clientWorld, double x, double y, double z, float width, SpriteSet provider) {
      super(clientWorld, x, y, z, provider.get(clientWorld.getRandom()));
      this.gravity = 0.0F;
      this.lifetime = 18;
      this.width = width;
      this.provider = provider;
      this.setSpriteFromAge(provider);
   }

   public SingleQuadParticle.Layer getLayer() {
      return SingleQuadParticle.Layer.TRANSLUCENT;
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.provider);
      if (!this.level.getFluidState(BlockPos.containing(this.x, this.y, this.z)).is(FluidTags.WATER)) {
         this.remove();
      }
   }

   public float getQuadSize(float tickDelta) {
      float ageDelta = Mth.lerp(tickDelta, this.age - 1, this.age);
      return this.width * (0.8F + 0.2F * (ageDelta / this.lifetime));
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

      @Nullable
      public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double x, double y, double z, double g, double h, double i, net.minecraft.util.RandomSource random) {
         return new WaterSplashRingParticle(clientWorld, x, y, z, (float)g, this.provider);
      }
   }
}
