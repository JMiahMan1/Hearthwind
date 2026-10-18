package folk.sisby.antique_atlas.gui.tiles;

import folk.sisby.antique_atlas.TileTexture;

public class SubTile {
   public TileTexture texture;
   public int x;
   public int y;
   public SubTile.Shape shape;
   public final SubTile.Part part;

   public SubTile(SubTile.Part part) {
      this.part = part;
   }

   public int getTextureU() {
      return switch (this.shape) {
         case CONVEX, VERTICAL -> this.part.u * 3;
         case CONCAVE -> 2 + this.part.u;
         case HORIZONTAL, FULL -> 2 - this.part.u;
         case SINGLE_OBJECT -> this.part.u;
      };
   }

   public int getTextureV() {
      return switch (this.shape) {
         case CONVEX, HORIZONTAL -> 2 + this.part.v * 3;
         case CONCAVE, SINGLE_OBJECT -> this.part.v;
         case VERTICAL, FULL -> 4 - this.part.v;
      };
   }

   public SubTile copy() {
      SubTile copy = new SubTile(this.part);
      copy.x = this.x;
      copy.y = this.y;
      copy.shape = this.shape;
      return copy;
   }

   public static enum Part {
      TOP_LEFT(0, 0),
      TOP_RIGHT(1, 0),
      BOTTOM_LEFT(0, 1),
      BOTTOM_RIGHT(1, 1);

      final int u;
      final int v;

      private Part(int u, int v) {
         this.u = u;
         this.v = v;
      }
   }

   public static enum Shape {
      CONVEX,
      CONCAVE,
      HORIZONTAL,
      VERTICAL,
      FULL,
      SINGLE_OBJECT;
   }
}
