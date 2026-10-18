package folk.sisby.antique_atlas;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class AntiqueAtlasKeybindings {
   public static final KeyMapping ATLAS_KEYMAPPING = new KeyMapping("key.antique_atlas.open", Type.KEYSYM, 77, KeyMapping.Category.register(Identifier.fromNamespaceAndPath("antique_atlas", "antique_atlas")));

   public static void init() {
            ClientTickEvents.END_CLIENT_TICK.register(AntiqueAtlasKeybindings::onClientTick);
   }

   public static void onClientTick(Minecraft client) {
      while (ATLAS_KEYMAPPING.consumeClick()) {
         AntiqueAtlas.openAtlasScreen();
      }
   }
}
