package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import com.chailotl.particular.Particles;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Block.class})
public class InjectBlock {
   @Unique
   private static boolean isValidBiome(net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome) {
      Optional<net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>> key = biome.unwrapKey();
      return key.<Boolean>map(biomeRegistryKey -> !Main.CONFIG.caveDustSettings.excludeBiomes().contains(biomeRegistryKey.identifier())).orElse(true);
   }

   @Inject(
      at = {@At("TAIL")},
      method = {"randomDisplayTick"}
   )
   public void spawnParticles(BlockState state, Level world, BlockPos pos, RandomSource random, CallbackInfo ci) {
      Block block = state.getBlock();
      if (Main.CONFIG.fireflies()) {
         double val = random.nextDouble();
         if (block == Blocks.GRASS_BLOCK && val < Main.CONFIG.fireflySettings.grass()
            || block == Blocks.TALL_GRASS && val < Main.CONFIG.fireflySettings.tallGrass()
            || block instanceof FlowerBlock && val < Main.CONFIG.fireflySettings.flowers()
            || block instanceof TallFlowerBlock && val < Main.CONFIG.fireflySettings.tallFlowers()) {
            Main.spawnFirefly(world, pos, random);
            return;
         }
      }

      if (Main.CONFIG.caveDust()
         && (block == Blocks.AIR || block == Blocks.CAVE_AIR)
         && random.nextInt(Main.CONFIG.caveDustSettings.spawnChance()) == 0
         && pos.getY() < world.getSeaLevel()
         && isValidBiome(world.getBiome(pos))) {
         float lightChance = 1.0F - Math.min(8, world.getBrightness(LightLayer.SKY, pos)) / 8.0F;
         float depthChance = Math.min(1.0F, (world.getSeaLevel() - pos.getY()) / 96.0F);
         if (random.nextFloat() < lightChance * depthChance) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            world.addParticle(Particles.CAVE_DUST, x, y, z, 0.0, 0.0, 0.0);
         }
      }
   }
}
