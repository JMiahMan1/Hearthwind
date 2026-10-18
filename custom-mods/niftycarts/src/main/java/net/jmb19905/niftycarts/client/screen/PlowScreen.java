package net.jmb19905.niftycarts.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.container.PlowMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public final class PlowScreen extends net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<PlowMenu> {
   private static final Identifier PLOW_GUI_TEXTURES = NiftyCarts.resLoc("textures/gui/container/plow.png");

   public PlowScreen(PlowMenu screenContainer, Inventory inv, Component titleIn) {
      super(screenContainer, inv, titleIn);
   }

   public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PLOW_GUI_TEXTURES, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
   }
}
