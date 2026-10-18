package ht.treechop.client.gui.util;

public interface IPositionalGui {
   ScreenBox getBox();

   void setBox(ScreenBox var1);

   default void setBox(int left, int top, int width, int height) {
      this.setBox(new ScreenBox(left, top, width, height));
   }
}
