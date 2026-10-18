package com.chailotl.particular.particles;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.material.FluidState;

public class WaterfallSprayParticle extends WaterDropParticle {
   protected WaterfallSprayParticle(ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite) {
      super(clientWorld, d, e, f, sprite);
      this.xd += g;
      this.yd *= 0.75;
      this.zd += i;
      Color color = new Color(BiomeColors.getAverageWaterColor(clientWorld, BlockPos.containing(this.x, this.y, this.z)));
      this.rCol = color.getRed() / 255.0F;
      this.gCol = color.getGreen() / 255.0F;
      this.bCol = color.getBlue() / 255.0F;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.lifetime-- <= 0) {
         this.remove();
      } else {
         this.yd = this.yd - this.gravity;
         this.move(this.xd, this.yd, this.zd);
         this.xd *= 0.98F;
         this.yd *= 0.98F;
         this.zd *= 0.98F;
         if (this.onGround) {
            if (Math.random() < 0.5) {
               this.remove();
            }

            this.xd *= 0.7F;
            this.zd *= 0.7F;
         }

         BlockPos blockPos = BlockPos.containing(this.x, this.y, this.z);
         double d = this.level
            .getBlockState(blockPos)
            .getCollisionShape(this.level, blockPos)
            .max(Axis.Y, this.x - blockPos.getX(), this.z - blockPos.getZ());
         FluidState fluidState = this.level.getFluidState(blockPos);
         if (fluidState.isSource()) {
            d = Math.max(d, (double)fluidState.getHeight(this.level, blockPos));
         }

         if (d > 0.0 && this.y < blockPos.getY() + d) {
            this.remove();
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
      private final SpriteSet spriteProvider;

      public Factory(SpriteSet spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
         return new WaterfallSprayParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider.get(clientWorld.getRandom()));
      }
   }
}
