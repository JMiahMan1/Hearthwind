package net.jmb19905.niftycarts.client.renderer.texture;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class CartMaterial {
   public static final int[][] R0 = new int[][]{{1, 0}, {0, 1}};
   public static final int[][] R90 = new int[][]{{0, -1}, {1, 0}};
   public static final int[][] R180 = new int[][]{{-1, 0}, {-1, 0}};
   public static final int[][] R270 = new int[][]{{0, 1}, {-1, 0}};
   private final Identifier sprite;
   private final int size;
   private final ObjectList<Fill> fills = new ObjectArrayList();

   public CartMaterial(Identifier sprite, int size) {
      this.sprite = sprite;
      this.size = size;
   }

   public CartMaterial fill(int x, int y, int width, int height) {
      return this.fill(x, y, width, height, R0);
   }

   public CartMaterial fill(int x, int y, int width, int height, int[][] rot) {
      return this.fill(x, y, width, height, rot, 0, 0);
   }

   public CartMaterial fill(int x, int y, int width, int height, int[][] rot, int u, int v) {
      this.fills.add(new Fill(x, y, width, height, rot, u, v));
      return this;
   }

   PreparedMaterial prepare(AtlasManager sprites) {
      TextureAtlasSprite sprite = sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, this.sprite));
      return new PreparedMaterial(this.fills, sprite, sprite.contents().width() / this.size);
   }
}
