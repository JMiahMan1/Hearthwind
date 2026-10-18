package straywave.minecraft.immersivesnow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmersiveSnow {
   public static final String MOD_ID = "immersivesnow";
   public static Logger LOGGER = LogManager.getLogger("immersivesnow");

   public static void init() {
      Configuration.load();
   }
}
