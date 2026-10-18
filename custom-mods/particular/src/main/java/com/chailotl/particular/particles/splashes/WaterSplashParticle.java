package com.chailotl.particular.particles.splashes;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class WaterSplashParticle extends SingleQuadParticle {
   protected final SpriteSet provider;
   private final float width;
   private final float height;
   private final Color color;
   private final float unit;
   protected boolean colored = true;

   WaterSplashParticle(ClientLevel clientWorld, double x, double y, double z, float width, float height, SpriteSet provider) {
      super(clientWorld, x, y, z, provider.get(clientWorld.getRandom()));
      this.gravity = 0.0F;
      this.lifetime = 18;
      this.width = width;
      this.height = height;
      this.provider = provider;
      this.color = new Color(BiomeColors.getAverageWaterColor(clientWorld, BlockPos.containing(x, y, z)));
      this.unit = 2.0F / Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_PARTICLES).getWidth();
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

   protected float getU0() {
      return super.getU0() + this.unit;
   }

   protected float getU1() {
      return super.getU1() - this.unit;
   }

   public void extract(QuadParticleRenderState state, Camera camera, float tickDelta) {
      Vec3 vec3d = camera.position();
      float f = (float)(Mth.lerp(tickDelta, this.xo, this.x) - vec3d.x());
      float g = (float)(Mth.lerp(tickDelta, this.yo, this.y) - vec3d.y());
      float h = (float)(Mth.lerp(tickDelta, this.zo, this.z) - vec3d.z());
      float scale = this.getQuadSize(tickDelta);
      if (this.colored) {
         this.rCol = this.color.getRed() / 255.0F;
         this.gCol = this.color.getGreen() / 255.0F;
         this.bCol = this.color.getBlue() / 255.0F;
      } else {
         this.rCol = 1.0F;
         this.gCol = 1.0F;
         this.bCol = 1.0F;
      }

      float y = g + this.height * 0.5F;

      for (int i = 0; i < 4; i++) {
         float angle = (float)((i - 1) * Math.PI / 2.0);
         Quaternionf quaternionf = new Quaternionf().rotateY(angle);
         float mx = i == 0 ? -scale : (i == 2 ? scale : 0.0F);
         float mz = i == 1 ? scale : (i == 3 ? -scale : 0.0F);
         state.add(
            this.getLayer(),
            f + mx,
            y,
            h + mz,
            quaternionf.x,
            quaternionf.y,
            quaternionf.z,
            quaternionf.w,
            scale,
            this.getU0(),
            this.getU1(),
            this.getV0(),
            this.getV1(),
            ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol),
            this.getLightCoords(tickDelta)
         );
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
      private final SpriteSet provider;

      public Factory(SpriteSet provider) {
         this.provider = provider;
      }

      @Nullable
      public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double x, double y, double z, double g, double h, double i, net.minecraft.util.RandomSource random) {
         return new WaterSplashParticle(clientWorld, x, y, z, (float)g, (float)h, this.provider);
      }
   }
}
