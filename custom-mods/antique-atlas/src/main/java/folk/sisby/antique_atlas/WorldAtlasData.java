package folk.sisby.antique_atlas;

import com.google.common.collect.Multimap;
import folk.sisby.antique_atlas.gui.AtlasScreen;
import folk.sisby.antique_atlas.reloader.BiomeTileProviders;
import folk.sisby.antique_atlas.reloader.MarkerTextures;
import folk.sisby.antique_atlas.reloader.StructureTileProviders;
import folk.sisby.antique_atlas.reloader.TileTextures;
import folk.sisby.antique_atlas.util.Rect;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClient;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentMap;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import folk.sisby.surveyor.util.RegionPos;
import it.unimi.dsi.fastutil.Pair;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class WorldAtlasData {
   public static final Map<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>, WorldAtlasData> WORLDS = new HashMap<>();
   protected final Map<net.minecraft.world.level.ChunkPos, TileTexture> biomeTiles = new HashMap<>();
   protected final Map<net.minecraft.world.level.ChunkPos, TileTexture> structureTiles = new HashMap<>();
   protected final Map<UUID, Map<net.minecraft.resources.Identifier, Pair<Landmark, MarkerTexture>>> landmarkMarkers = new ConcurrentHashMap<>();
   protected final Map<Landmark, MarkerTexture> structureMarkers = new ConcurrentHashMap<>();
   protected final Rect tileScope = new Rect(0, 0, 0, 0);
   protected final Set<net.minecraft.world.level.ChunkPos> terrainDequeHash = new HashSet<>();
   protected final Deque<net.minecraft.world.level.ChunkPos> terrainDeque = new ConcurrentLinkedDeque<>();
   protected boolean isFinished = false;
   protected final Map<net.minecraft.world.level.ChunkPos, String> debugBiomePredicates = new HashMap<>();
   protected final Map<net.minecraft.world.level.ChunkPos, String> debugStructurePredicates = new HashMap<>();
   protected final Map<net.minecraft.world.level.ChunkPos, TerrainTileProvider> debugBiomes = new HashMap<>();
   protected final Map<net.minecraft.world.level.ChunkPos, StructureTileProvider> debugStructures = new HashMap<>();

   public static WorldAtlasData getOrCreate(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
      return WORLDS.computeIfAbsent(dimension, k -> new WorldAtlasData());
   }

   public static boolean isEmpty(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
      return !WORLDS.containsKey(dimension) || WORLDS.get(dimension).isEmpty();
   }

   private boolean isEmpty() {
      return this.tileScope.maxY == this.tileScope.minY && this.terrainDequeHash.isEmpty();
   }

   public void onTerrainUpdated(WorldSummary summary, Map<RegionPos, BitSet> chunks) {
      for (ChunkPos pos : RegionPos.regionsToChunks(chunks)) {
         if (!this.biomeTiles.containsKey(pos) && !this.terrainDequeHash.contains(pos)) {
            this.terrainDequeHash.add(pos);
            this.terrainDeque.add(pos);
         }
      }
   }

   public void onStructuresAdded(
      WorldSummary summary,
      Multimap<net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure>, net.minecraft.world.level.ChunkPos> starts
   ) {
      starts.forEach(
         (key, pos) -> StructureTileProviders.getInstance()
            .resolve(
               this.structureTiles,
               this.debugStructures,
               this.debugStructurePredicates,
               this.structureMarkers,
               summary,
               key,
               pos,
               summary.structures().get(key, pos),
               summary.structures().getType(key),
               summary.structures().getTags(key)
            )
      );
   }

   public void tick(WorldSummary summary) {
      if (BiomeTileProviders.getInstance().hasFallbacks()) {
         RegistryAccess manager = Minecraft.getInstance().getConnection().registryAccess();

         for (int i = 0; i < AntiqueAtlas.CONFIG.chunkTickLimit; i++) {
            ChunkPos pos = (ChunkPos)this.terrainDeque.pollFirst();
            this.terrainDequeHash.remove(pos);
            if (pos == null) {
               break;
            }

            Pair<TerrainTileProvider, TileElevation> tile = summary.dimension() == Level.NETHER
               ? TerrainTiling.terrainToTileNether(summary, pos)
               : TerrainTiling.terrainToTile(summary, pos);
            if (tile != null) {
               this.tileScope.extendTo(pos.x(), pos.z());
               this.biomeTiles.put(pos, ((TerrainTileProvider)tile.left()).getTexture(pos, (TileElevation)tile.right()));
               this.debugBiomes.put(pos, (TerrainTileProvider)tile.left());
               this.debugBiomePredicates.put(pos, tile.right() == null ? null : ((TileElevation)tile.right()).getName());
            }
         }

         if (!this.isFinished && this.terrainDeque.isEmpty()) {
            this.isFinished = true;
            AntiqueAtlas.LOGGER.info("[Antique Atlas] Finished loading terrain for {} - {} tiles.", summary.dimension(), this.biomeTiles.size());
         }
      }
   }

   public Rect getScope() {
      return this.tileScope;
   }

   public TileTexture getTile(int x, int z) {
      return this.getTile(new ChunkPos(x, z));
   }

   public TileTexture getTile(ChunkPos pos) {
      if (!this.biomeTiles.containsKey(pos)) {
         return AntiqueAtlas.CONFIG.emptyHandling == AntiqueAtlasConfig.EmptyHandling.CLOUDS
            ? TileTextures.getInstance().getTextures().get(AntiqueAtlas.id("clouds"))
            : null;
      } else {
         return this.structureTiles.getOrDefault(pos, this.biomeTiles.get(pos));
      }
   }

   public Identifier getProvider(ChunkPos pos) {
      if (this.structureTiles.containsKey(pos)) {
         return this.debugStructures.get(pos).id();
      } else {
         return this.debugBiomes.containsKey(pos) ? this.debugBiomes.get(pos).id() : null;
      }
   }

   public String getTilePredicate(ChunkPos pos) {
      return this.structureTiles.containsKey(pos) ? this.debugStructurePredicates.get(pos) : this.debugBiomePredicates.get(pos);
   }

   public void addLandmarkMarker(Landmark landmark, MarkerTexture texture) {
      this.landmarkMarkers.computeIfAbsent(landmark.owner(), t -> new ConcurrentHashMap<>()).put(landmark.id(), Pair.of(landmark, texture));
   }

   public static Landmark copyLandmarkWith(Landmark landmark, net.minecraft.resources.Identifier id, Consumer<LandmarkComponentMap> modifier) {
      LandmarkComponentMap copy = LandmarkComponentMap.builder().build();
      landmark.components().keySet().forEach(t -> copy.set(t, landmark.components().get(t)));
      modifier.accept(copy);
      return new Landmark(landmark.owner(), id, copy);
   }

   public void addLandmark(Landmark landmark) {
      if (landmark != null) {
         if (landmark.id().getPath().startsWith("grave")) {
            AntiqueAtlasConfig.GraveStyle style = AntiqueAtlas.CONFIG.graveStyle;
            Component name = (Component)landmark.get(LandmarkComponentTypes.NAME);
            if (name == null && style == AntiqueAtlasConfig.GraveStyle.CAUSE) {
               style = AntiqueAtlasConfig.GraveStyle.DIED;
            }

            MutableComponent timeText = Component.literal(String.valueOf(1L + (Long)landmark.getOrDefault(LandmarkComponentTypes.TIME, 0L) / 24000L))
               .withStyle(ChatFormatting.WHITE);
            String key = "gui.antique_atlas.marker.death.%s".formatted(style.toString().toLowerCase());

            MutableComponent text = switch (style) {
               case CAUSE -> Component.translatable(
                     key, new Object[]{name.copy().withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.RED), timeText}
                  )
                  .withStyle(ChatFormatting.GRAY);
               case GRAVE, ITEMS, DIED -> Component.translatable(
                     key,
                     new Object[]{
                        Component.translatable("gui.antique_atlas.marker.death.%s.verb".formatted(style.toString().toLowerCase()))
                           .withStyle(ChatFormatting.RED),
                        timeText
                     }
                  )
                  .withStyle(ChatFormatting.GRAY);
               case EUPHEMISMS -> Component.translatable(
                     key,
                     new Object[]{
                        Component.translatable(
                              "gui.antique_atlas.marker.death.%s.verb.%s"
                                 .formatted(
                                    style.toString().toLowerCase(),
                                    new Random(((Integer)landmark.getOrDefault(LandmarkComponentTypes.SEED, 0)).intValue()).nextInt(11)
                                 )
                           )
                           .withStyle(ChatFormatting.RED),
                        timeText
                     }
                  )
                  .withStyle(ChatFormatting.GRAY);
            };
            this.addLandmarkMarker(copyLandmarkWith(landmark, landmark.id(), m -> {
               m.set(LandmarkComponentTypes.COLOR, DyeColor.GRAY.getTextureDiffuseColor());
               m.set(LandmarkComponentTypes.NAME, text);
            }), MarkerTextures.getInstance().fromLandmark(landmark, style == AntiqueAtlasConfig.GraveStyle.ITEMS ? "items" : null));
         } else {
            this.addLandmarkMarker(landmark, MarkerTextures.getInstance().fromLandmark(landmark));
         }
      }
   }

   public void onLandmarksAdded(WorldSummary summary, Multimap<UUID, net.minecraft.resources.Identifier> landmarks) {
      landmarks.forEach((type, pos) -> this.addLandmark(summary.landmarks().get(type, pos)));
      if (Minecraft.getInstance().gui.screen() instanceof AtlasScreen as) {
         as.updateBookmarkerList();
      }
   }

   public void onLandmarksRemoved(WorldSummary summary, Multimap<UUID, net.minecraft.resources.Identifier> landmarks) {
      landmarks.forEach((type, pos) -> {
         if (this.landmarkMarkers.containsKey(type)) {
            this.landmarkMarkers.get(type).remove(pos);
            if (this.landmarkMarkers.get(type).isEmpty()) {
               this.landmarkMarkers.remove(type);
            }
         }
      });
      if (Minecraft.getInstance().gui.screen() instanceof AtlasScreen as) {
         as.updateBookmarkerList();
      }
   }

   public boolean deleteLandmark(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, Landmark landmark) {
      WorldSummary summary = SurveyorClient.tryGetSummary(dimension);
      if (summary != null && summary.landmarks() != null && !landmark.owner().equals(WorldLandmarks.GLOBAL) && SurveyorClient.canModify(landmark.owner())) {
         summary.landmarks().remove(landmark.owner(), landmark.id());
         return true;
      } else {
         return false;
      }
   }

   public Map<Landmark, MarkerTexture> getEditableLandmarks() {
      Map<Landmark, MarkerTexture> map = new HashMap<>();
      this.landmarkMarkers.forEach((type, landmarks) -> landmarks.forEach((pos, pair) -> {
         if (!((Landmark)pair.left()).owner().equals(WorldLandmarks.GLOBAL) && SurveyorClient.canModify(((Landmark)pair.left()).owner())) {
            map.put((Landmark)pair.left(), (MarkerTexture)pair.right());
         }
      }));
      return map;
   }

   public Map<Landmark, MarkerTexture> getAllMarkers(int tileChunks) {
      Map<Landmark, MarkerTexture> map = new HashMap<>();
      this.landmarkMarkers.forEach((type, landmarks) -> landmarks.forEach((pos, pair) -> map.put((Landmark)pair.left(), (MarkerTexture)pair.right())));
      this.structureMarkers.forEach((landmark, texture) -> {
         if (tileChunks >= texture.nearClip() && tileChunks <= texture.farClip()) {
            map.put(landmark, texture);
         }
      });
      return map;
   }

   public MarkerTexture getMarkerTexture(Landmark landmark) {
      return this.landmarkMarkers.containsKey(landmark.owner()) && this.landmarkMarkers.get(landmark.owner()).containsKey(landmark.id())
         ? (MarkerTexture)this.landmarkMarkers.get(landmark.owner()).get(landmark.id()).right()
         : this.structureMarkers.get(landmark);
   }

   public boolean isLoading() {
      return this.terrainDequeHash.size() > 20;
   }
}
