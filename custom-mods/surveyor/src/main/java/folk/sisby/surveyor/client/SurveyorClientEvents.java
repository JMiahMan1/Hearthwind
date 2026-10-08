package folk.sisby.surveyor.client;

import com.google.common.collect.Multimap;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.util.MapUtil;
import folk.sisby.surveyor.util.RegionPos;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;

public class SurveyorClientEvents {
   private static final Map<Identifier, SurveyorClientEvents.TerrainUpdated> terrainUpdated = new HashMap<>();
   private static final Map<Identifier, SurveyorClientEvents.StructuresAdded> structuresAdded = new HashMap<>();
   private static final Map<Identifier, SurveyorClientEvents.LandmarksAdded> landmarksAdded = new HashMap<>();
   private static final Map<Identifier, SurveyorClientEvents.LandmarksRemoved> landmarksRemoved = new HashMap<>();

   public static class Invoke {
      public static void terrainUpdated(WorldSummary summary, Map<RegionPos, BitSet> chunks) {
         if (!SurveyorClientEvents.terrainUpdated.isEmpty() && !chunks.isEmpty()) {
            SurveyorClientEvents.terrainUpdated.forEach((id, handler) -> handler.onTerrainUpdated(summary, chunks));
         }
      }

      public static void terrainUpdated(WorldSummary summary, ChunkPos pos) {
         terrainUpdated(summary, Map.of(RegionPos.of(pos), RegionPos.chunkToBitSet(pos)));
      }

      public static void structuresAdded(WorldSummary summary, Multimap<ResourceKey<Structure>, ChunkPos> starts) {
         if (!SurveyorClientEvents.structuresAdded.isEmpty() && !starts.isEmpty()) {
            SurveyorClientEvents.structuresAdded.forEach((id, handler) -> handler.onStructuresAdded(summary, starts));
         }
      }

      public static void structuresAdded(WorldSummary summary, ResourceKey<Structure> key, ChunkPos pos) {
         structuresAdded(summary, MapUtil.asMultiMap(Map.of(key, List.of(pos))));
      }

      public static void landmarksAdded(WorldSummary summary, Multimap<UUID, Identifier> landmarks) {
         if (!SurveyorClientEvents.landmarksAdded.isEmpty() && !landmarks.isEmpty()) {
            SurveyorClientEvents.landmarksAdded.forEach((id, handler) -> handler.onLandmarksAdded(summary, landmarks));
         }
      }

      public static void landmarksRemoved(WorldSummary summary, Multimap<UUID, Identifier> landmarks) {
         if (!SurveyorClientEvents.landmarksRemoved.isEmpty() && !landmarks.isEmpty()) {
            SurveyorClientEvents.landmarksRemoved.forEach((id, handler) -> handler.onLandmarksRemoved(summary, landmarks));
         }
      }
   }

   @FunctionalInterface
   public interface LandmarksAdded {
      void onLandmarksAdded(WorldSummary var1, Multimap<UUID, Identifier> var2);
   }

   @FunctionalInterface
   public interface LandmarksRemoved {
      void onLandmarksRemoved(WorldSummary var1, Multimap<UUID, Identifier> var2);
   }

   public static class Register {
      public static void terrainUpdated(Identifier id, SurveyorClientEvents.TerrainUpdated handler) {
         SurveyorClientEvents.terrainUpdated.put(id, handler);
      }

      public static void structuresAdded(Identifier id, SurveyorClientEvents.StructuresAdded handler) {
         SurveyorClientEvents.structuresAdded.put(id, handler);
      }

      public static void landmarksAdded(Identifier id, SurveyorClientEvents.LandmarksAdded handler) {
         SurveyorClientEvents.landmarksAdded.put(id, handler);
      }

      public static void landmarksRemoved(Identifier id, SurveyorClientEvents.LandmarksRemoved handler) {
         SurveyorClientEvents.landmarksRemoved.put(id, handler);
      }
   }

   @FunctionalInterface
   public interface StructuresAdded {
      void onStructuresAdded(WorldSummary var1, Multimap<ResourceKey<Structure>, ChunkPos> var2);
   }

   @FunctionalInterface
   public interface TerrainUpdated {
      void onTerrainUpdated(WorldSummary var1, Map<RegionPos, BitSet> var2);
   }
}
