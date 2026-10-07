package com.github.legoatoom.connectiblechains.networking.packet;

import com.github.legoatoom.connectiblechains.util.Helper;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

@Deprecated
public record MultiChainAttachPayload(List<ChainAttachS2CPacket> packets) implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<MultiChainAttachPayload> PAYLOAD_ID = new Type(
      Helper.identifier("s2c_multi_chain_attach_packet_id")
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, MultiChainAttachPayload> PACKET_CODEC = StreamCodec.ofMember(
      MultiChainAttachPayload::encode, MultiChainAttachPayload::decode
   );

   private static MultiChainAttachPayload decode(RegistryFriendlyByteBuf buf) {
      return new MultiChainAttachPayload(buf.readList(ChainAttachS2CPacket::new));
   }

   private static void encode(MultiChainAttachPayload packet, RegistryFriendlyByteBuf buf) {
      buf.writeCollection(packet.packets, ChainAttachS2CPacket::encode);
   }

   @Environment(EnvType.CLIENT)
   public void apply(Context context) {
      this.packets.forEach(packet -> packet.apply(context));
   }

   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<MultiChainAttachPayload> type() {
      return PAYLOAD_ID;
   }
}
