package folk.sisby.antique_atlas.util;

public class Rect {
   public int minX;
   public int minY;
   public int maxX;
   public int maxY;

   public Rect() {
      this(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
   }

   public Rect(int minX, int minY, int maxX, int maxY) {
      this.set(minX, minY, maxX, maxY);
   }

   public Rect set(int minX, int minY, int maxX, int maxY) {
      this.minX = minX;
      this.minY = minY;
      this.maxX = maxX;
      this.maxY = maxY;
      return this;
   }

   public Rect set(Rect r) {
      this.set(r.minX, r.minY, r.maxX, r.maxY);
      return this;
   }

   public void extendTo(int x, int y) {
      if (x < this.minX) {
         this.minX = x;
      }

      if (x > this.maxX) {
         this.maxX = x;
      }

      if (y < this.minY) {
         this.minY = y;
      }

      if (y > this.maxY) {
         this.maxY = y;
      }
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof Rect r) ? false : this.minX == r.minX && this.minY == r.minY && this.maxX == r.maxX && this.maxY == r.maxY;
   }

   @Override
   public String toString() {
      return String.format("Rect{%d, %d, %d, %d}", this.minX, this.minY, this.maxX, this.maxY);
   }
}
