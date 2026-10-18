package net.smitherz.network.packet;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record SmithUpdatePacket(boolean disableButton) implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<SmithUpdatePacket> PACKET_ID = new Type(
      Identifier.fromNamespaceAndPath("smitherz", "smith_update_packet")
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, SmithUpdatePacket> PACKET_CODEC = StreamCodec.ofMember(
      (value, buf) -> buf.writeBoolean(value.disableButton()), buf -> new SmithUpdatePacket(buf.readBoolean())
   );

   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return PACKET_ID;
   }
}
