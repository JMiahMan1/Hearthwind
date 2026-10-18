package net.jmb19905.niftycarts.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Environment(EnvType.CLIENT)
public class Fill {
   private final int x;
   private final int y;
   private final int width;
   private final int height;
   private final int[][] rot;
   private final int u;
   private final int v;

   Fill(int x, int y, int width, int height, int[][] rot, int u, int v) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.rot = rot;
      this.u = u;
      this.v = v;
   }

   void fill(NativeImage image, TextureAtlasSprite sprite, int resolution, int outResolution) {
      int r = outResolution / resolution;
      int x1 = (this.x + this.width) * resolution;
      int y1 = (this.y + this.height) * resolution;
      int u0 = this.u * resolution;
      int v0 = this.v * resolution;

      for (int y = this.y * resolution; y < y1; y++) {
         for (int dy = 0; dy < r; dy++) {
            for (int x = this.x * resolution; x < x1; x++) {
               for (int dx = 0; dx < r; dx++) {
                  int u1 = x * this.rot[0][0] + y * this.rot[0][1] + Math.min(this.rot[0][0], this.rot[0][1]) + u0;
                  int v1 = x * this.rot[1][0] + y * this.rot[1][1] + Math.min(this.rot[1][0], this.rot[1][1]) + v0;
                  int rgba = sprite.contents()
                     .originalImage
                     .getPixel(Math.floorMod(u1, sprite.contents().width()), Math.floorMod(v1, sprite.contents().height()));
                  image.setPixel(r * x + dx, r * y + dy, rgba);
               }
            }
         }
      }
   }
}
