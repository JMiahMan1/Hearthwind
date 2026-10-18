package net.jmb19905.niftycarts.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Environment(EnvType.CLIENT)
public class PreparedMaterial {
   private final ObjectList<Fill> fills;
   private final TextureAtlasSprite sprite;
   private final int resolution;

   PreparedMaterial(ObjectList<Fill> fills, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite, int resolution) {
      this.fills = fills;
      this.sprite = sprite;
      this.resolution = resolution;
   }

   int getResolution() {
      return this.resolution;
   }

   void draw(NativeImage image, int resolution) {
      ObjectListIterator var3 = this.fills.iterator();

      while (var3.hasNext()) {
         Fill m = (Fill)var3.next();
         m.fill(image, this.sprite, this.resolution, resolution);
      }
   }
}
