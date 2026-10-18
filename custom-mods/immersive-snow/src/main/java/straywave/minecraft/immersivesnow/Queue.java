package straywave.minecraft.immersivesnow;

import java.util.ArrayList;
import net.minecraft.world.level.ChunkPos;

public class Queue {
   private static final ArrayList<Queue.Entry> currentTickChunks = new ArrayList<>();
   private static final ArrayList<Queue.Entry> nextTickChunks = new ArrayList<>();

   public static void tryAdd(ChunkPos chunkPos, boolean currentTick) {
      if (Memory.hasForgotten(chunkPos)) {
         Memory.remember(chunkPos);
         add(new Queue.Entry(chunkPos, 0), currentTick);
      }
   }

   public static void add(Queue.Entry entry, boolean currentTick) {
      if (currentTick) {
         currentTickChunks.add(entry);
      } else {
         nextTickChunks.add(entry);
      }
   }

   public static int size() {
      return currentTickChunks.size();
   }

   public static boolean isEmpty() {
      return currentTickChunks.isEmpty();
   }

   public static Queue.Entry pop() {
      return currentTickChunks.remove(0);
   }

   public static void shuffle() {
      currentTickChunks.addAll(nextTickChunks);
      nextTickChunks.clear();
   }

   public static void clear() {
      currentTickChunks.clear();
      nextTickChunks.clear();
   }

   public record Entry(ChunkPos pos, int sittingFor) {
   }
}
