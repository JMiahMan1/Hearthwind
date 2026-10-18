package folk.sisby.antique_atlas.reloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.StructureTileProvider;
import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import folk.sisby.surveyor.structure.JigsawPieceSummary;
import folk.sisby.surveyor.structure.StructurePieceSummary;
import folk.sisby.surveyor.structure.StructureStartSummary;
import it.unimi.dsi.fastutil.Pair;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

public class StructureTileProviders extends SimplePreparableReloadListener<Map<Identifier,JsonElement>> implements IdentifiableResourceReloadListener {
   public static final StructureTileProviders INSTANCE = new StructureTileProviders();
   public static final Identifier ID = AntiqueAtlas.id("structures");
   protected final Map<net.minecraft.resources.Identifier, StructureTileProvider> startTiles = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, StructureTileProvider> typeTiles = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, StructureTileProvider> tagTiles = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, StructureTileProvider> pieceTypeTiles = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, StructureTileProvider> pieceJigsawSingleTiles = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, StructureTileProvider> pieceJigsawFeatureTiles = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, MarkerTexture> startMarkers = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, MarkerTexture> typeMarkers = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, MarkerTexture> tagMarkers = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, MarkerTexture> pieceTypeMarkers = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, MarkerTexture> pieceJigsawSingleMarkers = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, MarkerTexture> pieceJigsawFeatureMarkers = new HashMap<>();
   protected final Map<StructureTileProviders.ProviderType, Pair<Map<net.minecraft.resources.Identifier, StructureTileProvider>, Map<net.minecraft.resources.Identifier, MarkerTexture>>> PROVIDER_MAPS = Map.of(
      StructureTileProviders.ProviderType.START,
      Pair.of(this.startTiles, this.startMarkers),
      StructureTileProviders.ProviderType.TYPE,
      Pair.of(this.typeTiles, this.typeMarkers),
      StructureTileProviders.ProviderType.TAG,
      Pair.of(this.tagTiles, this.tagMarkers),
      StructureTileProviders.ProviderType.PIECE_TYPE,
      Pair.of(this.pieceTypeTiles, this.pieceTypeMarkers),
      StructureTileProviders.ProviderType.JIGSAW_SINGLE,
      Pair.of(this.pieceJigsawSingleTiles, this.pieceJigsawSingleMarkers),
      StructureTileProviders.ProviderType.JIGSAW_FEATURE,
      Pair.of(this.pieceJigsawFeatureTiles, this.pieceJigsawFeatureMarkers)
   );

   public static StructureTileProviders getInstance() {
      return INSTANCE;
   }

   public StructureTileProviders() {
      
   }

   public void resolve(
      Map<net.minecraft.world.level.ChunkPos, TileTexture> outTiles,
      Map<net.minecraft.world.level.ChunkPos, StructureTileProvider> structureProviders,
      Map<net.minecraft.world.level.ChunkPos, String> tilePredicates,
      StructurePieceSummary piece,
      WorldSummary summary
   ) {
      if (piece instanceof JigsawPieceSummary jigsawPiece) {
         if (this.pieceJigsawSingleTiles.containsKey(jigsawPiece.getId())) {
            StructureTileProvider provider = (jigsawPiece.getElementType() == StructurePoolElementType.FEATURE
                  ? this.pieceJigsawFeatureTiles
                  : this.pieceJigsawSingleTiles)
               .get(jigsawPiece.getId());
            provider.getTextures(summary, jigsawPiece.getBoundingBox(), jigsawPiece.getJunctions(), tilePredicates).forEach((pos, texture) -> {
               if (!structureProviders.containsKey(pos) || structureProviders.get(pos).priority() >= provider.priority()) {
                  outTiles.put((net.minecraft.world.level.ChunkPos)pos, texture);
                  structureProviders.put((net.minecraft.world.level.ChunkPos)pos, provider);
               }
            });
         }
      } else {
         Identifier pieceTypeId = BuiltInRegistries.STRUCTURE_PIECE.getKey(piece.getType());
         if (this.pieceTypeTiles.containsKey(pieceTypeId)) {
            StructureTileProvider provider = this.pieceTypeTiles.get(pieceTypeId);
            provider.getTextures(summary, piece.getBoundingBox(), tilePredicates).forEach((pos, texture) -> {
               if (!structureProviders.containsKey(pos) || structureProviders.get(pos).priority() >= provider.priority()) {
                  outTiles.put((net.minecraft.world.level.ChunkPos)pos, texture);
                  structureProviders.put((net.minecraft.world.level.ChunkPos)pos, provider);
               }
            });
         }
      }
   }

