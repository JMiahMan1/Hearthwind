package folk.sisby.antique_atlas.util;

import java.util.Iterator;

public class ArrayIterator<T> implements Iterator<T> {
   protected final T[] array;
   protected int currentIndex = 0;

   public ArrayIterator(T[] array) {
      this.array = array;
   }

   @Override
   public boolean hasNext() {
      return this.currentIndex < this.array.length;
   }

   @Override
   public T next() {
      return this.array[this.currentIndex++];
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("cannot remove items from an array");
   }
}
