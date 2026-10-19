package io.github.phantomloader.library.events;

import java.util.function.Supplier;

public interface RegisterBlockEntityRenderersEvent {
   <T extends net.minecraft.world.level.block.entity.BlockEntity> void register(
      net.minecraft.world.level.block.entity.BlockEntityType<? extends T> var1,
      net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider var2
   );

   default void registerItemRenderer(Supplier<? extends net.minecraft.world.level.block.Block> block) {
   }
}
