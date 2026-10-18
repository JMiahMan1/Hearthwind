package folk.sisby.antique_atlas;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public record TerrainTileProvider(Identifier id, Map<TileElevation, List<TileTexture>> textures) {
   public static TerrainTileProvider DEFAULT = new TerrainTileProvider(AntiqueAtlas.id("default"), List.of(TileTexture.DEFAULT));

   public TerrainTileProvider(net.minecraft.resources.Identifier id, List<TileTexture> textures) {
      this(id, Arrays.stream(TileElevation.values()).collect(Collectors.toMap(e -> (TileElevation)e, e -> textures)));
   }

   public TileTexture getTexture(ChunkPos pos, @Nullable TileElevation elevation) {
      int variation = (int)(Mth.getSeed(pos.x(), pos.z(), pos.x() * pos.z()) & 2147483647L);
      TileElevation usedElevation = elevation == null ? TileElevation.VALLEY : elevation;
      return this.textures.get(usedElevation).get(variation % this.textures.get(usedElevation).size());
   }
}
