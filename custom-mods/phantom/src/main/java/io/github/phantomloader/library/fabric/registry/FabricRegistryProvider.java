package io.github.phantomloader.library.fabric.registry;

import io.github.phantomloader.library.registry.ModRegistry;
import io.github.phantomloader.library.registry.RegistryProvider;

public class FabricRegistryProvider implements RegistryProvider {
   @Override
   public ModRegistry instantiate(String mod) {
      return new FabricRegistry(mod);
   }
}
