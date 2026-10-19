package io.github.hexagonnico.spidercaves.fabric;

import io.github.hexagonnico.spidercaves.SpiderCaves;
import net.fabricmc.api.ModInitializer;

public class FabricInitializer implements ModInitializer {
   public void onInitialize() {
      SpiderCaves.register();
   }
}
