package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.Setting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;

public class ConfirmedSetting extends Setting {
   protected final ConfirmedSetting.Event event;

   public ConfirmedSetting(Setting setting, ConfirmedSetting.Event event) {
      super(setting.getField(), setting.getValue());
      this.event = event;
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      super.encode(buffer);
      this.event.encode(buffer);
   }

   public static ConfirmedSetting decode(FriendlyByteBuf buffer) {
      Setting setting = Setting.decode(buffer);
      ConfirmedSetting.Event event = ConfirmedSetting.Event.decode(buffer);
      return new ConfirmedSetting(setting, event);
   }

   public static enum Event {
      ACCEPT {
         @Override
         public void run(ConfirmedSetting setting) {
            if (Minecraft.getInstance().gui.screen() == null) {
               String fieldName = setting.getField().getFancyName();
               String valueName = setting.getField().getValueName(setting.getValue());
               if ((Boolean)ConfigHandler.CLIENT.showFeedbackMessages.get()) {
                  TreeChop.showText(String.format("%s %s", fieldName, valueName));
               }
            }
         }
      },
      DENY {
         @Override
         public void run(ConfirmedSetting setting) {
            if (Minecraft.getInstance().gui.screen() == null) {
               String fieldName = setting.getField().getFancyName();
               String valueName = setting.getField().getValueName(setting.getValue());
               if ((Boolean)ConfigHandler.CLIENT.showFeedbackMessages.get()) {
                  TreeChop.showText(
                     String.format(
                        "%s %s %s(%s)",
                        fieldName,
                        valueName,
                        ChatFormatting.RED,
                        I18n.get("treechop.setting.missing_permissions", new Object[0])
                     )
                  );
               }
            }
         }
      },
      SILENT;

      private static final ConfirmedSetting.Event[] values = values();

      public void run(ConfirmedSetting message) {
      }

      public static ConfirmedSetting.Event decode(FriendlyByteBuf buffer) {
         int ordinal = buffer.readByte() % values.length;
         return values[ordinal];
      }

      public void encode(FriendlyByteBuf buffer) {
         buffer.writeByte(this.ordinal());
      }
   }
}
