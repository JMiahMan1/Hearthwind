package io.github.hexagonnico.spidercaves.blocks;

import io.github.hexagonnico.spidercaves.SpiderCaves;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.NotNull;

public class ModChestBlock extends ChestBlock {
   public ModChestBlock(Properties properties) {
      super(() -> (BlockEntityType)SpiderCaves.CHEST_ENTITY.get(), net.minecraft.sounds.SoundEvents.CHEST_OPEN, net.minecraft.sounds.SoundEvents.CHEST_CLOSE, properties);
   }

   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new ModChestBlockEntity(blockPos, blockState);
   }
}
