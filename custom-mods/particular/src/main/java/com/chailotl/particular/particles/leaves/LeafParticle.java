package com.chailotl.particular.particles.leaves;

import com.chailotl.particular.Main;
import com.chailotl.particular.Particles;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class LeafParticle extends SingleQuadParticle {
   protected static final int fadeInDuration = 10;
   protected static final int rampUpDuration = 20;
   protected static final int fadeOutDuration = 100;
   protected float rotateFactor;
   protected float gravityFactor = 0.075F;
   protected final boolean flippedSprite;
   protected final int flippedDirection;
   protected boolean expiring = false;

   protected LeafParticle(ClientLevel world, double x, double y, double z, double r, double g, double b, SpriteSet provider) {
      super(world, x, y, z, r, g, b, provider.get(world.getRandom()));
      this.hasPhysics = true;
      this.gravity = 0.0F;
      this.lifetime = 200;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.alpha = 0.0F;
      this.rCol = (float)r;
      this.gCol = (float)g;
      this.bCol = (float)b;
      this.rotateFactor = 6.0F + (float)Math.random() * 3.0F;
      this.flippedSprite = this.random.nextBoolean();
      this.flippedDirection = this.random.nextBoolean() ? 1 : -1;
      this.quadSize = 0.21875F;
   }

   protected float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   protected float getAngle() {
      int time = this.age - 10;
      float speed = this.rotateFactor;
      float amplitude = this.clamp(time, 0.0F, 30.0F) / 30.0F;
      return (float)Math.sin(time / speed) * amplitude * 0.5F * this.flippedDirection;
   }

   public void tick() {
      super.tick();
      if (this.age <= 10) {
         this.yd = 0.0;
      } else if (!this.expiring && (this.onGround || this.yd == 0.0)) {
         this.expiring = true;
         this.age = this.lifetime - 100;
         this.y += 0.01;
         if (Main.CONFIG.fallingLeavesSettings.layFlatOnGround()) {
            if (Main.CONFIG.fallingLeavesSettings.layFlatRightAngles()) {
               this.roll = (float)(this.random.nextInt(4) / 2.0 * Math.PI);
            } else {
               this.roll = (float)(Math.random() * Math.PI * 2.0);
            }
         }
      }

      this.oRoll = this.roll;
      BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
      FluidState fluidState = this.level.getFluidState(pos);
      if (fluidState.is(FluidTags.WATER)) {
         if (this.gravity > 0.0F) {
            this.y = pos.getY() + fluidState.getHeight(this.level, pos);
            if (Main.CONFIG.fallingLeavesSettings.spawnRipples()) {
               this.level.addParticle(Particles.WATER_RIPPLE, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            }
         }

         this.yd = 0.0;
         this.gravity = 0.0F;
      } else if (this.age >= 10) {
         this.gravity = this.gravityFactor;
         if (!this.onGround) {
            this.roll = this.getAngle();
         }
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
      float j = this.getQuadSize(tickDelta);
      Quaternionf quaternionf;
      if (this.expiring && Main.CONFIG.fallingLeavesSettings.layFlatOnGround()) {
         quaternionf = new Quaternionf().rotateY(this.roll).rotateX((float)Math.PI / 2.0F);
      } else {
         quaternionf = new Quaternionf(camera.rotation());
         if (this.roll != 0.0F) {
            quaternionf.rotateZ(Mth.lerp(tickDelta, this.oRoll, this.roll));
         }
      }
      float ageDelta = Mth.lerpInt(tickDelta, this.age, this.age + 1);
      if (this.age <= 10) {
         this.alpha = ageDelta / 10.0F;
      } else if (this.age > this.lifetime - 100) {
         this.alpha = (this.lifetime - ageDelta) / 100.0F;
      } else {
         this.alpha = 1.0F;
      }
      float l = this.getU0();
      float m = this.getU1();
      float n = this.getV0();
      float o = this.getV1();
      if (this.flippedSprite) {
         float temp = l;
         l = m;
         m = temp;
      }
      int color = ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol);
      state.add(this.getLayer(), f, g, h, quaternionf.x, quaternionf.y, quaternionf.z, quaternionf.w, j, l, m, n, o, color, this.getLightCoords(tickDelta));
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
      private final SpriteSet provider;

      public Factory(SpriteSet provider) {
         this.provider = provider;
      }

      @Nullable
      public Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double velX, double velY, double velZ, net.minecraft.util.RandomSource random) {
         return new LeafParticle(world, x, y, z, velX, velY, velZ, this.provider);
      }
   }
}
