package net.mehvahdjukaar.smarterfarmers.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-tree replacement for Moonlight's collection of the same name: keeps
 * insertion order for equal counts while iterating most-frequent-first.
 */
public class FrequencyOrderedCollection<T> implements Iterable<T> {
   private final Map<T, Integer> counts = new LinkedHashMap<>();

   public void add(T value) {
      this.counts.merge(value, 1, Integer::sum);
   }

   public int getCount(T value) {
      return this.counts.getOrDefault(value, 0);
   }

   public boolean isEmpty() {
      return this.counts.isEmpty();
   }

   public int size() {
      return this.counts.size();
   }

   @Override
   public Iterator<T> iterator() {
      List<Map.Entry<T, Integer>> entries = new ArrayList<>(this.counts.entrySet());
      entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
      List<T> ordered = new ArrayList<>(entries.size());
      for (Map.Entry<T, Integer> entry : entries) {
         ordered.add(entry.getKey());
      }
      return ordered.iterator();
   }
}
