package net.smitherz.network.packet;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record ScreenPacket(int screenId, int mouseX, int mouseY) implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ScreenPacket> PACKET_ID = new Type(
      Identifier.fromNamespaceAndPath("smitherz", "screen_packet")
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ScreenPacket> PACKET_CODEC = StreamCodec.ofMember(
      (value, buf) -> {
         buf.writeInt(value.screenId());
         buf.writeInt(value.mouseX());
         buf.writeInt(value.mouseY());
      }, buf -> new ScreenPacket(buf.readInt(), buf.readInt(), buf.readInt())
   );

   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return PACKET_ID;
   }
}
