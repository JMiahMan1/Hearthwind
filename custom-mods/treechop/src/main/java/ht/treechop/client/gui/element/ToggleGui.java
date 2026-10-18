package ht.treechop.client.gui.element;

import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.widget.ToggleWidget;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ToggleGui extends NestedGui {
   private final AbstractWidget widget;
   private final Supplier<net.minecraft.network.chat.Component> tooltipSupplier;

   public ToggleGui(Runnable onPress, Supplier<ToggleWidget.State> stateSupplier, Supplier<net.minecraft.network.chat.Component> componentSupplier) {
      super(0, 0, 0, 0, Component.empty());
      this.widget = new ToggleWidget(0, 0, onPress, stateSupplier);
      this.tooltipSupplier = componentSupplier;
   }

   public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
      return Collections.singletonList(this.widget);
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
      this.widget.setX(this.getBox().getLeft());
      this.widget.setY(this.getBox().getTop());
      this.widget.extractRenderState(gui, mouseX, mouseY, partialTicks);
      if (this.widget.isHoveredOrFocused()) {
         GUIUtil.showTooltip(mouseX, mouseY, (Component)this.tooltipSupplier.get());
      }
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
