package net.jmb19905.niftycarts.network.serverbound;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jmb19905.niftycarts.network.clientbound.UpdateDrawnPayload;
import net.jmb19905.niftycarts.util.NiftyWorld;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record RequestCartUpdatePayload(int cartId) implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<RequestCartUpdatePayload> TYPE = CustomPacketPayload.createType(
      "niftycarts_request_cart_update"
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, RequestCartUpdatePayload> CODEC = new net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, RequestCartUpdatePayload>() {
      @NotNull
      public RequestCartUpdatePayload decode(FriendlyByteBuf buf) {
         return new RequestCartUpdatePayload(buf.readVarInt());
      }

      public void encode(FriendlyByteBuf buf, RequestCartUpdatePayload payload) {
         buf.writeVarInt(payload.cartId());
      }
   };

   @NotNull
   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(RequestCartUpdatePayload msg, ServerPlayer player) {
      ServerLevel level = player.level();
      Int2ObjectMap<UUID> pulling = NiftyWorld.get(level).getPulling();
      pulling.keySet()
         .intStream()
         .filter(pullId -> NiftyWorld.get(level).getDrawn(level.getEntity(pullId)).<Integer>map(Entity::getId).orElse(-1) == msg.cartId)
         .findFirst()
         .ifPresent(pullId -> ServerPlayNetworking.send(player, new UpdateDrawnPayload(pullId, msg.cartId)));
   }
}
