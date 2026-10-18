package net.jmb19905.niftycarts.network.clientbound;

import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record UpdateDrawnPayload(int pullingId, int cartId) implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<UpdateDrawnPayload> TYPE = CustomPacketPayload.createType(
      "niftycarts_update_drawn"
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, UpdateDrawnPayload> CODEC = new net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, UpdateDrawnPayload>() {
      @NotNull
      public UpdateDrawnPayload decode(FriendlyByteBuf buf) {
         int pullingId = buf.readVarInt();
         int cartId = buf.readVarInt();
         return new UpdateDrawnPayload(pullingId, cartId);
      }

      public void encode(FriendlyByteBuf buf, UpdateDrawnPayload payload) {
         buf.writeVarInt(payload.pullingId);
         buf.writeVarInt(payload.cartId);
      }
   };

   @NotNull
   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(UpdateDrawnPayload msg, Level level) {
      if (level.getEntity(msg.cartId) instanceof AbstractDrawnEntity drawn) {
         if (msg.pullingId < 0) {
            drawn.setPulling(null);
         } else {
            drawn.setPulling(level.getEntity(msg.pullingId));
         }
      }
   }
}
