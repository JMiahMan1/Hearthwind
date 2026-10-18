package ht.treechop.client.gui.element;

import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.widget.StickyWidget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ExclusiveButtonsGui extends NestedGui {
   private final List<net.minecraft.client.gui.components.AbstractWidget> widgets;
   private final Supplier<net.minecraft.network.chat.Component> tooltipSupplier;

   protected ExclusiveButtonsGui(
      Collection<net.minecraft.client.gui.components.AbstractWidget> widgets, Supplier<net.minecraft.network.chat.Component> tooltipSupplier
   ) {
      super(0, 0, 0, 0, Component.empty());
      this.widgets = new ArrayList<>(widgets);
      this.tooltipSupplier = tooltipSupplier;
   }

   @Override
   public void expand(int width) {
      if (this.getMinimumWidth() < width) {
         int targetWidth = width / this.widgets.size();
         List<net.minecraft.client.gui.components.AbstractWidget> smallerWidgets = this.widgets
            .stream()
            .filter(widgetx -> widgetx.getWidth() <= targetWidth)
            .collect(Collectors.toList());
         List<net.minecraft.client.gui.components.AbstractWidget> biggerWidgets = this.widgets
            .stream()
            .filter(widgetx -> widgetx.getWidth() > targetWidth)
            .collect(Collectors.toList());
         int totalWidthForSmallers = width - biggerWidgets.stream().<Integer>map(AbstractWidget::getWidth).reduce(Integer::sum).orElse(0);
         int i = 0;

         for (AbstractWidget widget : smallerWidgets) {
            double lower = (double)i / smallerWidgets.size();
            double upper = (double)(i + 1) / smallerWidgets.size();
            int widgetWidth = (int)(totalWidthForSmallers * (upper - lower));
            widget.setWidth(widgetWidth);
            i++;
         }
      }
   }

   public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
      return this.widgets;
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
      int x = this.getBox().getLeft();
      int y = this.getBox().getTop();
      int maxX = x;
      int maxY = y;

      for (AbstractWidget widget : this.widgets) {
         widget.setX(maxX);
         widget.setY(y);
         widget.extractRenderState(gui, mouseX, mouseY, partialTicks);
         maxX = Math.max(maxX, maxX + widget.getWidth());
         maxY = Math.max(maxY, y + widget.getHeight());
      }

      this.isHovered = mouseX >= x && mouseY >= y && mouseX < maxX && mouseY < maxY;
      if (this.isHoveredOrFocused()) {
         GUIUtil.showTooltip(mouseX, mouseY, (Component)this.tooltipSupplier.get());
      }
   }

   @Override
   public int getMinimumWidth() {
      return this.widgets.stream().<Integer>map(AbstractWidget::getWidth).reduce(Integer::sum).orElse(0);
   }

   @Override
   public int getMinimumHeight() {
      return this.widgets.stream().<Integer>map(AbstractWidget::getHeight).reduce(Integer::max).orElse(0);
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }

   public static class Builder {
      private final List<ExclusiveButtonsGui.Builder.Option> options = new LinkedList<>();

      public ExclusiveButtonsGui.Builder add(net.minecraft.network.chat.Component name, Runnable onPress, Supplier<StickyWidget.State> stateSupplier) {
         this.options.add(new ExclusiveButtonsGui.Builder.Option(name, onPress, stateSupplier));
         return this;
      }

      public ExclusiveButtonsGui build(Supplier<net.minecraft.network.chat.Component> tooltipSupplier) {
         List<net.minecraft.client.gui.components.AbstractWidget> widgets = this.options
            .stream()
            .map(option -> new StickyWidget(0, 0, 0, 0, option.name, option.onPress, option.stateSupplier))
            .collect(Collectors.toList());
         return new ExclusiveButtonsGui(widgets, tooltipSupplier);
      }

      private static class Option {
         private final Component name;
         private final Runnable onPress;
         private final Supplier<StickyWidget.State> stateSupplier;

         public Option(net.minecraft.network.chat.Component name, Runnable onPress, Supplier<StickyWidget.State> stateSupplier) {
            this.name = name;
            this.onPress = onPress;
            this.stateSupplier = stateSupplier;
         }
      }
   }
}
