package folk.sisby.antique_atlas.gui.tiles;

import folk.sisby.antique_atlas.util.ArrayIterator;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class SubTileQuartet implements Iterable<SubTile> {
   protected final SubTile[] array;

   public SubTileQuartet(SubTile a, SubTile b, SubTile c, SubTile d) {
      this.array = new SubTile[]{a, b, c, d};
   }

   public void setCoords(int x, int y) {
      this.array[0].x = x;
      this.array[1].x = x + 1;
      this.array[2].x = x;
      this.array[3].x = x + 1;
      this.array[0].y = y;
      this.array[1].y = y;
      this.array[2].y = y + 1;
      this.array[3].y = y + 1;
   }

   @NotNull
   @Override
   public Iterator<SubTile> iterator() {
      return new ArrayIterator<>(this.array);
   }
}
