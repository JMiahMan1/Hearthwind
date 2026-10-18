package net.nameplate.network.packet;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record LevelPacket(int mobLevel, int mobId, boolean hasRpgLabel) implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<LevelPacket> PACKET_ID = new Type(
      Identifier.fromNamespaceAndPath("nameplate", "level_packet")
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, LevelPacket> PACKET_CODEC = StreamCodec.ofMember(
      (value, buf) -> {
         buf.writeInt(value.mobLevel);
         buf.writeInt(value.mobId);
         buf.writeBoolean(value.hasRpgLabel);
      }, buf -> new LevelPacket(buf.readInt(), buf.readInt(), buf.readBoolean())
   );

   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return PACKET_ID;
   }
}
