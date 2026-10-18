package folk.sisby.antique_atlas;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.api.WrappedConfig.Section;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;
import folk.sisby.kaleido.lib.quiltconfig.impl.Comments;
import folk.sisby.surveyor.client.SurveyorClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceKey;

public class AntiqueAtlasConfig extends WrappedConfig {
   @Comments({@Comment({"Whether to display the map in full-screen"}), @Comment({"The background is slightly less stylish, but more tiles are shown at once"})})
   public boolean fullscreen = true;
   @Comment({"Whether to require an item to display the map"})
   public boolean requireItem = false;
   @Comment({"Whether to keep scale after closing the map"})
   public boolean keepZoom = false;
   @Comment({"Whether to keep offset after closing the map"})
   public boolean keepOffset = false;
   @Comment({"How to depict player death locations."})
   public AntiqueAtlasConfig.GraveStyle graveStyle = AntiqueAtlasConfig.GraveStyle.EUPHEMISMS;
   @Comments({@Comment({"The maximum number of chunks to represent as a tile, as a power of 2"}), @Comment({"Effectively the 'minimum zoom'"}), @Comment({"0: 1x1 chunk = 1 tile | 6: 64x64 chunks = 1 tile"})})
   @IntegerRange(
      min = 0L,
      max = 6L
   )
   public int maxTileChunks = 5;
   @Comments({@Comment({"The maximum size to render a tile at, as a power of 2 multiplier"}), @Comment({"Effectively the 'maximum zoom'"}), @Comment({"0: 1 tile = 16x16 | 3: 1 tile = 128x128"})})
   @IntegerRange(
      min = 0L,
      max = 3L
   )
   public int maxTilePixels = 1;
   @Comments({@Comment({"The effective GUI scale for tiles and markers - independent of the overall GUI scale."}), @Comment({"0 will match your GUI scale - pixels will be the same size as the background & buttons"}), @Comment({"-1 will use half your GUI scale, rounding up."}), @Comment({"-2 will use half your GUI scale, rounding down."})})
   @IntegerRange(
      min = -2L,
      max = 10L
   )
   public int mapScale = 0;
   @Comment({"The maximum number of chunks to load onto the map per tick after entering a world"})
   public int chunkTickLimit = 100;
   @Comment({"How to handle biomes that aren't in any minecraft, conventional, or forge biome tags"})
   public AntiqueAtlasConfig.FallbackHandling fallbackFailHandling = AntiqueAtlasConfig.FallbackHandling.MISSING;
   @Comment({"How to display areas that aren't explored yet"})
   public AntiqueAtlasConfig.EmptyHandling emptyHandling = AntiqueAtlasConfig.EmptyHandling.EMPTY;
   public Map<String, Boolean> structureMarkers = ValueMap.builder(true).put("minecraft:type/end_city", false).build();
   @Comment({"Options to adjust map behaviour for custom or modified dimensions."})
   public AntiqueAtlasConfig.Dimensions dimensions = new AntiqueAtlasConfig.Dimensions();

   public static class Dimensions implements Section {
      @Comments({@Comment({"Cycle order and coordinate scales of each dimension."}), @Comment({"If not 0, the relative position of the player will be shown."})})
      public Map<String, Integer> scales = ValueMap.builder(0).put("minecraft:overworld", 8).put("minecraft:the_nether", 1).put("minecraft:the_end", 0).build();

      public List<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>> getOrder(
         net.minecraft.client.multiplayer.ClientPacketListener handler
      ) {
         List<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>> dims = new ArrayList<>(
            SurveyorClient.getSummaries(handler).keySet().stream().sorted(Comparator.comparing(ResourceKey::toString)).toList()
         );
         dims.removeIf(WorldAtlasData::isEmpty);
         this.scales.keySet().removeIf(v -> handler.levels().stream().noneMatch(d -> d.identifier().toString().equals(v)));
         dims.stream().filter(dim -> !this.scales.containsKey(dim.identifier().toString())).forEach(dim -> this.scales.put(dim.identifier().toString(), 0));
         return dims.stream().sorted(Comparator.comparing(dim -> this.scales.keySet().stream().toList().indexOf(dim.identifier().toString()))).toList();
      }

      public Map<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>, Integer> getScales(
         net.minecraft.client.multiplayer.ClientPacketListener handler
      ) {
         List<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>> dims = new ArrayList<>(
            SurveyorClient.getSummaries(handler).keySet().stream().sorted(Comparator.comparing(ResourceKey::toString)).toList()
         );
         dims.removeIf(WorldAtlasData::isEmpty);
         this.scales.keySet().removeIf(v -> handler.levels().stream().noneMatch(d -> d.identifier().toString().equals(v)));
         dims.stream().filter(dim -> !this.scales.containsKey(dim.identifier().toString())).forEach(dim -> this.scales.put(dim.identifier().toString(), 0));
         return dims.stream().collect(Collectors.toMap(k -> k, k -> this.scales.get(k.identifier().toString())));
      }
   }

   public static enum EmptyHandling {
      CLOUDS,
      EMPTY;
   }

   public static enum FallbackHandling {
      TEST,
      MISSING,
      PLAINS,
      CRASH;
   }

   public static enum GraveStyle {
      CAUSE,
      GRAVE,
      ITEMS,
      DIED,
      EUPHEMISMS;
   }
}
