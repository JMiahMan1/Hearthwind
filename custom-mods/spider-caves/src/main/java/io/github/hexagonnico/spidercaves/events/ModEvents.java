package io.github.hexagonnico.spidercaves.events;

import io.github.hexagonnico.spidercaves.SpiderCaves;
import io.github.hexagonnico.spidercaves.entities.BlackRecluse;
import io.github.phantomloader.library.events.ModEventHandler;
import java.util.function.BiConsumer;

public class ModEvents implements ModEventHandler {
   public void registerEntityAttributes(
      BiConsumer<net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity>, net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder> event
   ) {
      event.accept(SpiderCaves.BLACK_RECLUSE.get(), BlackRecluse.createAttributes());
   }
}
