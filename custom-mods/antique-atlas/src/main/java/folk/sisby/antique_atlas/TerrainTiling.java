package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.BiomeTileProviders;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.terrain.ChunkSummary;
import folk.sisby.surveyor.terrain.WorldTerrain;
import folk.sisby.surveyor.terrain.LayerSummary.Raw;
import folk.sisby.surveyor.util.RegistryPalette;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class TerrainTiling {
   public static final int EMPTY_PRIORITY = 16;
   public static final int RAVINE_PRIORITY = 12;
   public static final int LAVA_PRIORITY = 6;
   public static final int WATER_PRIORITY = 4;
   public static final int ICE_PRIORITY = 3;
   public static final int BEACH_PRIORITY = 3;
   public static final List<net.minecraft.resources.Identifier> CUSTOM_TILES = List.of(
      FeatureTiles.BEDROCK_ROOF,
      FeatureTiles.EMPTY,
      FeatureTiles.END_VOID,
      FeatureTiles.WATER,
      FeatureTiles.ICE,
      FeatureTiles.TILE_RAVINE,
      FeatureTiles.SWAMP_WATER,
      FeatureTiles.TILE_LAVA,
      FeatureTiles.TILE_LAVA_SHORE
   );
   public static final int NETHER_SCAN_HEIGHT = 50;
   public static final Map<net.minecraft.world.level.biome.Biome, Integer> priorityCache = new Reference2IntArrayMap();
   public static final Map<net.minecraft.world.level.biome.Biome, Boolean> swampCache = new Reference2BooleanArrayMap();
   private static final int SEA_LEVEL = 63;

   public static int priorityForBiome(
      net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry, net.minecraft.world.level.biome.Biome biome
   ) {
      return priorityCache.computeIfAbsent(biome, b -> {
         net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeEntry = biomeRegistry.wrapAsHolder(biome);
         if (biomeEntry.is(BiomeTags.IS_BEACH)) {
            return 3;
         } else {
            return biomeEntry.is(BiomeTags.IS_NETHER) ? 2 : 1;
         }
      });
   }

   public static boolean isSwamp(
      net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry, net.minecraft.world.level.biome.Biome biome
   ) {
      return swampCache.computeIfAbsent(biome, b -> biomeRegistry.wrapAsHolder(b).is(ConventionalBiomeTags.IS_SWAMP));
   }

   public static Pair<TerrainTileProvider, TileElevation> frequencyToTexture(
      int[][] possibleTiles,
      net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry,
      net.minecraft.core.IdMap<net.minecraft.world.level.biome.Biome> biomePalette
   ) {
      int elevationOrdinal = -1;
      int biomeIndex = -1;
      int bestFrequency = 0;

      for (int i = 0; i < possibleTiles.length; i++) {
         for (int j = 0; j < possibleTiles[i].length; j++) {
            if (possibleTiles[i][j] > bestFrequency) {
               elevationOrdinal = i;
               biomeIndex = j;
               bestFrequency = possibleTiles[i][j];
            }
         }
      }

      if (bestFrequency == 0) {
         return null;
      } else {
         int customTileIndex = biomeIndex - possibleTiles[0].length + CUSTOM_TILES.size();
         Identifier providerId = customTileIndex >= 0
            ? (Identifier)CUSTOM_TILES.get(customTileIndex)
            : biomeRegistry.getKey((Biome)biomePalette.byId(biomeIndex));
         if (providerId == null) {
            throw new RuntimeException(
               customTileIndex >= 0
                  ? "Custom tile index %s was out of bounds for size %s!".formatted(customTileIndex, CUSTOM_TILES.size())
                  : "Biome ID was null at index %s and instance %S!".formatted(biomeIndex, biomePalette.byId(biomeIndex))
            );
         } else {
            return Pair.of(
               BiomeTileProviders.getInstance().getTileProvider(providerId),
               elevationOrdinal == TileElevation.values().length ? null : TileElevation.values()[elevationOrdinal]
            );
         }
      }
   }

   public static Pair<TerrainTileProvider, TileElevation> terrainToTile(WorldSummary summary, net.minecraft.world.level.ChunkPos pos) {
      int defaultTile = CUSTOM_TILES.indexOf(summary.dimension() == Level.END ? FeatureTiles.END_VOID : FeatureTiles.EMPTY);
      boolean checkRavines = summary.dimension() == Level.OVERWORLD;
      int topY = 999;
      WorldTerrain terrain = summary.terrain();
      if (terrain == null) {
         return null;
      } else {
         ChunkSummary chunk = terrain.get(pos);
         if (chunk == null) {
            return null;
         } else {
            Raw lithograph = chunk.toSingleLayer(null, null, topY);
            RegistryPalette<net.minecraft.world.level.biome.Biome>.ValueView biomePalette = terrain.getBiomePalette(pos);
            RegistryPalette<net.minecraft.world.level.block.Block>.ValueView blockPalette = terrain.getBlockPalette(pos);
            net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry = biomePalette.registry();
            if (lithograph == null) {
               return Pair.of(BiomeTileProviders.getInstance().getTileProvider((Identifier)CUSTOM_TILES.get(defaultTile)), null);
            } else {
               int elevationSize = TileElevation.values().length;
               int elevationCount = elevationSize + 1;
               int biomeCount = biomePalette.size();
               int baseTileCount = biomeCount + CUSTOM_TILES.size();
               int[][] possibleTiles = new int[elevationCount][baseTileCount];

               for (int i = 0; i < lithograph.depths().length; i++) {
                  if (!lithograph.exists().get(i)) {
                     possibleTiles[elevationSize][defaultTile] = possibleTiles[elevationSize][defaultTile] + 16;
                  } else {
                     int height = topY - lithograph.depths()[i] + lithograph.waterDepths()[i];
                     Block block = (Block)blockPalette.byId(lithograph.blocks()[i]);
                     Biome biome = (Biome)biomePalette.byId(lithograph.biomes()[i]);
                     if (checkRavines && height - 63 < -7) {
                        possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(FeatureTiles.TILE_RAVINE)] += 12;
                     } else if (lithograph.waterDepths()[i] > 0) {
                        possibleTiles[elevationSize][biomeCount
                           + CUSTOM_TILES.indexOf(isSwamp(biomeRegistry, biome) ? FeatureTiles.SWAMP_WATER : FeatureTiles.WATER)] += 4;
                     } else if (block == Blocks.ICE) {
                        possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(FeatureTiles.ICE)] += 3;
                     } else if (block == Blocks.LAVA) {
                        possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(FeatureTiles.TILE_LAVA)] += 6;
                     }

                     possibleTiles[TileElevation.fromBlocksAboveSea(height - 63).ordinal()][lithograph.biomes()[i]] += priorityForBiome(biomeRegistry, biome);
                  }
               }

               return frequencyToTexture(possibleTiles, biomeRegistry, biomePalette);
            }
         }
      }
   }

   public static Pair<TerrainTileProvider, TileElevation> terrainToTileNether(WorldSummary summary, net.minecraft.world.level.ChunkPos pos) {
      int defaultTile = CUSTOM_TILES.indexOf(FeatureTiles.BEDROCK_ROOF);
      int topY = 999;
      int logicalTopY = 126;
      WorldTerrain terrain = summary.terrain();
      if (terrain == null) {
         return null;
      } else {
         ChunkSummary chunk = terrain.get(pos);
         if (chunk == null) {
            return null;
         } else {
            Raw lowLithograph = chunk.toSingleLayer(null, 50, topY);
            Raw fullLithograph = chunk.toSingleLayer(null, logicalTopY, topY);
            RegistryPalette<net.minecraft.world.level.biome.Biome>.ValueView biomePalette = terrain.getBiomePalette(pos);
            RegistryPalette<net.minecraft.world.level.block.Block>.ValueView blockPalette = terrain.getBlockPalette(pos);
            net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry = biomePalette.registry();
            int elevationSize = TileElevation.values().length;
            int elevationCount = elevationSize + 1;
            int biomeCount = biomePalette.size();
            int baseTileCount = biomeCount + CUSTOM_TILES.size();
            int[][] possibleTiles = new int[elevationCount][baseTileCount];
            if (fullLithograph == null) {
               return Pair.of(BiomeTileProviders.getInstance().getTileProvider((Identifier)CUSTOM_TILES.get(defaultTile)), null);
            } else {
               int SEA_DEPTH = topY - 31;
               if (lowLithograph == null) {
                  for (int i = 0; i < fullLithograph.depths().length; i++) {
                     if (!fullLithograph.exists().get(i)) {
                        possibleTiles[elevationSize][defaultTile] = possibleTiles[elevationSize][defaultTile] + 16;
                     } else {
                        Biome biome = (Biome)biomePalette.byId(fullLithograph.biomes()[i]);
                        possibleTiles[elevationSize][fullLithograph.biomes()[i]] += priorityForBiome(biomeRegistry, biome);
                     }
                  }
               } else {
                  for (int ix = 0; ix < lowLithograph.depths().length; ix++) {
                     if (lowLithograph.exists().get(ix) && lowLithograph.depths()[ix] <= SEA_DEPTH) {
                        Block block = (Block)blockPalette.byId(lowLithograph.blocks()[ix]);
                        if (block == Blocks.LAVA) {
                           possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(FeatureTiles.TILE_LAVA)] += 6;
                        } else {
                           possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(FeatureTiles.TILE_LAVA_SHORE)] += 3;
                        }
                     } else {
                        Biome biome = (Biome)biomePalette.byId(fullLithograph.biomes()[ix]);
                        possibleTiles[elevationSize][fullLithograph.biomes()[ix]] += priorityForBiome(biomeRegistry, biome);
                     }
                  }
               }

               return frequencyToTexture(possibleTiles, biomeRegistry, biomePalette);
            }
         }
      }
   }
}
