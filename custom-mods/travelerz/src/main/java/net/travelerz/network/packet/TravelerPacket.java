package net.travelerz.network.packet;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record TravelerPacket(int mobLevel) implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<TravelerPacket> PACKET_ID = new Type(
      Identifier.fromNamespaceAndPath("travelerz", "level_packet")
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, TravelerPacket> PACKET_CODEC = StreamCodec.ofMember(
      (value, buf) -> buf.writeInt(value.mobLevel), buf -> new TravelerPacket(buf.readInt())
   );

   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return PACKET_ID;
   }
}
