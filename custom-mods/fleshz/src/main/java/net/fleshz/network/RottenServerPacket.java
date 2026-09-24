package net.fleshz.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fleshz.network.packet.RackPacket;

public class RottenServerPacket {

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(RackPacket.TYPE, RackPacket.PACKET_CODEC);
    }
}
