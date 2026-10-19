package io.github.hexagonnico.spidercaves.blocks;

import io.github.hexagonnico.spidercaves.SpiderCaves;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ModChestBlockEntity extends ChestBlockEntity {
   public ModChestBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType)SpiderCaves.CHEST_ENTITY.get(), blockPos, blockState);
   }
}
