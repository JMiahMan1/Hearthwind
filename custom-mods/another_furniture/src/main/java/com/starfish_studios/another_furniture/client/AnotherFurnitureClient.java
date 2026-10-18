package com.starfish_studios.another_furniture.client;

import com.starfish_studios.another_furniture.client.renderer.blockentity.FlowerBoxRenderer;
import com.starfish_studios.another_furniture.client.renderer.blockentity.ServiceBellButtonRenderer;
import com.starfish_studios.another_furniture.client.renderer.blockentity.ShelfRenderer;
import com.starfish_studios.another_furniture.client.renderer.entity.SeatRenderer;
import com.starfish_studios.another_furniture.registry.AFBlockEntityTypes;
import com.starfish_studios.another_furniture.registry.AFEntityTypes;
import com.starfish_studios.another_furniture.registry.AFRegistry;

public class AnotherFurnitureClient {
   public static void init() {
      AFRegistry.registerEntityRenderers(AFEntityTypes.SEAT, SeatRenderer::new);
      AFRegistry.registerBlockEntityRenderer(AFBlockEntityTypes.SHELF, ShelfRenderer::new);
      AFRegistry.registerBlockEntityRenderer(AFBlockEntityTypes.SERVICE_BELL, ServiceBellButtonRenderer::new);
      AFRegistry.registerBlockEntityRenderer(AFBlockEntityTypes.FLOWER_BOX, FlowerBoxRenderer::new);
   }
}
