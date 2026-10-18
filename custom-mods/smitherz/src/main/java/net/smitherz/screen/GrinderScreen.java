package net.smitherz.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.smitherz.SmitherzMain;

@Environment(EnvType.CLIENT)
public class GrinderScreen
   extends net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<GrinderScreenHandler>
   implements net.minecraft.world.inventory.ContainerListener,
   Tab {
   public static final Identifier TEXTURE = SmitherzMain.identifierOf("textures/gui/grinder_screen.png");

   public GrinderScreen(GrinderScreenHandler handler, Inventory playerInventory, Component title) {
      super(handler, playerInventory, title);
   }

   protected void init() {
      super.init();
      ((GrinderScreenHandler)this.menu).addSlotListener(this);
   }

   public void removed() {
      super.removed();
      ((GrinderScreenHandler)this.menu).removeSlotListener(this);
   }

   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      super.extractRenderState(context, mouseX, mouseY, delta);
      this.extractTooltip(context, mouseX, mouseY);
   }

   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
      if ((((GrinderScreenHandler)this.menu).getSlot(0).hasItem() || ((GrinderScreenHandler)this.menu).getSlot(1).hasItem())
         && !((GrinderScreenHandler)this.menu).getSlot(2).hasItem()) {
         context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 92, j + 31, (float)this.imageWidth, 0.0F, 28, 21, 256, 256);
      }
   }

   public Class<?> getParentScreenClass() {
      return GrindstoneScreen.class;
   }

   public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
   }

   public void dataChanged(AbstractContainerMenu handler, int property, int value) {
   }
}
