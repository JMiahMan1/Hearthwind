package com.github.legoatoom.connectiblechains.networking.packet;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.client.ClientInitializer;
import com.github.legoatoom.connectiblechains.util.Helper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public record ConfigSyncPayload(float chainHangAmount, int maxChainRange) implements CustomPacketPayload {
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ConfigSyncPayload> PAYLOAD_ID = new Type(
      Helper.identifier("s2c_config_sync_packet_id")
   );
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, ConfigSyncPayload> PACKET_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT, ConfigSyncPayload::chainHangAmount, ByteBufCodecs.INT, ConfigSyncPayload::maxChainRange, ConfigSyncPayload::new
   );

   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return PAYLOAD_ID;
   }

   public void apply(Context context) {
      Minecraft client = context.client();
      if (!client.isLocalServer()) {
         try {
            ConnectibleChains.LOGGER.info("Received {} config from server", "connectiblechains");
            ConnectibleChains.runtimeConfig.setChainHangAmount(this.chainHangAmount);
            ConnectibleChains.runtimeConfig.setMaxChainRange(this.maxChainRange);
         } catch (Exception var4) {
            ConnectibleChains.LOGGER.error("Could not deserialize config: ", var4);
         }

         ClientInitializer.getInstance().getChainKnotEntityRenderer().ifPresent(r -> r.getChainRenderer().purge());
      }
   }
}
