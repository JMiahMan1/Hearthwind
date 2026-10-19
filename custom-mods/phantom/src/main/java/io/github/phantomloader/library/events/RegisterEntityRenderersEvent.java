package io.github.phantomloader.library.events;

public interface RegisterEntityRenderersEvent {
   <T extends net.minecraft.world.entity.Entity> void register(
      net.minecraft.world.entity.EntityType<? extends T> var1, net.minecraft.client.renderer.entity.EntityRendererProvider<T> var2
   );
}
