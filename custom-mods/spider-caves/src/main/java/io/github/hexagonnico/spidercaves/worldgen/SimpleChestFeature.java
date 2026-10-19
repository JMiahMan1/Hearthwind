package io.github.hexagonnico.spidercaves.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class SimpleChestFeature extends net.minecraft.world.level.levelgen.feature.Feature<SimpleChestConfiguration> {
   public SimpleChestFeature() {
      super(SimpleChestConfiguration.CODEC);
   }

   public boolean place(@NotNull net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<SimpleChestConfiguration> featurePlaceContext) {
      WorldGenLevel world = featurePlaceContext.level();
      BlockPos pos = featurePlaceContext.origin();
      if (canStayHere(world, pos)) {
         SimpleChestConfiguration configuration = (SimpleChestConfiguration)featurePlaceContext.config();
         RandomSource random = featurePlaceContext.random();
         BlockState chest = configuration.chest().getState(world, random, pos);
         world.setBlock(pos, chest, 2);
         if (world.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity blockEntity) {
            blockEntity.setLootTable(configuration.lootTable());
            blockEntity.setLootTableSeed(random.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean canStayHere(WorldGenLevel world, BlockPos pos) {
      return !world.getBlockState(pos.below()).isAir() && world.getBlockState(pos.above()).isAir() && hasAirAround(world, pos);
   }

   private static boolean hasAirAround(WorldGenLevel world, BlockPos pos) {
      return world.getBlockState(pos.north()).isAir()
         || world.getBlockState(pos.south()).isAir()
         || world.getBlockState(pos.west()).isAir()
         || world.getBlockState(pos.east()).isAir();
   }
}
