package com.chailotl.particular;

import com.chailotl.particular.particles.CascadeParticle;
import com.chailotl.particular.particles.CaveDustParticle;
import com.chailotl.particular.particles.EnderBubbleParticle;
import com.chailotl.particular.particles.FireflyParticle;
import com.chailotl.particular.particles.WaterRippleParticle;
import com.chailotl.particular.particles.WaterfallSprayParticle;
import com.chailotl.particular.particles.leaves.BigLeafParticle;
import com.chailotl.particular.particles.leaves.ConiferLeafParticle;
import com.chailotl.particular.particles.leaves.LeafParticle;
import com.chailotl.particular.particles.leaves.SpinningLeafParticle;
import com.chailotl.particular.particles.splashes.WaterSplashEmitterParticle;
import com.chailotl.particular.particles.splashes.WaterSplashFoamParticle;
import com.chailotl.particular.particles.splashes.WaterSplashParticle;
import com.chailotl.particular.particles.splashes.WaterSplashRingParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.PendingParticleProvider;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class Particles {
   public static final SimpleParticleType OAK_LEAF = registerParticle("oak_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType BIRCH_LEAF = registerParticle("birch_leaf", SpinningLeafParticle.Factory::new);
   public static final SimpleParticleType SPRUCE_LEAF = registerParticle("spruce_leaf", ConiferLeafParticle.Factory::new);
   public static final SimpleParticleType JUNGLE_LEAF = registerParticle("jungle_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType ACACIA_LEAF = registerParticle("acacia_leaf", SpinningLeafParticle.Factory::new);
   public static final SimpleParticleType DARK_OAK_LEAF = registerParticle("dark_oak_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType AZALEA_LEAF = registerParticle("azalea_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType MANGROVE_LEAF = registerParticle("mangrove_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType WHITE_OAK_LEAF = registerParticle("white_oak_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType WHITE_SPRUCE_LEAF = registerParticle("white_spruce_leaf", ConiferLeafParticle.Factory::new);
   public static final SimpleParticleType MAPLE_LEAF = registerParticle("maple_leaf", SpinningLeafParticle.Factory::new);
   public static final SimpleParticleType BRIMWOOD_LEAF = registerParticle("brimwood_leaf", SpinningLeafParticle.Factory::new);
   public static final SimpleParticleType RU_BAOBAB_LEAF = registerParticle("ru_baobab_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType KAPOK_LEAF = registerParticle("kapok_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType EUCALYPTUS_LEAF = registerParticle("eucalyptus_leaf", BigLeafParticle.Factory::new);
   public static final SimpleParticleType REDWOOD_LEAF = registerParticle("redwood_leaf", ConiferLeafParticle.Factory::new);
   public static final SimpleParticleType MAGNOLIA_LEAF = registerParticle("magnolia_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType RU_PALM_LEAF = registerParticle("ru_palm_leaf", BigLeafParticle.Factory::new);
   public static final SimpleParticleType LARCH_LEAF = registerParticle("larch_leaf", ConiferLeafParticle.Factory::new);
   public static final SimpleParticleType GOLDEN_LARCH_LEAF = registerParticle("golden_larch_leaf", ConiferLeafParticle.Factory::new);
   public static final SimpleParticleType SOCOTRA_LEAF = registerParticle("socotra_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType BAMBOO_LEAF = registerParticle("bamboo_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType WILLOW_LEAF = registerParticle("willow_leaf", LeafParticle.Factory::new);
   public static final SimpleParticleType RU_CYPRESS_LEAF = registerParticle("ru_cypress_leaf", ConiferLeafParticle.Factory::new);
   public static final SimpleParticleType WW_BAOBAB_LEAF = registerParticle("ww_baobab_leaf", SpinningLeafParticle.Factory::new);
   public static final SimpleParticleType WW_CYPRESS_LEAF = registerParticle("ww_cypress_leaf", ConiferLeafParticle.Factory::new);
   public static final SimpleParticleType WW_PALM_LEAF = registerParticle("ww_palm_leaf", BigLeafParticle.Factory::new);
   public static final SimpleParticleType WATER_RIPPLE = registerParticle("water_ripple", WaterRippleParticle.Factory::new);
   public static final SimpleParticleType ENDER_BUBBLE = registerParticle("ender_bubble", EnderBubbleParticle.Factory::new);
   public static final SimpleParticleType ENDER_BUBBLE_POP = registerParticle("ender_bubble_pop", EnderBubblePopProvider::new);
   public static final SimpleParticleType CAVE_DUST = registerParticle("cave_dust", CaveDustParticle.Factory::new);
   public static final SimpleParticleType FIREFLY = registerParticle("firefly", FireflyParticle.Factory::new);
   public static final SimpleParticleType WATERFALL_SPRAY = registerParticle("waterfall_spray", WaterfallSprayParticle.Factory::new);
   public static final SimpleParticleType CASCADE = FabricParticleTypes.simple(true);
   public static final SimpleParticleType WATER_SPLASH_EMITTER = FabricParticleTypes.simple(true);
   public static final SimpleParticleType WATER_SPLASH = FabricParticleTypes.simple(true);
   public static final SimpleParticleType WATER_SPLASH_FOAM = FabricParticleTypes.simple(true);
   public static final SimpleParticleType WATER_SPLASH_RING = FabricParticleTypes.simple(true);

   public static void register() {
      Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath("particular", "cascade"), CASCADE);
      Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath("particular", "water_splash_emitter"), WATER_SPLASH_EMITTER);
      Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath("particular", "water_splash"), WATER_SPLASH);
      Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath("particular", "water_splash_foam"), WATER_SPLASH_FOAM);
      Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath("particular", "water_splash_ring"), WATER_SPLASH_RING);
      ParticleProviderRegistry.getInstance().register(CASCADE, CascadeParticle.Factory::new);
      ParticleProviderRegistry.getInstance().register(WATER_SPLASH_EMITTER, WaterSplashEmitterParticle.Factory::new);
      ParticleProviderRegistry.getInstance().register(WATER_SPLASH, WaterSplashParticle.Factory::new);
      ParticleProviderRegistry.getInstance().register(WATER_SPLASH_FOAM, WaterSplashFoamParticle.Factory::new);
      ParticleProviderRegistry.getInstance().register(WATER_SPLASH_RING, WaterSplashRingParticle.Factory::new);
   }

   public static net.minecraft.core.particles.SimpleParticleType registerParticle(
      String name, PendingParticleProvider<net.minecraft.core.particles.SimpleParticleType> constructor
   ) {
      SimpleParticleType particle = FabricParticleTypes.simple();
      Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath("particular", name), particle);
      ParticleProviderRegistry.getInstance().register(particle, constructor);
      return particle;
   }

   @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
   public static class EnderBubblePopProvider implements net.minecraft.client.particle.ParticleProvider<SimpleParticleType> {
      private final net.minecraft.client.particle.SpriteSet sprites;

      public EnderBubblePopProvider(net.minecraft.client.particle.SpriteSet sprites) {
         this.sprites = sprites;
      }

      @org.jetbrains.annotations.Nullable
      public net.minecraft.client.particle.Particle createParticle(
         SimpleParticleType type,
         net.minecraft.client.multiplayer.ClientLevel world,
         double x,
         double y,
         double z,
         double velX,
         double velY,
         double velZ,
         net.minecraft.util.RandomSource random
      ) {
         return new net.minecraft.client.particle.BubblePopParticle(world, x, y, z, velX, velY, velZ, this.sprites);
      }
   }
}
