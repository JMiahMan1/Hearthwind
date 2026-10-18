package net.jmb19905.niftycarts.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.container.SeedDrillMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class SeedDrillScreen extends net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<SeedDrillMenu> {
   private static final Identifier SEED_DRILL_GUI_TEXTURES = NiftyCarts.resLoc("textures/gui/container/seed_drill.png");

   public SeedDrillScreen(SeedDrillMenu abstractContainerMenu, Inventory inventory, Component component) {
      super(abstractContainerMenu, inventory, component);
   }

   public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SEED_DRILL_GUI_TEXTURES, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
   }
}
