package straywave.minecraft.immersivesnow;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class Logic {
   public static void processChunk(ServerLevel level, ChunkPos chunkPos) {
      for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
         for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
            BlockPos column = new BlockPos(x, 0, z);
            BlockPos motionBlockingPos = level.getHeightmapPos(Types.MOTION_BLOCKING, column);
            checkAndUpdateBlock(level, motionBlockingPos);
            BlockPos noLeavesPos = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, column);
            if (motionBlockingPos != noLeavesPos) {
               checkAndUpdateBlock(level, noLeavesPos);
            }
         }
      }
   }

   public static void checkAndUpdateBlock(ServerLevel level, BlockPos topPos) {
      BlockPos blockPos = topPos.below();
      BlockState topState = level.getBlockState(topPos);
      BlockState blockState = level.getBlockState(blockPos);
      Biome biome = level.getBiome(topPos).value();
      if (biome.shouldSnow(level, topPos) && !topState.is(Blocks.SNOW) && Blocks.SNOW.defaultBlockState().canSurvive(level, topPos)) {
         Utils.setBlock(level, topPos, Blocks.SNOW.defaultBlockState());
      } else if (biome.shouldFreeze(level, blockPos, false) && !blockState.is(Blocks.ICE)) {
         Utils.setBlock(level, blockPos, Blocks.ICE.defaultBlockState());
      } else if (blockState.is(Blocks.ICE) && shouldMelt(level, biome, topPos)) {
         Utils.setBlock(level, blockPos, IceBlock.meltsInto());
         level.neighborChanged(blockPos, IceBlock.meltsInto().getBlock(), null);
      } else if (topState.is(Blocks.SNOW) && shouldMelt(level, biome, topPos)) {
         Utils.setBlock(level, topPos, Blocks.AIR.defaultBlockState());
      }
   }

   private static boolean shouldMelt(Level level, Biome biome, BlockPos pos) {
      boolean brightEnough = level.getBrightness(LightLayer.BLOCK, pos) > 11;
      return biome.warmEnoughToRain(pos, level.getSeaLevel()) || brightEnough;
   }
}
