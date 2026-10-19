package io.github.phantomloader.library.events;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ClientEventHandler {
   default void addItemsToCreativeTab(
      net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> resourceKey,
      Consumer<Supplier<? extends net.minecraft.world.level.ItemLike>> event
   ) {
   }

   default void registerBlockEntityRenderers(RegisterBlockEntityRenderersEvent event) {
   }

   default void registerEntityRenderers(RegisterEntityRenderersEvent event) {
   }

   default void registerBlockRenderType(
      BiConsumer<Supplier<? extends net.minecraft.world.level.block.Block>, net.minecraft.client.renderer.rendertype.RenderType> event
   ) {
   }

   default void registerParticles(RegisterParticlesEvent event) {
   }
}
