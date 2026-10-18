package ht.treechop.client.gui.element;

import ht.treechop.client.gui.widget.TextWidget;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class LabeledGui extends NestedGui {
   private static final int COLUMN_PADDING = 4;
   private final TextWidget label;
   private final NestedGui gui;
   private int leftColumnWidth;
   private int rightcolumnWidth;
   private boolean rightAlignLabels = false;

   public LabeledGui(Font font, Component label, NestedGui gui) {
      super(0, 0, 0, 0, label);
      this.label = new TextWidget(0, 0, font, label);
      this.gui = gui;
      this.leftColumnWidth = this.getLeftColumnWidth();
      this.rightcolumnWidth = this.getRightColumnWidth();
   }

   @Override
   public int getMinimumWidth() {
      return this.getLeftColumnWidth() + this.gui.getMinimumWidth();
   }

   @Override
   public int getMinimumHeight() {
      return Math.max(this.label.getHeight(), this.gui.getMinimumHeight());
   }

   @Override
   public int getLeftColumnWidth() {
      return this.label.getWidth() + 4;
   }

   @Override
   public int getRightColumnWidth() {
      return this.gui.getMinimumWidth() + 4;
   }

   public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
      return Stream.of(Collections.singletonList(this.label), this.gui.children()).flatMap(Collection::stream).collect(Collectors.toList());
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
      int center = this.getBox().getCenterX() + (this.leftColumnWidth - this.rightcolumnWidth) / 2;
      this.label.setX(center - 4 + (this.rightAlignLabels ? 0 : -this.leftColumnWidth));
      this.label.setY(this.getBox().getCenterY() - 3);
      this.label.extractRenderState(gui, mouseX, mouseY, partialTicks);
      this.gui.setBox(center + 4, this.getBox().getTop(), this.rightcolumnWidth, this.getBox().getHeight());
      this.gui.extractRenderState(gui, mouseX, mouseY, partialTicks);
   }

   @Override
   public void setColumnWidths(int biggestLeftColumnWidth, int biggestRightColumnWidth) {
      this.leftColumnWidth = biggestLeftColumnWidth;
      this.rightcolumnWidth = biggestRightColumnWidth;
      this.gui.expand(biggestRightColumnWidth);
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }
}
