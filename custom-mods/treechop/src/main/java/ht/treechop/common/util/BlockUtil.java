package ht.treechop.common.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockUtil {
   public static BlockState copyStateProperties(BlockState blockState, BlockState stateToCopyFrom) {
      for (Property property : stateToCopyFrom.getProperties()) {
         if (blockState.hasProperty(property)) {
            blockState = (BlockState)blockState.setValue(property, stateToCopyFrom.getValue(property));
         }
      }

      return blockState;
   }
}
