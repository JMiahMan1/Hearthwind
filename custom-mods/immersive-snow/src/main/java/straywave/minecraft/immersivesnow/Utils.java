package straywave.minecraft.immersivesnow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.state.BlockState;

public class Utils {
   public static void setBlock(LevelWriter level, BlockPos pos, BlockState state) {
      level.setBlock(pos, state, 3, 1);
   }
}
