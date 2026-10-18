package net.medievalweapons;

import net.fabricmc.api.ClientModInitializer;
import net.medievalweapons.client.OffhandItemModel;
import net.medievalweapons.init.RenderInit;
import net.medievalweapons.network.MedievalClientPacket;

public class MedievalClient implements ClientModInitializer {
   public void onInitializeClient() {
      OffhandItemModel.init();
      RenderInit.init();
      MedievalClientPacket.init();
   }
}
