package com.starfish_studios.another_furniture.integration;

import com.starfish_studios.another_furniture.integration.fabric.IntegrationHandlerImpl;

public class IntegrationHandler {
   private IntegrationHandler() {
   }
   public static void init() {
      IntegrationHandlerImpl.init();
   }
}
