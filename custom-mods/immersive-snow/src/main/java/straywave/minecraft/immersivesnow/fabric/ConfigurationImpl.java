package straywave.minecraft.immersivesnow.fabric;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigurationImpl {
   public static Path getConfigDir() {
      return FabricLoader.getInstance().getConfigDir();
   }
}
