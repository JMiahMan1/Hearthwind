package folk.sisby.antique_atlas;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;

public record TileTexture(
   Identifier id, boolean innerBorder, Set<TileTexture> tilesTo, Set<TileTexture> tilesToHorizontal, Set<TileTexture> tilesToVertical
) {
   public static final TileTexture DEFAULT = empty(
      AntiqueAtlas.id(AntiqueAtlas.CONFIG.fallbackFailHandling == AntiqueAtlasConfig.FallbackHandling.TEST ? "test" : "missing"), false
   );

   public static TileTexture empty(Identifier id, boolean innerBorder) {
      return new TileTexture(
         Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/atlas/tile/%s.png".formatted(id.getPath())),
         innerBorder,
         new ReferenceOpenHashSet(),
         new ReferenceOpenHashSet(),
         new ReferenceOpenHashSet()
      );
   }

   public String displayId() {
      Identifier trimmed = this.id.withPath(p -> p.substring("textures/atlas/tile/".length(), this.id.getPath().length() - 4));
      return this.id.getNamespace().equals("antique_atlas") ? trimmed.getPath() : trimmed.toString();
   }

   public boolean tiles(TileTexture other) {
      return this == other
         || this.innerBorder ^ (this.tilesTo.contains(other) || this.tilesToHorizontal.contains(other) || this.tilesToVertical.contains(other));
   }

   public boolean tilesHorizontally(TileTexture other) {
      return this == other || this.innerBorder ^ (this.tilesTo.contains(other) || this.tilesToHorizontal.contains(other));
   }

   public boolean tilesVertically(TileTexture other) {
      return this == other || this.innerBorder ^ (this.tilesTo.contains(other) || this.tilesToVertical.contains(other));
   }

   public record Builder(
      Identifier id,
      boolean innerBorder,
      Set<net.minecraft.resources.Identifier> tilesTo,
      Set<net.minecraft.resources.Identifier> tilesToHorizontal,
      Set<net.minecraft.resources.Identifier> tilesToVertical
   ) {
      public void build(Map<net.minecraft.resources.Identifier, TileTexture> emptyTextures) {
         if (!this.tilesTo.isEmpty()) {
            emptyTextures.get(this.id).tilesTo.addAll(this.tilesTo.stream().map(emptyTextures::get).collect(Collectors.toSet()));
         }

         if (!this.tilesToHorizontal.isEmpty()) {
            emptyTextures.get(this.id).tilesToHorizontal.addAll(this.tilesToHorizontal.stream().map(emptyTextures::get).collect(Collectors.toSet()));
         }

         if (!this.tilesToVertical.isEmpty()) {
            emptyTextures.get(this.id).tilesToVertical.addAll(this.tilesToVertical.stream().map(emptyTextures::get).collect(Collectors.toSet()));
         }
      }
   }
}
