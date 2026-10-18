package ht.treechop.client.gui.element;

import ht.treechop.client.gui.util.IPositionalGui;
import ht.treechop.client.gui.util.ScreenBox;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public abstract class NestedGui extends AbstractWidget implements ContainerEventHandler, IPositionalGui {
   private GuiEventListener listener = null;
   private boolean dragging = false;
   private ScreenBox box;

   public NestedGui(int x, int y, int width, int height, Component message) {
      super(x, y, width, height, message);
   }

   public abstract int getMinimumWidth();

   public abstract int getMinimumHeight();

   public int getLeftColumnWidth() {
      return this.getMinimumWidth() / 2;
   }

   public int getRightColumnWidth() {
      return this.getMinimumWidth() / 2;
   }

   public void setColumnWidths(int leftColumnWidth, int rightColumnWidth) {
   }

   public boolean isDragging() {
      return this.dragging;
   }

   public void setDragging(boolean dragging) {
      this.dragging = dragging;
   }

   public void setFocused(@Nullable GuiEventListener listener) {
      this.listener = listener;
   }

   @Nullable
   public GuiEventListener getFocused() {
      return this.listener;
   }

   @Override
   public ScreenBox getBox() {
      return this.box;
   }

   public void setWidth(int width) {
      ScreenBox box = this.getBox();
      this.setBox(box.getLeft(), box.getTop(), width, box.getHeight());
   }

   @Override
   public void setBox(ScreenBox box) {
      this.box = box;
      this.width = box.getWidth();
      this.height = box.getHeight();
      this.setX(box.getLeft());
      this.setY(box.getTop());
   }

   public Optional<net.minecraft.client.gui.components.events.GuiEventListener> getChildAt(double x, double y) {
      for (GuiEventListener guieventlistener : this.children()) {
         if (guieventlistener.isMouseOver(x, y)) {
            return Optional.of(guieventlistener);
         }
      }

      return Optional.empty();
   }

   public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean bl) {
      for (GuiEventListener guieventlistener : this.children()) {
         if (guieventlistener.mouseClicked(event, bl)) {
            this.setFocused(guieventlistener);
            if (event.button() == 0) {
               this.setDragging(true);
            }

            return true;
         }
      }

      return false;
   }

   public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
      this.setDragging(false);
      return this.getChildAt(event.x(), event.y()).filter(child -> child.mouseReleased(event)).isPresent();
   }

   public void expand(int width) {
   }
}
