package io.github.phantomloader.library.fabric;

import io.github.phantomloader.library.events.ModEventHandler;
import java.util.ServiceLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class FabricInitializer implements ModInitializer {
   public void onInitialize() {
      for (ModEventHandler handler : ServiceLoader.load(ModEventHandler.class)) {
         handler.registerEntityAttributes(FabricDefaultAttributeRegistry::register);
      }
   }
}
