package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ServerConfirmSettingsPacket implements CustomPacketPayload {
   public static final Identifier ID = TreeChop.resource("server_confirm_settings");
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ServerConfirmSettingsPacket> TYPE = new Type(ID);
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerConfirmSettingsPacket> STREAM_CODEC = CustomPacketPayload.codec(
      ServerConfirmSettingsPacket::encode, ServerConfirmSettingsPacket::decode
   );
   private final List<ConfirmedSetting> settings;

   public ServerConfirmSettingsPacket(List<ConfirmedSetting> settings) {
      this.settings = settings;
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeInt(this.settings.size());
      this.settings.forEach(setting -> setting.encode(buffer));
   }

   public static ServerConfirmSettingsPacket decode(FriendlyByteBuf buffer) {
      int numSettings = buffer.readInt();
      List<ConfirmedSetting> settings = IntStream.range(0, numSettings).mapToObj($ -> ConfirmedSetting.decode(buffer)).collect(Collectors.toList());
      return new ServerConfirmSettingsPacket(settings);
   }

   public void handle() {
      this.settings.forEach(ServerConfirmSettingsPacket::processSingleSetting);
   }

   private static void processSingleSetting(ConfirmedSetting setting) {
      Client.getChopSettings().accept(setting.getField(), setting.getValue());
      setting.event.run(setting);
   }

   @NotNull
   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return TYPE;
   }
}
