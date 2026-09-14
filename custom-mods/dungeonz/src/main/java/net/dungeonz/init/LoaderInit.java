package net.dungeonz.init;

import net.dungeonz.data.DungeonLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.class_3264;

public class LoaderInit {

    public static void init() {
        ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new DungeonLoader());
    }
}
