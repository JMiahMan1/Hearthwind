package folk.sisby.euphonium.helper;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class WorldHelper {
   public static boolean isDay(Player player) {
      long dayTime = player.level().getOverworldClockTime() % 24000L;
      return dayTime >= 0L && dayTime < 12700L;
   }

   public static boolean isNight(Player player) {
      long dayTime = player.level().getOverworldClockTime() % 24000L;
      return dayTime >= 12700L;
   }

   public static boolean isThundering(Player player) {
      return player.level().isThundering();
   }

   public static boolean isOutside(Player player) {
      if (player.isUnderWater()) {
         return false;
      } else {
         int blocks = 24;
         int start = 1;
         BlockPos playerPos = player.blockPosition();
         if (player.level().canSeeSky(playerPos)) {
            return true;
         } else if (player.level().canSeeSkyFromBelowWater(playerPos)) {
            return true;
         } else {
            for (int i = start; i < start + blocks; i++) {
               BlockPos check = new BlockPos(playerPos.getX(), playerPos.getY() + i, playerPos.getZ());
               BlockState state = player.level().getBlockState(check);
               Block block = state.getBlock();
               if (!player.level().isEmptyBlock(check) && state.canOcclude()) {
                  if (player.level().canSeeSky(check)) {
                     return true;
                  }

                  if (player.level().canSeeSkyFromBelowWater(check)) {
                     return true;
                  }

                  if (state.canOcclude()) {
                     return false;
                  }
               }
            }

            return player.level().canSeeSky(playerPos.above(blocks));
         }
      }
   }

   public static float distanceFromGround(Player player, int check) {
      Level level = player.level();
      BlockPos pos = player.blockPosition();
      int playerHeight = pos.getY();
      List<net.minecraft.core.BlockPos> samples = List.of(
         pos.east(check), pos.west(check), pos.north(check), pos.south(check)
      );
      int avg = 0;

      for (BlockPos sample : samples) {
         avg += level.getHeight(Types.WORLD_SURFACE, sample.getX(), sample.getZ());
      }

      avg /= samples.size();
      return Math.max(0.0F, (float)(playerHeight - avg));
   }

   public static boolean isBelowSeaLevel(Player player) {
      return player.blockPosition().getY() < player.level().getSeaLevel();
   }

   public static double getDistanceSquared(BlockPos pos1, BlockPos pos2) {
      double d0 = pos1.getX();
      double d1 = pos1.getZ();
      double d2 = d0 - pos2.getX();
      double d3 = d1 - pos2.getZ();
      return d2 * d2 + d3 * d3;
   }
}
