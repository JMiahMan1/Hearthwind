package net.jmb19905.niftycarts.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class AssembledTexture {
   private final Identifier id;
   private final int width;
   private final int height;
   private final ObjectList<CartMaterial> materials = new ObjectArrayList();

   public AssembledTexture(Identifier id, int width, int height) {
      this.id = id;
      this.width = width;
      this.height = height;
   }

   public AssembledTexture add(CartMaterial material) {
      this.materials.add(material);
      return this;
   }

   public Identifier getId() {
      return this.id;
   }

   AbstractTexture assemble(AtlasManager sprites) {
      PreparedMaterial[] prepared = new PreparedMaterial[this.materials.size()];
      int resolution = 1;
      ObjectListIterator<CartMaterial> it = this.materials.iterator();

      while (it.hasNext()) {
         int i = it.nextIndex();
         PreparedMaterial p = ((CartMaterial)it.next()).prepare(sprites);
         prepared[i] = p;
         resolution = Math.max(resolution, p.getResolution());
      }

      NativeImage image = new NativeImage(this.width * resolution, this.height * resolution, true);

      for (PreparedMaterial p : prepared) {
         p.draw(image, resolution);
      }

      return new DynamicTexture(this.id::toString, image);
   }
}
