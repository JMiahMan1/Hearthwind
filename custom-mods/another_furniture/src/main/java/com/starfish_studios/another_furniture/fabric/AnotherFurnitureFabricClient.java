package com.starfish_studios.another_furniture.fabric;

import com.starfish_studios.another_furniture.client.AnotherFurnitureClient;
import com.starfish_studios.another_furniture.client.renderer.blockentity.ServiceBellButtonRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public class AnotherFurnitureFabricClient implements ClientModInitializer {
   public void onInitializeClient() {
      AnotherFurnitureClient.init();
      ModelLayerRegistry.registerModelLayer(ServiceBellButtonRenderer.SERVICE_BELL_MODEL, ServiceBellButtonRenderer::createBodyLayer);
   }
}
