package net.medievalweapons.network.packet;

import net.medievalweapons.MedievalMain;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public record AttackPacket(int entityId) implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<AttackPacket> PACKET_ID = new Type(
      MedievalMain.identifierOf("attack_packet")
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, AttackPacket> PACKET_CODEC = StreamCodec.ofMember(
      (value, buf) -> buf.writeInt(value.entityId), buf -> new AttackPacket(buf.readInt())
   );

   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return PACKET_ID;
   }
}