   public void resolve(
      Map<net.minecraft.world.level.ChunkPos, TileTexture> outTiles,
      Map<net.minecraft.world.level.ChunkPos, StructureTileProvider> structureProviders,
      Map<net.minecraft.world.level.ChunkPos, String> debugPredicates,
      Map<Landmark, MarkerTexture> outMarkers,
      WorldSummary summary,
      net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure> key,
      net.minecraft.world.level.ChunkPos pos,
      StructureStartSummary start,
      net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.StructureType<?>> type,
      Collection<net.minecraft.tags.TagKey<net.minecraft.world.level.levelgen.structure.Structure>> tags
   ) {
      if (this.startMarkers.containsKey(key.identifier())) {
         MarkerTexture texture = this.startMarkers.get(key.identifier());
         outMarkers.put(
            Landmark.create(
               WorldLandmarks.GLOBAL,
               key.identifier().withPath(p -> "start/" + p + "/" + pos.x() + "/" + pos.z()),
               b -> b.add(LandmarkComponentTypes.POS, pos.getMiddleBlockPosition(0))
                  .add(LandmarkComponentTypes.NAME, Component.translatable(StructureTileProviders.ProviderType.START.translation(key.identifier())))
            ),
            texture
         );
      } else if (type != null && this.typeMarkers.containsKey(type.identifier())) {
         MarkerTexture texture = this.typeMarkers.get(type.identifier());
         outMarkers.put(
            Landmark.create(
               WorldLandmarks.GLOBAL,
               key.identifier().withPath(p -> "start/" + p + "/" + pos.x() + "/" + pos.z()),
               b -> b.add(LandmarkComponentTypes.POS, pos.getMiddleBlockPosition(0))
                  .add(LandmarkComponentTypes.NAME, Component.translatable(StructureTileProviders.ProviderType.TYPE.translation(type.identifier())))
            ),
            texture
         );
      } else {
         this.tagMarkers
            .entrySet()
            .stream()
            .filter(entry -> tags.contains(TagKey.create(Registries.STRUCTURE, (Identifier)entry.getKey())))
            .findFirst()
            .ifPresent(
               entry -> outMarkers.put(
                  Landmark.create(
                     WorldLandmarks.GLOBAL,
                     key.identifier().withPath(p -> "start/" + p + "/" + pos.x() + "/" + pos.z()),
                     b -> b.add(LandmarkComponentTypes.POS, pos.getMiddleBlockPosition(0))
                        .add(
                           LandmarkComponentTypes.NAME,
                           Component.translatable(StructureTileProviders.ProviderType.TAG.translation((Identifier)entry.getKey()))
                        )
                  ),
                  entry.getValue()
               )
            );
      }

      if (this.startTiles.containsKey(key.identifier())) {
         StructureTileProvider provider = this.startTiles.get(key.identifier());
         provider.getTextures(summary, start.getBoundingBox(), debugPredicates).forEach((pos2, texture) -> {
            if (!structureProviders.containsKey(pos) || structureProviders.get(pos).priority() >= provider.priority()) {
               outTiles.put((net.minecraft.world.level.ChunkPos)pos2, texture);
               structureProviders.put((net.minecraft.world.level.ChunkPos)pos2, provider);
            }
         });
      } else if (type != null && this.typeTiles.containsKey(type.identifier())) {
         StructureTileProvider provider = this.typeTiles.get(key.identifier());
         provider.getTextures(summary, start.getBoundingBox(), debugPredicates).forEach((pos2, texture) -> {
            if (!structureProviders.containsKey(pos) || structureProviders.get(pos).priority() >= provider.priority()) {
               outTiles.put((net.minecraft.world.level.ChunkPos)pos2, texture);
               structureProviders.put((net.minecraft.world.level.ChunkPos)pos2, provider);
            }
         });
      } else {
         tags.stream().filter(t -> this.tagTiles.containsKey(t.location())).findFirst().ifPresent(tag -> {
            StructureTileProvider providerx = this.tagTiles.get(tag.location());
            providerx.getTextures(summary, start.getBoundingBox(), debugPredicates).forEach((pos2, texture) -> {
               if (!structureProviders.containsKey(pos) || structureProviders.get(pos).priority() >= providerx.priority()) {
                  outTiles.put((net.minecraft.world.level.ChunkPos)pos2, texture);
                  structureProviders.put((net.minecraft.world.level.ChunkPos)pos2, providerx);
               }
            });
         });
      }

      start.getChildren().forEach(p -> this.resolve(outTiles, structureProviders, debugPredicates, p, summary));
   }
   @Override
   protected Map<Identifier, JsonElement> prepare(net.minecraft.server.packs.resources.ResourceManager manager, net.minecraft.util.profiling.ProfilerFiller profiler) {
      Map<Identifier, JsonElement> map = new java.util.HashMap<>();
      Gson gson = new Gson();
      for (java.util.Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> e : manager.listResources("atlas/structure", p -> p.getPath().endsWith(".json")).entrySet()) {
         try (java.io.BufferedReader reader = e.getValue().openAsReader()) {
            map.put(e.getKey(), gson.fromJson(reader, JsonElement.class));
         } catch (Exception ex) {
            throw new IllegalStateException("Couldn't read " + e.getKey(), ex);
         }
      }
      return map;
   }


