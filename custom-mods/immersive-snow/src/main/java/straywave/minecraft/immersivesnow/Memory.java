package straywave.minecraft.immersivesnow;

import java.time.Instant;
import java.util.HashMap;
import net.minecraft.world.level.ChunkPos;

public class Memory {
   private static final HashMap<net.minecraft.world.level.ChunkPos, Long> data = new HashMap<>();

   public static void remember(ChunkPos chunkPos) {
      data.put(chunkPos, Instant.now().getEpochSecond());
   }

   public static void forget(ChunkPos chunkPos) {
      data.remove(chunkPos);
   }

   public static boolean hasForgotten(ChunkPos chunkPos) {
      long rememberedTime = data.getOrDefault(chunkPos, 0L);
      long currentTime = Instant.now().getEpochSecond();
      long difference = currentTime - rememberedTime;
      boolean hasForgotten = difference >= Configuration.data.memoryDuration;
      if (hasForgotten) {
         data.remove(chunkPos);
      }

      return hasForgotten;
   }

   public static void erase() {
      data.clear();
   }
}
