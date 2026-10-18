package net.jmb19905.niftycarts.network.serverbound;

import net.jmb19905.niftycarts.entity.SupplyCartEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record OpenSupplyCartPayload() implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<OpenSupplyCartPayload> TYPE = CustomPacketPayload.createType(
      "niftycarts_open_supply_cart"
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, OpenSupplyCartPayload> CODEC = new net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, OpenSupplyCartPayload>() {
      @NotNull
      public OpenSupplyCartPayload decode(FriendlyByteBuf object) {
         return new OpenSupplyCartPayload();
      }

      public void encode(FriendlyByteBuf object, OpenSupplyCartPayload object2) {
      }
   };

   @NotNull
   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(Player player) {
      Entity ridden = player.getVehicle();
      if (ridden instanceof SupplyCartEntity) {
         ((SupplyCartEntity)ridden).openCustomInventoryScreen(player);
      }
   }
}
