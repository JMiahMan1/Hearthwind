package io.github.phantomloader.library.fabric;

import io.github.phantomloader.library.events.ClientEventHandler;
import io.github.phantomloader.library.events.RegisterBlockEntityRenderersEvent;
import io.github.phantomloader.library.events.RegisterEntityRenderersEvent;
import io.github.phantomloader.library.events.RegisterParticlesEvent;
import io.github.phantomloader.library.utils.CreativeTabsUtils;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class FabricClientInitializer implements ClientModInitializer {
   public void onInitializeClient() {
      ServiceLoader.load(ClientEventHandler.class)
         .forEach(
            handler -> {
               CreativeTabsUtils.allTabKeys()
                  .forEach(
                     resourceKey -> handler.addItemsToCreativeTab(
                        (net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab>)resourceKey,
                        item -> CreativeModeTabEvents.modifyOutputEvent(resourceKey)
                           .register(output -> output.accept((ItemLike)item.get()))
                     )
                  );
               handler.registerBlockEntityRenderers(new FabricClientInitializer.RegisterBlockEntityRenderersEventFabric());
               handler.registerEntityRenderers(new FabricClientInitializer.RegisterEntityRenderersEventFabric());
               handler.registerBlockRenderType((block, renderType) -> { });
               handler.registerParticles(new FabricClientInitializer.RegisterParticlesEventFabric());
            }
         );
   }

   public static class RegisterBlockEntityRenderersEventFabric implements RegisterBlockEntityRenderersEvent {
      @Override
      public <T extends net.minecraft.world.level.block.entity.BlockEntity> void register(
         net.minecraft.world.level.block.entity.BlockEntityType<? extends T> blockEntity,
         net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider renderer
      ) {
         BlockEntityRenderers.register(blockEntity, renderer);
      }

      @Override
      public void registerItemRenderer(Supplier<? extends net.minecraft.world.level.block.Block> block) {
         // 26.2 removed BuiltinItemRendererRegistry; block-entity item previews use special model renderers now.
      }
   }

   public static class RegisterEntityRenderersEventFabric implements RegisterEntityRenderersEvent {
      @Override
      public <T extends net.minecraft.world.entity.Entity> void register(
         net.minecraft.world.entity.EntityType<? extends T> entity, net.minecraft.client.renderer.entity.EntityRendererProvider<T> renderer
      ) {
         EntityRendererRegistry.register(entity, renderer);
      }
   }

   public static class RegisterParticlesEventFabric implements RegisterParticlesEvent {
      @Override
      public <T extends net.minecraft.core.particles.ParticleOptions> void register(
         net.minecraft.core.particles.ParticleType<T> type, net.minecraft.client.particle.ParticleProvider<T> provider
      ) {
         ParticleProviderRegistry.getInstance().register(type, provider);
      }
   }
}
