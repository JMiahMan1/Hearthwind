package net.smitherz.network.packet;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record SmithPacket() implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<SmithPacket> PACKET_ID = new Type(
      Identifier.fromNamespaceAndPath("smitherz", "smith_packet")
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, SmithPacket> PACKET_CODEC = StreamCodec.ofMember(
      (value, buf) -> {}, buf -> new SmithPacket()
   );

   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return PACKET_ID;
   }
}
