package ht.treechop.client.gui.util;

public class ScreenBox {
   protected int left;
   protected int top;
   protected int width;
   protected int height;

   public ScreenBox(int left, int top, int width, int height) {
      this.set(left, top, width, height);
   }

   public void set(int left, int top, int width, int height) {
      this.left = left;
      this.top = top;
      this.width = width;
      this.height = height;
   }

   public void set(ScreenBox other) {
      this.set(other.getLeft(), other.getTop(), other.getWidth(), other.getHeight());
   }

   public int getLeft() {
      return this.left;
   }

   public int getTop() {
      return this.top;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getCenterX() {
      return this.getLeft() + this.getWidth() / 2;
   }

   public int getCenterY() {
      return this.getTop() + this.getHeight() / 2;
   }
}
