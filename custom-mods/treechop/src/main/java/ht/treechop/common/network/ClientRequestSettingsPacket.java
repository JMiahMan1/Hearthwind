package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SyncedChopData;
import ht.treechop.server.Server;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ClientRequestSettingsPacket implements CustomPacketPayload {
   public static final Identifier ID = TreeChop.resource("client_request_settings");
   public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ClientRequestSettingsPacket> TYPE = new Type(ID);
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientRequestSettingsPacket> STREAM_CODEC = StreamCodec.ofMember(
      ClientRequestSettingsPacket::encode, ClientRequestSettingsPacket::decode
   );
   private final List<Setting> settings;
   private final ClientRequestSettingsPacket.Event event;

   public ClientRequestSettingsPacket(List<Setting> settings, ClientRequestSettingsPacket.Event event) {
      this.settings = settings;
      this.event = event;
   }

   public ClientRequestSettingsPacket(SettingsField field, Object value) {
      this(Collections.singletonList(new Setting(field, value)), ClientRequestSettingsPacket.Event.REQUEST);
   }

   public ClientRequestSettingsPacket(ClientChopSettings chopSettings) {
      this(chopSettings.getAll(), ClientRequestSettingsPacket.Event.FIRST_TIME_SYNC);
   }

   public void encode(FriendlyByteBuf buffer) {
      this.event.encode(buffer);
      buffer.writeInt(this.settings.size());
      this.settings.forEach(setting -> setting.encode(buffer));
   }

   public static ClientRequestSettingsPacket decode(FriendlyByteBuf buffer) {
      ClientRequestSettingsPacket.Event event = ClientRequestSettingsPacket.Event.decode(buffer);
      int numSettings = buffer.readInt();
      List<Setting> settings = IntStream.range(0, numSettings).mapToObj($ -> Setting.decode(buffer)).collect(Collectors.toList());
      return new ClientRequestSettingsPacket(settings, event);
   }

   public void handle(Player player, PacketChannel replyChannel) {
      processSettingsRequest(Server.instance().getPlayerChopData(player), this, player, replyChannel);
   }

   private static void processSettingsRequest(SyncedChopData chopData, ClientRequestSettingsPacket message, Player player, PacketChannel replyChannel) {
      List<Setting> settings = message.event == ClientRequestSettingsPacket.Event.FIRST_TIME_SYNC && chopData.isSynced()
         ? chopData.getSettings().getAll()
         : message.settings;
      List<ConfirmedSetting> confirmedSettings = settings.stream()
         .map(setting -> processSingleSettingRequest(setting, player, chopData.getSettings(), message.event))
         .collect(Collectors.toList());
      if (!chopData.isSynced()) {
         chopData.setSynced();
      }

      replyChannel.send(new ServerConfirmSettingsPacket(confirmedSettings));
      if (message.event == ClientRequestSettingsPacket.Event.FIRST_TIME_SYNC) {
         replyChannel.send(new ServerPermissionsPacket(ConfigHandler.getServerPermissions()));
      }
   }

   private static ConfirmedSetting processSingleSettingRequest(
      Setting setting, Player player, ChopSettings chopSettings, ClientRequestSettingsPacket.Event requestEvent
   ) {
      ConfirmedSetting.Event confirmEvent;
      if (playerHasPermission(player, setting)) {
         chopSettings.set(setting);
         confirmEvent = ConfirmedSetting.Event.ACCEPT;
      } else {
         Setting defaultSetting = getDefaultSetting(player, setting);
         chopSettings.set(defaultSetting);
         confirmEvent = ConfirmedSetting.Event.DENY;
      }

      if (requestEvent == ClientRequestSettingsPacket.Event.FIRST_TIME_SYNC) {
         confirmEvent = ConfirmedSetting.Event.SILENT;
      }

      SettingsField field = setting.getField();
      return new ConfirmedSetting(new Setting(field, chopSettings.get(field)), confirmEvent);
   }

   private static Setting getDefaultSetting(Player player, Setting setting) {
      return Server.getDefaultPlayerSettings().getSetting(setting.getField());
   }

   private static boolean playerHasPermission(Player player, Setting setting) {
      return ConfigHandler.getServerPermissions().isPermitted(setting);
   }

   @NotNull
   public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
      return TYPE;
   }

   public static enum Event {
      FIRST_TIME_SYNC,
      REQUEST;

      private static final ClientRequestSettingsPacket.Event[] values = values();

      public static ClientRequestSettingsPacket.Event decode(FriendlyByteBuf buffer) {
         int ordinal = buffer.readByte() % values.length;
         return values[ordinal];
      }

      public void encode(FriendlyByteBuf buffer) {
         buffer.writeByte(this.ordinal());
      }
   }
}
