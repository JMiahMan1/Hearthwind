package folk.sisby.euphonium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EuphoniumClient implements ClientModInitializer {
   public static final String ID = "euphonium";
   public static final Logger LOGGER = LoggerFactory.getLogger("euphonium");
   public static EuphoniumConfig CONFIG = (EuphoniumConfig)EuphoniumConfig.createToml(
      FabricLoader.getInstance().getConfigDir(), "", "euphonium", EuphoniumConfig.class
   );

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("euphonium", path);
   }

   public void onInitializeClient() {
      EuphoniumBiome.init();
      EuphoniumWorld.init();
      LOGGER.info("[Euphonium] Initialized! Using sound channel: {}", CONFIG.channel.getName());
   }
}
