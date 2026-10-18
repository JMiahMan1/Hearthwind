package net.smitherz.screen.widget;

import net.libz.api.InventoryTab;
import net.minecraft.client.Minecraft;
import net.smitherz.network.SmitherClientPacket;
import org.jetbrains.annotations.Nullable;

public class SmithingTab extends InventoryTab {
   public SmithingTab(
      net.minecraft.network.chat.Component title,
      @Nullable net.minecraft.resources.Identifier texture,
      int preferedPos,
      Class<?>... screenClasses
   ) {
      super(title, texture, preferedPos, screenClasses);
   }

   public void onClick(Minecraft client) {
      SmitherClientPacket.writeC2SScreenPacket((int)client.mouseHandler.xpos(), (int)client.mouseHandler.ypos(), 2);
   }
}
