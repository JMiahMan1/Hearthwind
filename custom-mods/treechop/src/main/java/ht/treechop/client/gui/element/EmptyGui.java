package ht.treechop.client.gui.element;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class EmptyGui extends NestedGui {
   private int width;
   private int height;

   public EmptyGui(int width, int height) {
      super(0, 0, width, height, Component.empty());
      this.width = width;
      this.height = height;
      this.active = false;
   }

   @Override
   public int getMinimumWidth() {
      return this.width;
   }

   @Override
   public int getMinimumHeight() {
      return this.height;
   }

   @Override
   public int getLeftColumnWidth() {
      return 0;
   }

   @Override
   public int getRightColumnWidth() {
      return 0;
   }

   public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
      return Collections.emptyList();
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor poseStack, int mouseX, int mouseY, float partialTicks) {
   }

   @Override
   public void setColumnWidths(int biggestLeftColumnWidth, int biggestRightColumnWidth) {
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }
}
