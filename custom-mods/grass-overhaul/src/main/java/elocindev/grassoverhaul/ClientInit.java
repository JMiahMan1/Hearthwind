package elocindev.grassoverhaul;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {
   public void onInitializeClient() {
      GOBlocks.colorreg();
   }
}
