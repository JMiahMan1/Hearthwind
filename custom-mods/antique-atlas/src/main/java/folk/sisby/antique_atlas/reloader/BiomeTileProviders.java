package folk.sisby.antique_atlas.reloader;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.AntiqueAtlasConfig;
import folk.sisby.antique_atlas.TerrainTileProvider;
import folk.sisby.antique_atlas.TileElevation;
import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.antique_atlas.util.ForgeTags;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.Nullable;

public class BiomeTileProviders extends SimplePreparableReloadListener<Map<Identifier,JsonElement>> implements IdentifiableResourceReloadListener {
   public static final BiomeTileProviders INSTANCE = new BiomeTileProviders();
   public static final Identifier ID = AntiqueAtlas.id("tile_provider/biome");
   protected final Map<net.minecraft.resources.Identifier, TerrainTileProvider> tileProviders = new HashMap<>();
   protected final Map<net.minecraft.resources.Identifier, net.minecraft.resources.Identifier> biomeFallbacks = new HashMap<>();
   protected boolean hasFallbacks = false;

   public static BiomeTileProviders getInstance() {
      return INSTANCE;
   }

   public BiomeTileProviders() {
      
   }

   public TerrainTileProvider getTileProvider(Identifier providerId) {
      return this.tileProviders
         .getOrDefault(
            providerId,
            this.tileProviders
               .getOrDefault(
                  this.biomeFallbacks.get(providerId),
                  AntiqueAtlas.CONFIG.fallbackFailHandling == AntiqueAtlasConfig.FallbackHandling.PLAINS
                        && !providerId.equals(Biomes.PLAINS.identifier())
                     ? this.getTileProvider(Biomes.PLAINS.identifier())
                     : TerrainTileProvider.DEFAULT
               )
         );
   }

   public void registerFallbacks(net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry) {
      for (Biome biome : biomeRegistry) {
         Identifier biomeId = biomeRegistry.getKey(biome);
         if (!this.tileProviders.containsKey(biomeId)) {
            Identifier fallbackBiome = getFallbackBiome(biomeRegistry.wrapAsHolder(biome));
            if (fallbackBiome != null && this.tileProviders.containsKey(fallbackBiome)) {
               this.biomeFallbacks.put(biomeId, fallbackBiome);
               AntiqueAtlas.LOGGER
                  .info("[Antique Atlas] Set fallback biome for {} to {}. You can set a more fitting texture using a resource pack!", biomeId, fallbackBiome);
            } else if (fallbackBiome != null) {
               AntiqueAtlas.LOGGER.error("[Antique Atlas] Fallback biome for {} is {}, which has no defined tile provider.", biomeId, fallbackBiome);
            } else {
               AntiqueAtlas.LOGGER
                  .warn(
                     "[Antique Atlas] No fallback could be found for {}. This shouldn't happen! This means the biome is not in ANY conventional or vanilla tag on the client!",
                     biomeId
                  );
               if (AntiqueAtlas.CONFIG.fallbackFailHandling == AntiqueAtlasConfig.FallbackHandling.CRASH) {
                  throw new IllegalStateException(
                     "Antique Atlas fallback biome registration failed! Fix the missing biome or change fallbackFailHandling in antique_atlas.toml"
                  );
               }
            }
         }
      }

      this.hasFallbacks = true;
   }

   public void clearFallbacks() {
      this.hasFallbacks = false;
      this.biomeFallbacks.clear();
   }

   public boolean hasFallbacks() {
      return this.hasFallbacks;
   }

