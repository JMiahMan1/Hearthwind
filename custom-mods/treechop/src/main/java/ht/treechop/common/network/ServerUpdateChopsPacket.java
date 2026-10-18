package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ServerUpdateChopsPacket implements CustomPacketPayload {
   public static final Identifier ID = TreeChop.resource("server_update_chops");
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ServerUpdateChopsPacket> TYPE = new Type(ID);
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerUpdateChopsPacket> STREAM_CODEC = CustomPacketPayload.codec(
      ServerUpdateChopsPacket::encode, ServerUpdateChopsPacket::decode
   );
   private final BlockPos pos;
   private final CompoundTag tag;

   public ServerUpdateChopsPacket(BlockPos pos, CompoundTag tag) {
      this.pos = pos;
      this.tag = tag;
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeBlockPos(this.pos);
      buffer.writeNbt(this.tag);
   }

   public static ServerUpdateChopsPacket decode(FriendlyByteBuf buffer) {
      BlockPos pos = buffer.readBlockPos();
      CompoundTag tag = buffer.readNbt();
      return new ServerUpdateChopsPacket(pos, tag);
   }

   public void handle() {
      Client.handleUpdateChopsPacket(this.pos, this.tag);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public CompoundTag getTag() {
      return this.tag;
   }

   @NotNull
   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return TYPE;
   }
}
