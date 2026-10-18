package ht.treechop.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TextWidget extends AbstractWidget {
   private final Font font;

   public TextWidget(int x, int y, Font font, Component text) {
      super(x, y, font.width(text.getString()), 8, text);
      this.font = font;
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks, boolean rightAligned) {
      this.extractWidgetRenderState(gui, mouseX, mouseY, partialTicks, rightAligned ? -this.font.width(this.getMessage()) : 0);
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
      this.extractWidgetRenderState(gui, mouseX, mouseY, partialTicks, 0);
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks, int xOffset) {
      gui.text(this.font, this.getMessage(), this.getX() + xOffset, this.getY(), 16777215);
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }
}
