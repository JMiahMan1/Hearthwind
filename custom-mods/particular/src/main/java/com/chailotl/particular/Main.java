package com.chailotl.particular;

import com.chailotl.particular.compat.RegionsUnexplored;
import com.chailotl.particular.compat.Traverse;
import com.chailotl.particular.compat.WilderWild;
import com.chailotl.particular.mixin.AccessorBiome;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.Load;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.Unload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartLevelTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ClientModInitializer {
   public static final String MOD_ID = "particular";
   public static final Logger LOGGER = LoggerFactory.getLogger("particular");
   public static final ParticularConfig CONFIG = ParticularConfig.createAndLoad();
   public static Identifier currentDimension;
   public static ConcurrentHashMap<net.minecraft.core.BlockPos, Integer> cascades = new ConcurrentHashMap<>();
   private static float fireflyFrequency = 1.0F;
   private static Map<net.minecraft.world.level.block.Block, Main.LeafData> leavesData = new HashMap<>(
      Map.of(
         Blocks.OAK_LEAVES,
         new Main.LeafData(Particles.OAK_LEAF),
         Blocks.BIRCH_LEAVES,
         new Main.LeafData(Particles.BIRCH_LEAF, new Color(FoliageColor.FOLIAGE_BIRCH)),
         Blocks.SPRUCE_LEAVES,
         new Main.LeafData(Particles.SPRUCE_LEAF, new Color(FoliageColor.FOLIAGE_EVERGREEN)),
         Blocks.JUNGLE_LEAVES,
         new Main.LeafData(Particles.JUNGLE_LEAF),
         Blocks.ACACIA_LEAVES,
         new Main.LeafData(Particles.ACACIA_LEAF),
         Blocks.DARK_OAK_LEAVES,
         new Main.LeafData(Particles.DARK_OAK_LEAF),
         Blocks.AZALEA_LEAVES,
         new Main.LeafData(Particles.AZALEA_LEAF, Color.white),
         Blocks.FLOWERING_AZALEA_LEAVES,
         new Main.LeafData(Particles.AZALEA_LEAF, Color.white),
         Blocks.MANGROVE_LEAVES,
         new Main.LeafData(Particles.MANGROVE_LEAF),
         Blocks.CHERRY_LEAVES,
         new Main.LeafData(null)
      )
   );

   public void onInitializeClient() {
      LOGGER.info("I am quite particular about the effects I choose to add :3");
      Particles.register();
      if (FabricLoader.getInstance().isModLoaded("traverse")) {
         Traverse.addLeaves();
      }

      if (FabricLoader.getInstance().isModLoaded("regions_unexplored")) {
         RegionsUnexplored.addLeaves();
      }

      if (FabricLoader.getInstance().isModLoaded("wilderwild")) {
         WilderWild.addLeaves();
      }

      ClientChunkEvents.CHUNK_LOAD.register((Load)(world, chunk) -> {
         if (CONFIG.cascades()) {
            Identifier newDimension = world.dimension().identifier();
            if (newDimension != currentDimension) {
               currentDimension = newDimension;
               cascades.clear();
            }

            chunk.findBlocks(state -> state.getFluidState().is(Fluids.WATER), (pos, state) -> updateCascade(world, pos, state.getFluidState()));
         }
      });
      ClientChunkEvents.CHUNK_UNLOAD.register((Unload)(world, chunk) -> {
         if (CONFIG.cascades()) {
            cascades.forEach((pos, strength) -> {
               if (world.getChunk(pos).getPos().equals(chunk.getPos())) {
                  cascades.remove(pos);
               }
            });
         }
      });
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> cascades.clear());
      ClientTickEvents.START_LEVEL_TICK.register((StartLevelTick)world -> {
         RandomSource random = world.getRandom();
         if (world.getOverworldClockTime() == CONFIG.fireflySettings.startTime()) {
            fireflyFrequency = CONFIG.fireflySettings.dailyRandom().get(random.nextInt(CONFIG.fireflySettings.dailyRandom().size()));
         }

         if (CONFIG.cascades()) {
            cascades.forEach((pos, strength) -> {
               float height = world.getFluidState(pos.above()).getOwnHeight();
               double x = pos.getX();
               double y = pos.getY() + random.nextDouble() * height + 1.0;
               double z = pos.getZ();
               if (random.nextBoolean()) {
                  x += random.nextDouble();
                  z += random.nextIntBetweenInclusive(0, 1);
               } else {
                  x += random.nextIntBetweenInclusive(0, 1);
                  z += random.nextDouble();
               }

               Particle cascade = Minecraft.getInstance().particleEngine.createParticle(Particles.CASCADE, x, y, z, 0.0, 0.0, 0.0);
               if (cascade != null) {
                  float size = strength.intValue() / 4.0F * height;
                  cascade.scale(1.0F - (1.0F - size) / 2.0F);
               }
            });
         }
      });
   }

   public static void registerLeafData(Block block, Main.LeafData leafData) {
      leavesData.put(block, leafData);
   }

   public static void registerLeafData(Identifier id, Main.LeafData leafData) {
      BuiltInRegistries.BLOCK.getOptional(id).ifPresent(block -> leavesData.put(block, leafData));
   }

   public static Main.LeafData getLeafData(Block block) {
      return leavesData.getOrDefault(block, new Main.LeafData(Particles.OAK_LEAF));
   }

   public static void updateCascade(Level world, BlockPos pos, FluidState state) {
      if (state.is(Fluids.WATER)
         && world.getFluidState(pos.above()).is(Fluids.FLOWING_WATER)
         && world.getFluidState(pos.below()).is(Fluids.WATER)) {
         int strength = 0;
         if (world.getFluidState(pos.north()).is(Fluids.WATER)) {
            strength++;
         }

         if (world.getFluidState(pos.east()).is(Fluids.WATER)) {
            strength++;
         }

         if (world.getFluidState(pos.south()).is(Fluids.WATER)) {
            strength++;
         }

         if (world.getFluidState(pos.west()).is(Fluids.WATER)) {
            strength++;
         }

         if (strength > 0) {
            if (!world.getBlockState(pos.above().north()).isAir()
               && !world.getBlockState(pos.above().east()).isAir()
               && !world.getBlockState(pos.above().south()).isAir()
               && !world.getBlockState(pos.above().west()).isAir()) {
               return;
            }

            pos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
            if (cascades.contains(pos)) {
               cascades.replace(pos, strength);
            } else {
               cascades.put(pos, strength);
            }
         } else {
            cascades.remove(pos);
         }
      } else {
         cascades.remove(pos);
      }
   }

   public static void spawnBubble(ParticleOptions particle, Level world, BlockPos pos) {
      double x = pos.getX() + 0.25 + world.getRandom().nextDouble() * 0.5;
      double y = pos.getY() + 0.25 + world.getRandom().nextDouble() * 0.5;
      double z = pos.getZ() + 0.25 + world.getRandom().nextDouble() * 0.5;
      world.addParticle(particle, x, y, z, 0.0, 0.0, 0.0);
   }

   public static void spawnFirefly(Level world, BlockPos pos, RandomSource random) {
      if (!(random.nextDouble() > fireflyFrequency)) {
         Biome biome = world.getBiome(pos).value();
         float downfall = ((AccessorBiome)(Object)biome).getWeather().downfall();
         if ((!world.isRaining() || CONFIG.fireflySettings.canSpawnInRain()) && random.nextInt(30 - (int)(10.0F * downfall)) == 0) {
            long time = world.getOverworldClockTime();
            float temp = biome.getBaseTemperature();
            if (time >= CONFIG.fireflySettings.startTime()
               && time <= CONFIG.fireflySettings.endTime()
               && temp >= CONFIG.fireflySettings.minTemp()
               && temp <= CONFIG.fireflySettings.maxTemp()) {
               world.addParticle(Particles.FIREFLY, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   public static void spawnDoubleBubbles(ParticleOptions particle, Level world, BlockPos pos, BlockState state) {
      ChestType chestType = (ChestType)state.getValue(BlockStateProperties.CHEST_TYPE);
      int xLen = 0;
      int zLen = 0;
      int xOffset = 0;
      int zOffset = 0;
      switch ((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
         case NORTH:
            xLen = 1;
            if (chestType == ChestType.RIGHT) {
               xOffset = -1;
            }
            break;
         case SOUTH:
            xLen = 1;
            if (chestType == ChestType.LEFT) {
               xOffset = -1;
            }
            break;
         case EAST:
            zLen = 1;
            if (chestType == ChestType.RIGHT) {
               zOffset = -1;
            }
            break;
         case WEST:
            zLen = 1;
            if (chestType == ChestType.LEFT) {
               zOffset = -1;
            }
      }

      for (int i = 0; i < 2; i++) {
         double x = pos.getX() + 0.25 + world.getRandom().nextDouble() * (0.5 + xLen) + xOffset;
         double y = pos.getY() + 0.25 + world.getRandom().nextDouble() * 0.5;
         double z = pos.getZ() + 0.25 + world.getRandom().nextDouble() * (0.5 + zLen) + zOffset;
         world.addParticle(particle, x, y, z, 0.0, 0.0, 0.0);
      }
   }

   public static void spawnChestBubbles(ParticleOptions particle, Level world, BlockPos pos) {
      for (int i = 0; i < 10; i++) {
         spawnBubble(particle, world, pos);
      }
   }

   public static void spawnDoubleChestBubbles(ParticleOptions particle, Level world, BlockPos pos, BlockState state) {
      ChestType chestType = (ChestType)state.getValue(BlockStateProperties.CHEST_TYPE);
      int xLen = 0;
      int zLen = 0;
      int xOffset = 0;
      int zOffset = 0;
      switch ((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
         case NORTH:
            xLen = 1;
            if (chestType == ChestType.RIGHT) {
               xOffset = -1;
            }
            break;
         case SOUTH:
            xLen = 1;
            if (chestType == ChestType.LEFT) {
               xOffset = -1;
            }
            break;
         case EAST:
            zLen = 1;
            if (chestType == ChestType.RIGHT) {
               zOffset = -1;
            }
            break;
         case WEST:
            zLen = 1;
            if (chestType == ChestType.LEFT) {
               zOffset = -1;
            }
      }

      for (int i = 0; i < 20; i++) {
         double x = pos.getX() + 0.25 + world.getRandom().nextDouble() * (0.5 + xLen) + xOffset;
         double y = pos.getY() + 0.25 + world.getRandom().nextDouble() * 0.5;
         double z = pos.getZ() + 0.25 + world.getRandom().nextDouble() * (0.5 + zLen) + zOffset;
         world.addParticle(particle, x, y, z, 0.0, 0.0, 0.0);
      }
   }

   public static class LeafData {
      private final ParticleOptions particle;
      private final BiFunction<net.minecraft.world.level.Level, net.minecraft.core.BlockPos, Color> colorBiFunc;

      public LeafData(
         net.minecraft.core.particles.ParticleOptions particle,
         BiFunction<net.minecraft.world.level.Level, net.minecraft.core.BlockPos, Color> colorBiFunc
      ) {
         this.particle = particle;
         this.colorBiFunc = colorBiFunc;
      }

      public LeafData(ParticleOptions particle, Color color) {
         this(particle, (world, pos) -> color);
      }

      public LeafData(ParticleOptions particle) {
         this(particle, (world, pos) -> new Color(BiomeColors.getAverageFoliageColor((net.minecraft.client.renderer.block.BlockAndTintGetter) world, pos)));
      }

      public ParticleOptions getParticle() {
         return this.particle;
      }

      public Color getColor(Level world, BlockPos pos) {
         return this.colorBiFunc.apply(world, pos);
      }
   }
}
