package com.github.legoatoom.connectiblechains.networking.packet;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class Payloads {
   public static void init() {
      ConnectibleChains.LOGGER.info("Register Custom Payloads for Networking.");
      PayloadTypeRegistry.clientboundPlay().register(ChainAttachS2CPacket.PAYLOAD_ID, ChainAttachS2CPacket.PACKET_CODEC);
      PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.PAYLOAD_ID, ConfigSyncPayload.PACKET_CODEC);
   }
}
