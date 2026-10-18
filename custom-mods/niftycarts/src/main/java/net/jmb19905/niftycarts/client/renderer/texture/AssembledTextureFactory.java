package net.jmb19905.niftycarts.client.renderer.texture;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class AssembledTextureFactory {
   private final Object2ObjectMap<net.minecraft.resources.Identifier, AssembledTexture> textures = new Object2ObjectOpenHashMap();

   public AssembledTextureFactory add(AssembledTexture texture) {
      this.textures.put(texture.getId(), texture);
      return this;
   }

   public void bake() {
      Minecraft mc = Minecraft.getInstance();
      ResourceManager resources = mc.getResourceManager();
      TextureManager textures = mc.getTextureManager();
      AtlasManager sprites = mc.getAtlasManager();
      Object2ObjectMaps.fastForEach(this.textures, e -> {
         if (resources.getResource((Identifier)e.getKey()).isPresent()) {
            textures.release((Identifier)e.getKey());
         } else {
            textures.register((Identifier)e.getKey(), ((AssembledTexture)e.getValue()).assemble(sprites));
         }
      });
   }
}