   public static net.minecraft.resources.Identifier getFallbackBiome(net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome) {
      if (biome.is(ConventionalBiomeTags.IS_VOID)
         || false
         || biome.is(ForgeTags.Biomes.IS_VOID)) {
         return Biomes.THE_VOID.identifier();
      } else if (!biome.is(ConventionalBiomeTags.IS_END)
         && !biome.is(BiomeTags.IS_END)
         && true
         && true) {
         if (biome.is(ConventionalBiomeTags.IS_NETHER_FOREST)
            || false) {
            return Biomes.WARPED_FOREST.identifier();
         } else if (biome.is(ConventionalBiomeTags.IS_NETHER)
            || biome.is(BiomeTags.IS_NETHER)
            || false) {
            return Biomes.SOUL_SAND_VALLEY.identifier();
         } else if (biome.is(ConventionalBiomeTags.IS_SWAMP)
            || false
            || biome.is(ForgeTags.Biomes.IS_SWAMP)) {
            return Biomes.SWAMP.identifier();
         } else if (!biome.is(ConventionalBiomeTags.IS_OCEAN)
            && !biome.is(ConventionalBiomeTags.IS_RIVER)
            && !biome.is(BiomeTags.IS_OCEAN)
            && !biome.is(BiomeTags.IS_DEEP_OCEAN)
            && true
            && true
            && true
            && !biome.is(BiomeTags.IS_RIVER)
            && true
            && true
            && true
            && !biome.is(ForgeTags.Biomes.IS_WATER)) {
            if (biome.is(ConventionalBiomeTags.IS_STONY_SHORES)
               || false) {
               return Biomes.STONY_SHORE.identifier();
            } else if (biome.is(ConventionalBiomeTags.IS_BEACH)
               || biome.is(BiomeTags.IS_BEACH)
               || false) {
               return Biomes.BEACH.identifier();
            } else if (biome.is(ConventionalBiomeTags.IS_JUNGLE_TREE)
               || biome.is(ConventionalBiomeTags.IS_JUNGLE)
               || biome.is(BiomeTags.IS_JUNGLE)
               || false
               || false) {
               return Biomes.JUNGLE.identifier();
            } else if (biome.is(ConventionalBiomeTags.IS_FLOWER_FOREST)
               || biome.is(ConventionalBiomeTags.IS_FLORAL)
               || false
               || false) {
               return Biomes.FLOWER_FOREST.identifier();
            } else if (biome.is(ConventionalBiomeTags.IS_SAVANNA_TREE)
               || biome.is(ConventionalBiomeTags.IS_SAVANNA)
               || biome.is(BiomeTags.IS_SAVANNA)
               || false
               || false) {
               return Biomes.SAVANNA.identifier();
            } else if (biome.is(ConventionalBiomeTags.IS_BADLANDS)
               || biome.is(BiomeTags.IS_BADLANDS)
               || false
               || false) {
               return Biomes.BADLANDS.identifier();
            } else if (!biome.is(ConventionalBiomeTags.IS_CONIFEROUS_TREE)
               && !biome.is(ConventionalBiomeTags.IS_TAIGA)
               && true
               && !biome.is(ForgeTags.Biomes.IS_CONIFEROUS)
               && !biome.is(BiomeTags.IS_TAIGA)
               && true) {
               if (biome.is(ConventionalBiomeTags.IS_OLD_GROWTH)) {
                  return Biomes.BIRCH_FOREST.identifier();
               } else if (biome.is(ConventionalBiomeTags.IS_BIRCH_FOREST)
                  || biome.is(ConventionalBiomeTags.IS_DECIDUOUS_TREE)
                  || false
                  || false) {
                  return Biomes.BIRCH_FOREST.identifier();
               } else if (biome.is(ConventionalBiomeTags.IS_FOREST)
                  || biome.is(BiomeTags.IS_FOREST)
                  || false) {
                  return Biomes.FOREST.identifier();
               } else if (!biome.is(ConventionalBiomeTags.IS_SNOWY_PLAINS)
                  && !biome.is(ConventionalBiomeTags.IS_PLAINS)
                  && true
                  && true
                  && !biome.is(ForgeTags.Biomes.IS_PLAINS)
                  && true
                  && !biome.is(ForgeTags.Biomes.IS_SNOWY)) {
                  if (biome.is(ConventionalBiomeTags.IS_WASTELAND)
                     || biome.is(ConventionalBiomeTags.IS_DEAD)
                     || biome.is(ConventionalBiomeTags.IS_DESERT)
                     || false
                     || false
                     || false
                     || biome.is(ForgeTags.Biomes.IS_SANDY)
                     || biome.is(ForgeTags.Biomes.IS_DESERT)
                     || biome.is(ForgeTags.Biomes.IS_DEAD)
                     || biome.is(ForgeTags.Biomes.IS_WASTELAND)) {
                     return Biomes.DESERT.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_ICY)
                     || false) {
                     return Biomes.FROZEN_OCEAN.identifier();
                  } else if (biome.is(ForgeTags.Biomes.IS_PLATEAU)) {
                     return Biomes.MEADOW.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_WINDSWEPT)
                     || false
                     || false) {
                     return Biomes.WINDSWEPT_HILLS.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_MOUNTAIN_PEAK)
                     || false
                     || biome.is(ForgeTags.Biomes.IS_PEAK)) {
                     return Biomes.JAGGED_PEAKS.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_MOUNTAIN_SLOPE)
                     || biome.is(ConventionalBiomeTags.IS_MOUNTAIN)
                     || biome.is(BiomeTags.IS_MOUNTAIN)
                     || false
                     || false
                     || biome.is(ForgeTags.Biomes.IS_SLOPE)
                     || biome.is(ForgeTags.Biomes.IS_MOUNTAIN)) {
                     return Biomes.STONY_PEAKS.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_MUSHROOM)
                     || false
                     || biome.is(ForgeTags.Biomes.IS_MUSHROOM)) {
                     return Biomes.MUSHROOM_FIELDS.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_HILL) || biome.is(BiomeTags.IS_HILL)) {
                     return Biomes.WINDSWEPT_GRAVELLY_HILLS.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_UNDERGROUND)
                     || biome.is(ConventionalBiomeTags.IS_CAVE)
                     || false
                     || false
                     || biome.is(ForgeTags.Biomes.IS_UNDERGROUND)
                     || biome.is(ForgeTags.Biomes.IS_CAVE)) {
                     return Biomes.DRIPSTONE_CAVES.identifier();
                  } else if (biome.is(ForgeTags.Biomes.IS_SPOOKY)) {
                     return Biomes.DARK_FOREST.identifier();
                  } else if (biome.is(ForgeTags.Biomes.IS_MAGICAL)) {
                     return Biomes.MUSHROOM_FIELDS.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_VEGETATION_DENSE)
                     || false
                     || biome.is(ForgeTags.Biomes.IS_DENSE)) {
                     return Biomes.FOREST.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_VEGETATION_SPARSE)
                     || false
                     || biome.is(ForgeTags.Biomes.IS_SPARSE)) {
                     return Biomes.PLAINS.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_HOT)
                     || false
                     || biome.is(ForgeTags.Biomes.IS_HOT)) {
                     return Biomes.DESERT.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_COLD)
                     || false
                     || biome.is(ForgeTags.Biomes.IS_COLD)) {
                     return Biomes.SNOWY_PLAINS.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_TEMPERATE)
                     || false) {
                     return Biomes.PLAINS.identifier();
                  } else if (biome.is(ConventionalBiomeTags.IS_DRY)
                     || false
                     || biome.is(ForgeTags.Biomes.IS_DRY)) {
                     return Biomes.BADLANDS.identifier();
                  } else {
                     return !biome.is(ConventionalBiomeTags.IS_WET)
                           && true
                           && !biome.is(ForgeTags.Biomes.IS_WET)
                        ? null
                        : Biomes.SWAMP.identifier();
                  }
               } else {
                  return !biome.is(ConventionalBiomeTags.IS_ICY)
                        && !biome.is(ConventionalBiomeTags.IS_SNOWY_PLAINS)
                        && !biome.is(ConventionalBiomeTags.IS_SNOWY)
                        && true
                        && true
                        && true
                     ? Biomes.PLAINS.identifier()
                     : Biomes.SNOWY_PLAINS.identifier();
               }
            } else {
               return !biome.is(ConventionalBiomeTags.IS_SNOWY)
                     && !biome.is(ConventionalBiomeTags.IS_ICY)
                     && true
                     && true
                  ? Biomes.TAIGA.identifier()
                  : Biomes.SNOWY_TAIGA.identifier();
            }
         } else {
            return !biome.is(ConventionalBiomeTags.IS_AQUATIC_ICY)
                  && !biome.is(ConventionalBiomeTags.IS_ICY)
                  && true
                  && true
               ? Biomes.RIVER.identifier()
               : Biomes.FROZEN_RIVER.identifier();
         }
      } else {
         return !biome.is(ConventionalBiomeTags.IS_VEGETATION_SPARSE)
               && !biome.is(ConventionalBiomeTags.IS_VEGETATION_DENSE)
               && true
               && true
               && !biome.is(ForgeTags.Biomes.IS_LUSH)
            ? Biomes.END_BARRENS.identifier()
            : Biomes.END_HIGHLANDS.identifier();
      }
   }

   public static TileTexture getTexture(
      Map<net.minecraft.resources.Identifier, TileTexture> textures, net.minecraft.resources.Identifier id
   ) {
      if (textures.containsKey(id)) {
         return textures.get(id);
      } else {
         throw new IllegalStateException("texture %s is not present!".formatted(id));
      }
   }

   @Nullable
   public static List<TileTexture> resolveTextureJson(Map<net.minecraft.resources.Identifier, TileTexture> textures, JsonElement textureJson) {
      if (textureJson instanceof JsonPrimitive texturePrimitive && texturePrimitive.isString()) {
         return List.of(getTexture(textures, Identifier.tryParse(texturePrimitive.getAsString())));
      } else if (textureJson instanceof JsonArray textureArray) {
         return textureArray.asList().stream().map(je -> getTexture(textures, Identifier.tryParse(je.getAsString()))).toList();
      } else if (textureJson instanceof JsonObject textureObject
         && textureObject.keySet().stream().allMatch(k -> textureObject.get(k) instanceof JsonPrimitive jp && jp.isNumber())) {
         Multiset<TileTexture> outList = HashMultiset.create();
         textureObject.entrySet()
            .forEach(e -> outList.add(getTexture(textures, Identifier.tryParse((String)e.getKey())), ((JsonElement)e.getValue()).getAsInt()));
         return outList.stream().toList();
      } else {
         return null;
      }
   }
   @Override
   protected Map<Identifier, JsonElement> prepare(net.minecraft.server.packs.resources.ResourceManager manager, net.minecraft.util.profiling.ProfilerFiller profiler) {
      Map<Identifier, JsonElement> map = new java.util.HashMap<>();
      Gson gson = new Gson();
      for (java.util.Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> e : manager.listResources("atlas/biome", p -> p.getPath().endsWith(".json")).entrySet()) {
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
      AntiqueAtlas.LOGGER.info("[Antique Atlas] Reloading Biome Tile Providers...");
      Map<net.minecraft.resources.Identifier, TileTexture> textures = TileTextures.getInstance().getTextures();
      Set<TileTexture> unusedTextures = new HashSet<>(textures.values().stream().filter(t -> t.id().getPath().startsWith("biome")).toList());
      Map<net.minecraft.resources.Identifier, net.minecraft.resources.Identifier> providerParents = new HashMap<>();

      for (Entry<net.minecraft.resources.Identifier, JsonElement> fileEntry : prepared.entrySet()) {
         Identifier fileId = (Identifier)fileEntry.getKey();

         try {
            JsonObject fileJson = fileEntry.getValue().getAsJsonObject();
            if (fileJson.has("parent")) {
               Identifier parentId = Identifier.tryParse(fileJson.getAsJsonPrimitive("parent").getAsString());
               providerParents.put(fileId, parentId);
            } else {
               JsonElement textureJson = fileJson.get("textures");
               List<TileTexture> defaultTextures = resolveTextureJson(textures, textureJson);
               if (defaultTextures != null) {
                  defaultTextures.forEach(unusedTextures::remove);
                  this.tileProviders.put(fileId, new TerrainTileProvider(fileId, defaultTextures));
               } else {
                  JsonObject textureObject = textureJson.getAsJsonObject();
                  Map<TileElevation, List<TileTexture>> textureElevations = new HashMap<>();
                  Set<TileElevation> skippedElevations = new HashSet<>();
                  List<TileTexture> elevationTextures = null;

                  for (TileElevation elevation : TileElevation.values()) {
                     if (!textureObject.has(elevation.getName())) {
                        skippedElevations.add(elevation);
                     } else {
                        elevationTextures = resolveTextureJson(textures, textureObject.get(elevation.getName()));
                        if (elevationTextures == null) {
                           throw new IllegalStateException("Malformed object %s in textures object!".formatted(elevation.getName()));
                        }

                        elevationTextures.forEach(unusedTextures::remove);
                        textureElevations.put(elevation, elevationTextures);

                        for (TileElevation skipped : skippedElevations) {
                           textureElevations.put(skipped, elevationTextures);
                        }

                        skippedElevations.clear();
                     }
                  }

                  if (textureElevations.isEmpty()) {
                     throw new IllegalStateException("No elevation keys were found in the textures object!");
                  }

                  for (TileElevation elevationx : skippedElevations) {
                     textureElevations.put(elevationx, elevationTextures);
                  }

                  this.tileProviders.put(fileId, new TerrainTileProvider(fileId, textureElevations));
               }
            }
         } catch (Exception var23) {
            AntiqueAtlas.LOGGER.error("[Antique Atlas] Error reading biome tile provider {}!", fileId, var23);
         }
      }

      providerParents.forEach(
         (id, parentId) -> {
            if (this.tileProviders.containsKey(parentId)) {
               this.tileProviders.put((net.minecraft.resources.Identifier)id, this.tileProviders.get(parentId));
            } else {
               AntiqueAtlas.LOGGER
                  .error(
                     "[Antique Atlas] Error reading biome tile provider {}!", id, new IllegalStateException("Parent id %s doesn't exist".formatted(parentId))
                  );
            }
         }
      );

      for (TileTexture texture : unusedTextures) {
         if (!texture.displayId().startsWith("test") && !texture.displayId().startsWith("base")) {
            AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} isn't referenced by any biome tile provider!", texture.displayId());
         }
      }
   }

   public Identifier getFabricId() {
      return ID;
   }

   public Collection<net.minecraft.resources.Identifier> getFabricDependencies() {
      return List.of(TileTextures.ID);
   }
}
