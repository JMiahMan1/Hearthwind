package folk.sisby.antique_atlas.gui.tiles;

import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.antique_atlas.util.Rect;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class TileRenderIterator implements Iterator<SubTileQuartet>, Iterable<SubTileQuartet> {
   protected final WorldAtlasData tiles;
   protected int step = 1;
   protected final Rect scope = new Rect();
   protected TileTexture a;
   protected TileTexture b;
   protected TileTexture c;
   protected TileTexture d;
   protected TileTexture e;
   protected TileTexture f;
   protected TileTexture g;
   protected TileTexture h;
   protected TileTexture i;
   protected TileTexture j;
   protected TileTexture k;
   protected TileTexture l;
   protected final SubTile _d = new SubTile(SubTile.Part.BOTTOM_RIGHT);
   protected final SubTile _e = new SubTile(SubTile.Part.BOTTOM_LEFT);
   protected final SubTile _h = new SubTile(SubTile.Part.TOP_RIGHT);
   protected final SubTile _i = new SubTile(SubTile.Part.TOP_LEFT);
   protected final SubTileQuartet quartet = new SubTileQuartet(this._d, this._e, this._h, this._i);
   protected int chunkX;
   protected int chunkY;
   protected int subtileX = -1;
   protected int subtileY = -1;

   public void setStep(int step) {
      if (step >= 1) {
         this.step = step;
      }
   }

   public void setScope(Rect scope) {
      this.scope.set(scope);
      this.chunkX = scope.minX;
      this.chunkY = scope.minY;
   }

   public TileRenderIterator(WorldAtlasData tiles) {
      this.tiles = tiles;
      this.setScope(tiles.getScope());
   }

   @NotNull
   @Override
   public Iterator<SubTileQuartet> iterator() {
      return this;
   }

   @Override
   public boolean hasNext() {
      return this.chunkX >= this.scope.minX && this.chunkX <= this.scope.maxX + 1 && this.chunkY >= this.scope.minY && this.chunkY <= this.scope.maxY + 1;
   }

   public SubTileQuartet next() {
      this.a = this.b;
      this.b = this.tiles.getTile(this.chunkX, this.chunkY - this.step * 2);
      this.c = this.d;
      this.d = this.e;
      this.e = this.f;
      this.f = this.tiles.getTile(this.chunkX + this.step, this.chunkY - this.step);
      this.g = this.h;
      this.h = this.i;
      this.i = this.j;
      this.j = this.tiles.getTile(this.chunkX + this.step, this.chunkY);
      this.k = this.l;
      this.l = this.tiles.getTile(this.chunkX, this.chunkY + this.step);
      this.quartet.setCoords(this.subtileX, this.subtileY);
      this._d.texture = this.d;
      this._e.texture = this.e;
      this._h.texture = this.h;
      this._i.texture = this.i;

      for (SubTile subtile : this.quartet) {
         subtile.shape = SubTile.Shape.CONVEX;
      }

      if (tilesToHorizontal(this.d, this.e)) {
         applyTilingHorizontally(this._d);
      }

      if (tilesToHorizontal(this.e, this.d)) {
         applyTilingHorizontally(this._e);
      }

      if (tilesToHorizontal(this.h, this.i)) {
         applyTilingHorizontally(this._h);
      }

      if (tilesToHorizontal(this.i, this.h)) {
         applyTilingHorizontally(this._i);
      }

      if (tilesToVertical(this.d, this.h)) {
         applyTilingVertically(this._d);
         if (this._d.shape == SubTile.Shape.CONCAVE && tilesTo(this.d, this.i)) {
            this._d.shape = SubTile.Shape.FULL;
         }
      }

      if (tilesToVertical(this.h, this.d)) {
         applyTilingVertically(this._h);
         if (this._h.shape == SubTile.Shape.CONCAVE && tilesTo(this.h, this.e)) {
            this._h.shape = SubTile.Shape.FULL;
         }
      }

      if (tilesToVertical(this.e, this.i)) {
         applyTilingVertically(this._e);
         if (this._e.shape == SubTile.Shape.CONCAVE && tilesTo(this.e, this.h)) {
            this._e.shape = SubTile.Shape.FULL;
         }
      }

      if (tilesToVertical(this.i, this.e)) {
         applyTilingVertically(this._i);
         if (this._i.shape == SubTile.Shape.CONCAVE && tilesTo(this.i, this.d)) {
            this._i.shape = SubTile.Shape.FULL;
         }
      }

      if (this._d.shape == SubTile.Shape.CONVEX && !tilesToVertical(this.d, this.a) && !tilesToHorizontal(this.d, this.c)) {
         this._d.shape = SubTile.Shape.SINGLE_OBJECT;
      }

      if (this._e.shape == SubTile.Shape.CONVEX && !tilesToVertical(this.e, this.b) && !tilesToHorizontal(this.e, this.f)) {
         this._e.shape = SubTile.Shape.SINGLE_OBJECT;
      }

      if (this._h.shape == SubTile.Shape.CONVEX && !tilesToHorizontal(this.h, this.g) && !tilesToVertical(this.h, this.k)) {
         this._h.shape = SubTile.Shape.SINGLE_OBJECT;
      }

      if (this._i.shape == SubTile.Shape.CONVEX && !tilesToHorizontal(this.i, this.j) && !tilesToVertical(this.i, this.l)) {
         this._i.shape = SubTile.Shape.SINGLE_OBJECT;
      }

      this.chunkX = this.chunkX + this.step;
      this.subtileX += 2;
      if (this.chunkX > this.scope.maxX + 1) {
         this.chunkX = this.scope.minX;
         this.subtileX = -1;
         this.chunkY = this.chunkY + this.step;
         this.subtileY += 2;
         this.a = null;
         this.b = null;
         this.c = null;
         this.d = null;
         this.e = null;
         this.f = this.tiles.getTile(this.chunkX, this.chunkY - this.step);
         this.g = null;
         this.h = null;
         this.i = null;
         this.j = this.tiles.getTile(this.chunkX, this.chunkY);
         this.k = null;
         this.l = null;
      }

      return this.quartet;
   }

   public static boolean tilesTo(TileTexture tile, TileTexture to) {
      return tile != null && to != null && tile.tiles(to);
   }

   public static boolean tilesToHorizontal(TileTexture tile, TileTexture to) {
      return tile != null && to != null && tile.tilesHorizontally(to);
   }

   public static boolean tilesToVertical(TileTexture tile, TileTexture to) {
      return tile != null && to != null && tile.tilesVertically(to);
   }

   public static void applyTilingHorizontally(SubTile subtile) {
      if (subtile.shape == SubTile.Shape.VERTICAL) {
         subtile.shape = SubTile.Shape.CONCAVE;
      } else if (subtile.shape == SubTile.Shape.CONVEX) {
         subtile.shape = SubTile.Shape.HORIZONTAL;
      }
   }

   public static void applyTilingVertically(SubTile subtile) {
      if (subtile.shape == SubTile.Shape.HORIZONTAL) {
         subtile.shape = SubTile.Shape.CONCAVE;
      } else if (subtile.shape == SubTile.Shape.CONVEX) {
         subtile.shape = SubTile.Shape.VERTICAL;
      }
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("cannot remove subtiles from tile storage");
   }
}
