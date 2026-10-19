package io.github.phantomloader.library.events;

import java.util.function.BiConsumer;

public interface ModEventHandler {
   default void registerEntityAttributes(
      BiConsumer<net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity>, net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder> event
   ) {
   }
}
