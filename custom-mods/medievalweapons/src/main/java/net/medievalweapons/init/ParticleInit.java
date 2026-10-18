package net.medievalweapons.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.medievalweapons.MedievalMain;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ParticleInit {
   public static final SimpleParticleType HEALING_AURA_PARTICLE = FabricParticleTypes.simple();

   public static void init() {
      Registry.register(BuiltInRegistries.PARTICLE_TYPE, MedievalMain.identifierOf("healing_aura_particle"), HEALING_AURA_PARTICLE);
   }

   @Environment(EnvType.CLIENT)
   static class HealingAuraParticle extends SingleQuadParticle {
      private final SpriteSet sprites;
      private boolean reachedGround;

      public HealingAuraParticle(ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, SpriteSet sprites) {
         super(clientWorld, d, e, f, g, h, i, sprites.get(clientWorld.getRandom()));
         this.rCol = Mth.nextFloat(this.random, 0.182645F, 0.787239F);
         this.bCol = Mth.nextFloat(this.random, 0.136784F, 0.756783F);
         this.gCol = Mth.nextFloat(this.random, 0.8235294F, 0.9764706F);
         this.hasPhysics = true;
         this.quadSize *= 0.75F;
         this.lifetime = (int)(20.0 / (this.random.nextFloat() * 0.8 + 0.2));
         this.reachedGround = false;
         this.hasPhysics = true;
         this.sprites = sprites;
         this.setSpriteFromAge(sprites);
      }

      public void tick() {
         if (this.random.nextInt(2) == 0 && this.age == 0) {
            this.remove();
         }

         this.xo = this.x;
         this.yo = this.y;
         this.zo = this.z;
         if (this.age++ >= this.lifetime) {
            this.remove();
         } else {
            this.setSpriteFromAge(this.sprites);
            if (this.onGround) {
               this.yd = 0.0;
               this.reachedGround = true;
            }

            if (this.reachedGround) {
               this.yd += 0.001;
            }

            this.move(this.xd * 0.1, this.yd * 0.1, this.zd * 0.1);
            if (this.y == this.yo) {
               this.xd *= 1.1;
               this.zd *= 1.1;
            }

            this.xd *= 0.96F;
            this.zd *= 0.96F;
            if (this.reachedGround) {
               this.yd *= 0.96F;
            }
         }
      }

      protected SingleQuadParticle.Layer getLayer() {
         return SingleQuadParticle.Layer.bySprite(this.sprite);
      }

      public float getQuadSize(float tickDelta) {
         return this.quadSize * Mth.clamp((this.age + tickDelta) / this.lifetime * 32.0F, 0.0F, 1.0F);
      }

      @Environment(EnvType.CLIENT)
      public static class Factory implements net.minecraft.client.particle.ParticleProvider<net.minecraft.core.particles.SimpleParticleType> {
         private final FabricSpriteSet sprites;

         public Factory(FabricSpriteSet sprites) {
            this.sprites = sprites;
         }

         public Particle createParticle(
            SimpleParticleType type, ClientLevel world, double x, double y, double z, double vX, double vY, double vZ, RandomSource random
         ) {
            return new ParticleInit.HealingAuraParticle(world, x, y, z, vX, vY, vZ, this.sprites);
         }
      }
   }
}
