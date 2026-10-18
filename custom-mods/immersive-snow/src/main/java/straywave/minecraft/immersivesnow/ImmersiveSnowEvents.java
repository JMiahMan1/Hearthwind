package straywave.minecraft.immersivesnow;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import straywave.minecraft.immersivesnow.mixin.MinecraftServerInvoker;

public class ImmersiveSnowEvents {
   public static void onLevelLoad(ServerLevel level) {
      Queue.clear();
      Memory.erase();
   }

   public static void onLevelUnload(ServerLevel level) {
      Queue.clear();
      Memory.erase();
   }

   public static void onChunkLoad(ServerLevel level, LevelChunk chunk, boolean newChunk) {
      if (level.dimension().identifier().toString().equals("minecraft:overworld")) {
         Queue.tryAdd(chunk.getPos(), false);
      }
   }

   public static void onWorldTick(ServerLevel level) {
      MinecraftServerInvoker server = (MinecraftServerInvoker)level.getServer();
      if (level.dimension().identifier().toString().equals("minecraft:overworld")) {
         if (Queue.isEmpty()) {
            Queue.shuffle();
         } else {
            for (int i = 0; i < Mth.clamp(Queue.size(), 0, Configuration.data.maxChunksToProcessPerTick); i++) {
               Queue.Entry entry = Queue.pop();
               ChunkPos chunkPos = entry.pos();
               int sittingFor = entry.sittingFor();
               if (level.hasChunk(chunkPos.x(), chunkPos.z())
                  && level.hasChunk(chunkPos.x() - 1, chunkPos.z() - 1)
                  && level.hasChunk(chunkPos.x() + 1, chunkPos.z() - 1)
                  && level.hasChunk(chunkPos.x() - 1, chunkPos.z() + 1)
                  && level.hasChunk(chunkPos.x() + 1, chunkPos.z() + 1)) {
                  Logic.processChunk(level, chunkPos);
                  if (!server.snow$hasTimeRemaining() && i >= Configuration.data.minChunksToProcessPerTick) {
                     break;
                  }
               } else if (sittingFor < 100) {
                  Queue.add(new Queue.Entry(chunkPos, sittingFor + 1), false);
                  i--;
               } else {
                  Memory.forget(chunkPos);
               }
            }
         }
      }
   }
}