   protected void apply(
      Map<net.minecraft.resources.Identifier, JsonElement> prepared,
      net.minecraft.server.packs.resources.ResourceManager manager,
      net.minecraft.util.profiling.ProfilerFiller profiler
   ) {
      AntiqueAtlas.LOGGER.info("[Antique Atlas] Reloading Structure Tile / Marker Providers...");
      Map<net.minecraft.resources.Identifier, TileTexture> textures = TileTextures.getInstance().getTextures();
      Set<TileTexture> unusedTextures = new HashSet<>(textures.values().stream().filter(t -> t.id().getPath().startsWith("structure")).toList());
      this.PROVIDER_MAPS.values().forEach(p -> ((Map)p.left()).clear());
      this.PROVIDER_MAPS.values().forEach(p -> ((Map)p.right()).clear());

      for (Entry<net.minecraft.resources.Identifier, JsonElement> fileEntry : prepared.entrySet()) {
         Identifier fileId = (Identifier)fileEntry.getKey();
         this.PROVIDER_MAPS
            .forEach(
               (providerType, pair) -> {
                  Map<net.minecraft.resources.Identifier, StructureTileProvider> providerMap = (Map<net.minecraft.resources.Identifier, StructureTileProvider>)pair.left();
                  Map<net.minecraft.resources.Identifier, MarkerTexture> markerMap = (Map<net.minecraft.resources.Identifier, MarkerTexture>)pair.right();
                  if (fileId.getPath().startsWith(providerType.prefix())) {
                     Identifier id = Identifier.fromNamespaceAndPath(fileId.getNamespace(), fileId.getPath().substring(providerType.prefix().length()));

                     try {
                        JsonObject fileJson = fileEntry.getValue().getAsJsonObject();
                        if (fileJson.has("textures")) {
                           JsonElement textureJson = fileJson.get("textures");
                           int priority = fileJson.has("priority") ? fileJson.get("priority").getAsInt() : 999;
                           List<TileTexture> defaultTextures = BiomeTileProviders.resolveTextureJson(textures, textureJson);
                           if (defaultTextures != null) {
                              StructureTileProvider provider = new StructureTileProvider(id, priority, defaultTextures);
                              providerMap.put(provider.id(), provider);
                              unusedTextures.removeAll(provider.allTextures());
                           } else {
                              JsonObject textureObject = textureJson.getAsJsonObject();
                              Map<StructureTileProvider.ChunkMatcher, List<TileTexture>> matchers = new HashMap<>();

                              for (String matcherKey : textureObject.keySet()) {
                                 Identifier matcherId = AntiqueAtlas.id(matcherKey);
                                 StructureTileProvider.ChunkMatcher matcher = StructureTileProvider.getChunkMatcher(matcherId);
                                 if (matcher == null) {
                                    throw new IllegalStateException("Matcher %s does not exist!".formatted(matcherId.toString()));
                                 }

                                 List<TileTexture> matcherTextures = BiomeTileProviders.resolveTextureJson(textures, textureObject.get(matcherKey));
                                 if (matcherTextures == null) {
                                    throw new IllegalStateException("Malformed object %s in textures object!".formatted(matcherId.toString()));
                                 }

                                 matcherTextures.forEach(unusedTextures::remove);
                                 matchers.put(matcher, matcherTextures);
                              }

                              if (matchers.isEmpty()) {
                                 throw new IllegalStateException("No matcher keys were found in the textures object!");
                              }

                              StructureTileProvider provider = new StructureTileProvider(id, priority, matchers);
                              providerMap.put(provider.id(), provider);
                              unusedTextures.removeAll(provider.allTextures());
                           }
                        }

                        if (fileJson.has("markers")) {
                           JsonElement markerJson = fileJson.get("markers");
                           Identifier markerTextureId = Identifier.tryParse(markerJson.getAsString());
                           MarkerTexture texturex = MarkerTextures.getInstance().asMap().get(markerTextureId);
                           if (texturex == null) {
                              throw new IllegalStateException("Marker texture %s does not exist!".formatted(markerTextureId));
                           }

                           AntiqueAtlas.CONFIG.structureMarkers.putIfAbsent(fileId.toString(), true);
                           if (AntiqueAtlas.CONFIG.structureMarkers.get(fileId.toString())) {
                              markerMap.put(id, texturex);
                           }
                        }
                     } catch (Exception var20) {
                        AntiqueAtlas.LOGGER.error("[Antique Atlas] Error reading structure tile provider {}!", fileId, var20);
                     }
                  }
               }
            );
      }

      for (TileTexture texture : unusedTextures) {
         AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} isn't referenced by any structure tile provider!", texture.displayId());
      }
   }

   public Identifier getFabricId() {
      return ID;
   }

   public Collection<net.minecraft.resources.Identifier> getFabricDependencies() {
      return List.of(TileTextures.ID, MarkerTextures.ID);
   }

   public static enum ProviderType {
      START("start"),
      TAG("tag"),
      TYPE("type"),
      PIECE_TYPE("piece/type"),
      JIGSAW_SINGLE("piece/jigsaw/single"),
      JIGSAW_FEATURE("piece/jigsaw/feature");

      public final String key;

      private ProviderType(String key) {
         this.key = key;
      }

      public String prefix() {
         return this.key + "/";
      }

      public String translation(Identifier id) {
         return "structure.%s.%s".formatted(this.key.replace('/', '.'), id.toString().replace(':', '.'));
      }
   }
}
