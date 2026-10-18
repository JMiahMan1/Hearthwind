package ht.treechop.client.gui.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class RowsGui extends NestedGui {
   private final int rowSeparation;
   private final int biggestLeftColumnWidth;
   private final int biggestRightColumnWidth;
   private final List<NestedGui> rows = new ArrayList<>();

   public RowsGui(int rowSeparation, Collection<NestedGui> rows) {
      super(0, 0, 0, 0, Component.empty());
      this.rowSeparation = rowSeparation;
      this.rows.addAll(rows);
      this.biggestLeftColumnWidth = rows.stream().map(NestedGui::getLeftColumnWidth).reduce(Integer::max).orElse(0);
      this.biggestRightColumnWidth = rows.stream().map(NestedGui::getRightColumnWidth).reduce(Integer::max).orElse(0);
      rows.forEach(row -> row.setColumnWidths(this.biggestLeftColumnWidth, this.biggestRightColumnWidth));
   }

   public int getHeight() {
      return getHeightForRows(this.rows, this.rowSeparation);
   }

   public static int getHeightForRows(Collection<NestedGui> rows, int rowSeparation) {
      return Math.max(0, rows.stream().map(NestedGui::getMinimumHeight).reduce(Integer::sum).orElse(0) + (rows.size() - 1) * rowSeparation);
   }

   public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
      return this.rows;
   }

   public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
      int rowLeft = this.getBox().getLeft();
      int rowTop = this.getBox().getTop();
      int rowWidth = this.getBox().getWidth();

      for (NestedGui row : this.rows) {
         int rowHeight = row.getMinimumHeight();
         row.setBox(rowLeft, rowTop, rowWidth, rowHeight);
         row.extractRenderState(gui, mouseX, mouseY, partialTicks);
         rowTop += rowHeight + this.rowSeparation;
      }
   }

   @Override
   public int getMinimumWidth() {
      return 0;
   }

   @Override
   public int getMinimumHeight() {
      return 0;
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }
}
