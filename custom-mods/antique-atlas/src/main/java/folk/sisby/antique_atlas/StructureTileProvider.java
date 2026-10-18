package folk.sisby.antique_atlas;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import folk.sisby.surveyor.WorldSummary;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;

public record StructureTileProvider(Identifier id, int priority, Map<StructureTileProvider.ChunkMatcher, List<TileTexture>> matchers) {
   public static final BiMap<net.minecraft.resources.Identifier, StructureTileProvider.ChunkMatcher> CHUNK_MATCHERS = HashBiMap.create();
   private static final int SEA_LEVEL = 63;

   public StructureTileProvider(net.minecraft.resources.Identifier id, int priority, List<TileTexture> textures) {
      this(id, priority, Map.of(StructureTileProvider.ChunkMatcher::center, textures));
   }

   public StructureTileProvider(
      net.minecraft.resources.Identifier id, int priority, StructureTileProvider.ChunkMatcher matcher, List<TileTexture> textures
   ) {
      this(id, priority, Map.of(matcher, textures));
   }

   public static StructureTileProvider.ChunkMatcher getChunkMatcher(Identifier id) {
      return (StructureTileProvider.ChunkMatcher)CHUNK_MATCHERS.get(id);
   }

   public Map<net.minecraft.world.level.ChunkPos, TileTexture> getTextures(
      Function<StructureTileProvider.ChunkMatcher, Collection<net.minecraft.world.level.ChunkPos>> matcherFunction,
      Map<net.minecraft.world.level.ChunkPos, String> tilePredicates
   ) {
      Map<net.minecraft.world.level.ChunkPos, TileTexture> outMap = new HashMap<>();
      this.matchers.forEach((matcher, textures) -> {
         for (ChunkPos pos : matcherFunction.apply(matcher)) {
            tilePredicates.put(pos, Objects.toString(CHUNK_MATCHERS.inverse().get(matcher), null));
            int variation = (int)(Mth.getSeed(pos.x(), pos.z(), pos.x() * pos.z()) & 2147483647L);
            outMap.put(pos, textures.get(variation % textures.size()));
         }
      });
      return outMap;
   }

   public Map<net.minecraft.world.level.ChunkPos, TileTexture> getTextures(
      WorldSummary summary,
      net.minecraft.world.level.levelgen.structure.BoundingBox box,
      Map<net.minecraft.world.level.ChunkPos, String> tilePredicates
   ) {
      return this.getTextures(matcher -> matcher.matches(summary, box), tilePredicates);
   }

   public Map<net.minecraft.world.level.ChunkPos, TileTexture> getTextures(
      WorldSummary summary,
      net.minecraft.world.level.levelgen.structure.BoundingBox box,
      List<net.minecraft.world.level.levelgen.structure.pools.JigsawJunction> junctions,
      Map<net.minecraft.world.level.ChunkPos, String> tilePredicates
   ) {
      return this.getTextures(matcher -> matcher.matches(summary, box, junctions), tilePredicates);
   }

   public Set<TileTexture> allTextures() {
      Set<TileTexture> outSet = new HashSet<>();
      this.matchers.values().forEach(outSet::addAll);
      return outSet;
   }

   static {
      CHUNK_MATCHERS.put(AntiqueAtlas.id("center"), StructureTileProvider.ChunkMatcher::center);
      CHUNK_MATCHERS.put(AntiqueAtlas.id("center_above_ground"), StructureTileProvider.ChunkMatcher::aboveGround);
      CHUNK_MATCHERS.put(AntiqueAtlas.id("center_top_above_ground"), StructureTileProvider.ChunkMatcher::topAboveGround);
      CHUNK_MATCHERS.put(AntiqueAtlas.id("center_horizontal"), StructureTileProvider.ChunkMatcher::centerIfHorizontal);
      CHUNK_MATCHERS.put(AntiqueAtlas.id("center_vertical"), StructureTileProvider.ChunkMatcher::centerIfVertical);
      CHUNK_MATCHERS.put(AntiqueAtlas.id("bridge_horizontal"), StructureTileProvider.ChunkMatcher::bridgeHorizontal);
      CHUNK_MATCHERS.put(AntiqueAtlas.id("bridge_vertical"), StructureTileProvider.ChunkMatcher::bridgeVertical);
      CHUNK_MATCHERS.put(AntiqueAtlas.id("path_straight_horizontal"), (summary, box) -> StructureTileProvider.ChunkJunctionMatcher.straightHorizontal(summary, box, List.of()));
      CHUNK_MATCHERS.put(AntiqueAtlas.id("path_straight_vertical"), (summary, box) -> StructureTileProvider.ChunkJunctionMatcher.straightVertical(summary, box, List.of()));
   }

   public interface ChunkJunctionMatcher extends StructureTileProvider.ChunkMatcher {
      @Override
      default Collection<net.minecraft.world.level.ChunkPos> matches(WorldSummary summary, net.minecraft.world.level.levelgen.structure.BoundingBox box) {
         return this.matches(summary, box, List.of());
      }

      @Override
      Collection<net.minecraft.world.level.ChunkPos> matches(
         WorldSummary var1,
         net.minecraft.world.level.levelgen.structure.BoundingBox var2,
         List<net.minecraft.world.level.levelgen.structure.pools.JigsawJunction> var3
      );

