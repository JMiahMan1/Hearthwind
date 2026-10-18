package net.nameplate.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.nameplate.network.packet.LevelPacket;

public class NameplateServerPacket {
   public static void init() {
      PayloadTypeRegistry.clientboundPlay().register(LevelPacket.PACKET_ID, LevelPacket.PACKET_CODEC);
   }
}
