package net.smitherz.init;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.smitherz.data.UpgradeLoader;

public class LoaderInit {
   public static void init() {
      ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new UpgradeLoader());
   }
}
