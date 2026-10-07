package com.github.legoatoom.connectiblechains.networking.packet;

import com.github.legoatoom.connectiblechains.entity.Chainable;
import com.github.legoatoom.connectiblechains.util.Helper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public record ChainAttachS2CPacket(int attachedEntityId, int oldHoldingEntityId, int newHoldingEntityId, int chainTypeId) implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ChainAttachS2CPacket> PAYLOAD_ID = new Type(
      Helper.identifier("s2c_chain_attach_packet_id")
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ChainAttachS2CPacket> PACKET_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      ChainAttachS2CPacket::attachedEntityId,
      ByteBufCodecs.INT,
      ChainAttachS2CPacket::oldHoldingEntityId,
      ByteBufCodecs.INT,
      ChainAttachS2CPacket::newHoldingEntityId,
      ByteBufCodecs.INT,
      ChainAttachS2CPacket::chainTypeId,
      ChainAttachS2CPacket::new
   );

   public ChainAttachS2CPacket(Entity attachedEntity, @Nullable Entity oldHoldingEntity, @Nullable Entity newHoldingEntity, Item souceItem) {
      this(
         attachedEntity.getId(),
         oldHoldingEntity != null ? oldHoldingEntity.getId() : 0,
         newHoldingEntity != null ? newHoldingEntity.getId() : 0,
         BuiltInRegistries.ITEM.getId(souceItem)
      );
   }

   public ChainAttachS2CPacket(FriendlyByteBuf buf) {
      this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
   }

   public static void encode(FriendlyByteBuf buf1, ChainAttachS2CPacket packet) {
      buf1.writeInt(packet.attachedEntityId);
      buf1.writeInt(packet.oldHoldingEntityId);
      buf1.writeInt(packet.newHoldingEntityId);
      buf1.writeInt(packet.chainTypeId);
   }

   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ChainAttachS2CPacket> type() {
      return PAYLOAD_ID;
   }

   @Environment(EnvType.CLIENT)
   public void apply(Context context) {
      if (context.player().level().getEntity(this.attachedEntityId()) instanceof Chainable chainable) {
         chainable.addUnresolvedChainHolderId(
            this.oldHoldingEntityId(), this.newHoldingEntityId(), (Item)BuiltInRegistries.ITEM.byId(this.chainTypeId())
         );
      }
   }

   public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.common.ClientCommonPacketListener> asPacket() {
      return ServerPlayNetworking.createClientboundPacket(this);
   }
}