      static Collection<net.minecraft.world.level.ChunkPos> straightHorizontal(
         WorldSummary summary,
         net.minecraft.world.level.levelgen.structure.BoundingBox box,
         List<net.minecraft.world.level.levelgen.structure.pools.JigsawJunction> junctions
      ) {
         return junctions.size() != 2
               || ((JigsawJunction)junctions.get(0)).getSourceZ() != ((JigsawJunction)junctions.get(1)).getSourceZ()
                  && ((JigsawJunction)junctions.get(0)).getSourceX() == ((JigsawJunction)junctions.get(1)).getSourceX()
            ? List.of()
            : List.of(ChunkPos.containing(box.getCenter()));
      }

      static Collection<net.minecraft.world.level.ChunkPos> straightVertical(
         WorldSummary summary,
         net.minecraft.world.level.levelgen.structure.BoundingBox box,
         List<net.minecraft.world.level.levelgen.structure.pools.JigsawJunction> junctions
      ) {
         return junctions.size() != 2
               || ((JigsawJunction)junctions.get(0)).getSourceX() != ((JigsawJunction)junctions.get(1)).getSourceX()
                  && ((JigsawJunction)junctions.get(0)).getSourceZ() == ((JigsawJunction)junctions.get(1)).getSourceZ()
            ? List.of()
            : List.of(
               ChunkPos.containing(
                  new BlockPos(
                     (((JigsawJunction)junctions.get(0)).getSourceX() + ((JigsawJunction)junctions.get(1)).getSourceX()) / 2,
                     0,
                     (((JigsawJunction)junctions.get(0)).getSourceZ() + ((JigsawJunction)junctions.get(1)).getSourceZ()) / 2
                  )
               )
            );
      }
   }

   public interface ChunkMatcher {
      Collection<net.minecraft.world.level.ChunkPos> matches(WorldSummary var1, net.minecraft.world.level.levelgen.structure.BoundingBox var2);

      default Collection<net.minecraft.world.level.ChunkPos> matches(
         WorldSummary summary,
         net.minecraft.world.level.levelgen.structure.BoundingBox box,
         List<net.minecraft.world.level.levelgen.structure.pools.JigsawJunction> junctions
      ) {
         return this.matches(summary, box);
      }

      static Collection<net.minecraft.world.level.ChunkPos> center(WorldSummary summary, net.minecraft.world.level.levelgen.structure.BoundingBox box) {
         return Collections.singleton(ChunkPos.containing(box.getCenter()));
      }

      static Collection<net.minecraft.world.level.ChunkPos> topAboveGround(
         WorldSummary summary, net.minecraft.world.level.levelgen.structure.BoundingBox box
      ) {
         return (Collection<net.minecraft.world.level.ChunkPos>)(63 <= box.maxY()
            ? Collections.singleton(ChunkPos.containing(box.getCenter()))
            : Collections.emptyList());
      }

      static Collection<net.minecraft.world.level.ChunkPos> aboveGround(
         WorldSummary summary, net.minecraft.world.level.levelgen.structure.BoundingBox box
      ) {
         BlockPos center = new BlockPos(box.getCenter());
         return (Collection<net.minecraft.world.level.ChunkPos>)(59 <= center.getY()
            ? Collections.singleton(ChunkPos.containing(center))
            : Collections.emptyList());
      }

      static Collection<net.minecraft.world.level.ChunkPos> bridgeHorizontal(
         WorldSummary summary, net.minecraft.world.level.levelgen.structure.BoundingBox box
      ) {
         HashSet<net.minecraft.world.level.ChunkPos> matches = new HashSet<>();
         if (box.getXSpan() > 16) {
            int chunkZ = box.getCenter().getZ() >> 4;

            for (int x = box.minX(); x < box.maxX(); x += 16) {
               matches.add(new ChunkPos(x >> 4, chunkZ));
            }
         }

         return matches;
      }

      static Collection<net.minecraft.world.level.ChunkPos> bridgeVertical(
         WorldSummary summary, net.minecraft.world.level.levelgen.structure.BoundingBox box
      ) {
         HashSet<net.minecraft.world.level.ChunkPos> matches = new HashSet<>();
         if (box.getZSpan() > 16) {
            int chunkX = box.getCenter().getX() >> 4;

            for (int z = box.minZ(); z < box.maxZ(); z += 16) {
               matches.add(new ChunkPos(chunkX, z >> 4));
            }
         }

         return matches;
      }

      static Collection<net.minecraft.world.level.ChunkPos> centerIfHorizontal(
         WorldSummary summary, net.minecraft.world.level.levelgen.structure.BoundingBox box
      ) {
         return box.getXSpan() > box.getZSpan() ? Collections.singleton(ChunkPos.containing(box.getCenter())) : Collections.emptySet();
      }

      static Collection<net.minecraft.world.level.ChunkPos> centerIfVertical(
         WorldSummary summary, net.minecraft.world.level.levelgen.structure.BoundingBox box
      ) {
         return box.getZSpan() > box.getXSpan() ? Collections.singleton(ChunkPos.containing(box.getCenter())) : Collections.emptySet();
      }
   }
}
