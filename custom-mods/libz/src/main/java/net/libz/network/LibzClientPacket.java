package net.libz.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.libz.access.MouseAccessor;

@Environment(EnvType.CLIENT)
public class LibzClientPacket {
   public static void init() {
      ClientPlayNetworking.registerGlobalReceiver(
         LibzServerPacket.SET_MOUSE_POSITION,
         (payload, context) -> context.client().execute(() -> ((MouseAccessor)context.client().mouseHandler).setMousePosition(payload.mouseX(), payload.mouseY()))
      );
   }
}
