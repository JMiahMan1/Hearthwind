package net.smitherz.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.smitherz.SmitherzMain;
import net.smitherz.network.SmitherClientPacket;

@Environment(EnvType.CLIENT)
public class SmitherScreen
   extends net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<SmitherScreenHandler>
   implements net.minecraft.world.inventory.ContainerListener,
   Tab {
   public static final Identifier TEXTURE = SmitherzMain.identifierOf("textures/gui/smither_screen.png");
   private SmitherScreen.SmitherButton smitherButton;

   public SmitherScreen(SmitherScreenHandler handler, Inventory playerInventory, Component title) {
      super(handler, playerInventory, title);
      this.titleLabelX = 44;
   }

   protected void init() {
      super.init();
      ((SmitherScreenHandler)this.menu).addSlotListener(this);
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      this.smitherButton = (SmitherScreen.SmitherButton)this.addRenderableWidget(new SmitherScreen.SmitherButton(i + 148, j + 18, button -> {
         if (button instanceof SmitherScreen.SmitherButton && !((SmitherScreen.SmitherButton)button).disabled) {
            SmitherClientPacket.writeC2SSmitherPacket();
         }
      }));
   }

   public void removed() {
      super.removed();
      ((SmitherScreenHandler)this.menu).removeSlotListener(this);
   }

   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      super.extractRenderState(context, mouseX, mouseY, delta);
      this.extractTooltip(context, mouseX, mouseY);
   }

   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
   }

   public void dataChanged(AbstractContainerMenu handler, int property, int value) {
   }

   public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
   }

   public Class<?> getParentScreenClass() {
      return SmithingScreen.class;
   }

   public SmitherScreen.SmitherButton getSmitherButton() {
      return this.smitherButton;
   }

   public class SmitherButton extends Button {
      private boolean disabled = true;

      public SmitherButton(int x, int y, OnPress onPress) {
         super(x, y, 18, 18, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
      }

      protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
         int j = 176;
         if (this.disabled) {
            j += this.width * 2;
         } else if (this.isHovered()) {
            j += this.width;
         }

         context.blit(RenderPipelines.GUI_TEXTURED, SmitherScreen.TEXTURE, this.getX(), this.getY(), (float)j, 0.0F, this.width, this.height, 256, 256);
      }

      public void setDisabled(boolean disable) {
         this.disabled = disable;
      }
   }
}
