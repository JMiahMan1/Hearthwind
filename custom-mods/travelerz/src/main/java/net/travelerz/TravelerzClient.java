package net.travelerz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.travelerz.network.TravelerzClientPacket;

@Environment(EnvType.CLIENT)
public class TravelerzClient implements ClientModInitializer {
   public void onInitializeClient() {
      TravelerzClientPacket.init();
   }
}
