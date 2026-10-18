package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.ITreeChopAPIProvider;
import ht.treechop.api.TreeChopAPI;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents.TagsLoaded;
import net.fabricmc.loader.api.FabricLoader;

public class TreeChopFabricAPITest {
   private static TreeChopAPI api = null;

   public static void init() {
      FabricLoader.getInstance().getObjectShare().whenAvailable("treechop:api_provider", (key, value) -> {
         if (value instanceof ITreeChopAPIProvider provider) {
            api = provider.get("treechop");
            CommonLifecycleEvents.TAGS_LOADED.register((TagsLoaded)(registries, client) -> onTagsUpdated());
         }
      });
   }

   public static void onTagsUpdated() {
      if (api != null) {
         TreeChop.initUsingAPI(api);
      }
   }
}
