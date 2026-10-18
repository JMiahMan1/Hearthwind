package net.libz.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class LibzServerPacket {
   public static final CustomPacketPayload.Type<SetMousePositionPayload> SET_MOUSE_POSITION = new CustomPacketPayload.Type<>(
      Identifier.fromNamespaceAndPath("libz", "set_mouse_position")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, SetMousePositionPayload> PACKET_CODEC = StreamCodec.ofMember(
      (value, buf) -> {
         buf.writeInt(value.mouseX());
         buf.writeInt(value.mouseY());
      },
      buf -> new SetMousePositionPayload(buf.readInt(), buf.readInt())
   );

   public static void init() {
      PayloadTypeRegistry.clientboundPlay().register(SET_MOUSE_POSITION, PACKET_CODEC);
   }

   public static void writeS2CMousePositionPacket(ServerPlayer serverPlayerEntity, int mouseX, int mouseY) {
      ServerPlayNetworking.send(serverPlayerEntity, new SetMousePositionPayload(mouseX, mouseY));
   }

   public record SetMousePositionPayload(int mouseX, int mouseY) implements CustomPacketPayload {
      @Override
      public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
         return SET_MOUSE_POSITION;
      }
   }
}
