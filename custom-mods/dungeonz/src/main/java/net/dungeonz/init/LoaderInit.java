package net.dungeonz.init;

import net.dungeonz.data.DungeonLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class LoaderInit {

    public static void init() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new DungeonLoader());
    }
}
