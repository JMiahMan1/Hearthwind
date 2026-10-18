package ht.treechop.client.gui.element;

import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.SpriteButtonWidget;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ButtonGui extends NestedGui {
   private final SpriteButtonWidget widget;

   public ButtonGui(Sprite sprite, Sprite highlightedSprite, Runnable onPress) {
      super(0, 0, 0, 0, Component.empty());
      this.widget = new SpriteButtonWidget(0, 0, sprite, highlightedSprite, onPress);
   }

   @NotNull
   public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
      return Collections.singletonList(this.widget);
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
      this.widget.setX(this.getBox().getCenterX() - this.widget.getWidth() / 2);
      this.widget.setY(this.getBox().getCenterY() - this.widget.getHeight() / 2);
      this.widget.extractRenderState(gui, mouseX, mouseY, partialTicks);
   }

   @Override
   public int getMinimumWidth() {
      return this.widget.getWidth();
   }

   @Override
   public int getMinimumHeight() {
      return this.widget.getHeight();
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }
}
