package net.libz.util;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.LibzClient;
import net.libz.api.InventoryTab;
import net.libz.api.Tab;
import net.libz.init.ConfigInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class DrawTabHelper {
   public static void drawTab(Minecraft client, GuiGraphicsExtractor context, Screen screenClass, int x, int y, int mouseX, int mouseY) {
      if (client != null && client.player != null && ConfigInit.CONFIG.inventoryButton && screenClass instanceof Tab) {
         int xPos = x;
         Component shownTooltip = null;
         List<InventoryTab> list = null;
         if (((Tab)screenClass).getParentScreenClass() != null) {
            if (LibzClient.otherTabs.isEmpty() || !LibzClient.otherTabs.containsKey(((Tab)screenClass).getParentScreenClass())) {
               return;
            }

            list = LibzClient.otherTabs.get(((Tab)screenClass).getParentScreenClass());
         } else {
            list = LibzClient.inventoryTabs;
         }

         if (list != null) {
            for (int i = 0; i < list.size(); i++) {
               InventoryTab inventoryTab = list.get(i);
               if (inventoryTab.shouldShow(client)) {
                  boolean isFirstTab = i == 0;
                  boolean isSelectedTab = inventoryTab.isSelectedScreen(screenClass.getClass());
                  int textureX = isFirstTab ? 24 : 72;
                  if (isSelectedTab) {
                     textureX -= 24;
                  }

                  context.blit(
                     RenderPipelines.GUI_TEXTURED, LibzClient.tabTexture, xPos, isSelectedTab ? y - 23 : y - 21, textureX, 0, 24, isSelectedTab ? 27 : (isFirstTab ? 25 : 21), 256, 256
                  );
                  if (inventoryTab.getTexture() != null) {
                     context.blit(RenderPipelines.GUI_TEXTURED, inventoryTab.getTexture(), xPos + 5, y - 16, 0.0F, 0.0F, 14, 14, 14, 14);
                  } else if (inventoryTab.getItemStack(client) != null) {
                     context.item(inventoryTab.getItemStack(client), xPos + 4, y - 17);
                  }

                  if (!isSelectedTab && isPointWithinBounds(x, y, xPos - x + 1, -20, 22, 19, mouseX, mouseY)) {
                     shownTooltip = inventoryTab.getTitle();
                  }

                  xPos += 25;
               }
            }
         }

         if (shownTooltip != null) {
            context.setTooltipForNextFrame(client.font, shownTooltip, mouseX, mouseY);
         }
      }
   }

   public static void onTabButtonClick(Minecraft client, Screen screenClass, int x, int y, double mouseX, double mouseY, boolean focused) {
      if (client != null && ConfigInit.CONFIG.inventoryButton && !focused && screenClass instanceof Tab) {
         int xPos = x;
         List<InventoryTab> list = null;
         if (((Tab)screenClass).getParentScreenClass() != null) {
            if (LibzClient.otherTabs.isEmpty() || !LibzClient.otherTabs.containsKey(((Tab)screenClass).getParentScreenClass())) {
               return;
            }

            list = LibzClient.otherTabs.get(((Tab)screenClass).getParentScreenClass());
         } else {
            list = LibzClient.inventoryTabs;
         }

         if (list != null) {
            for (int i = 0; i < list.size(); i++) {
               InventoryTab inventoryTab = list.get(i);
               if (inventoryTab.shouldShow(client)) {
                  boolean isSelectedTab = inventoryTab.isSelectedScreen(screenClass.getClass());
                  if (inventoryTab.canClick(screenClass.getClass(), client)
                     && isPointWithinBounds(x, y, xPos - x + 1, isSelectedTab ? -24 : -20, 22, isSelectedTab ? 23 : 19, mouseX, mouseY)) {
                     inventoryTab.onClick(client);
                  }

                  xPos += 25;
               }
            }
         }
      }
   }

   private static boolean isPointWithinBounds(int xPos, int yPos, int x, int y, int width, int height, double pointX, double pointY) {
      double var10;
      double var11;
      return (var10 = pointX - xPos) >= x - 1 && var10 < x + width + 1 && (var11 = pointY - yPos) >= y - 1 && var11 < y + height + 1;
   }
}
