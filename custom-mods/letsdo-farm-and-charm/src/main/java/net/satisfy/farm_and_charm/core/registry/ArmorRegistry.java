package net.satisfy.farm_and_charm.core.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.satisfy.farm_and_charm.client.model.DungareesLeggingsModel;

import java.util.HashMap;
import java.util.Map;

// 26.2: pose copying moved into ArmorRenderer.submitTransformCopyingModel;
// this registry keeps one baked dungarees model per item, same as before.
@Environment(EnvType.CLIENT)
public class ArmorRegistry {
    private static final Map<Item, DungareesLeggingsModel> leggingsModels = new HashMap<>();

    public static DungareesLeggingsModel getLeggingsModel(Item item) {
        if (item != ObjectRegistry.DUNGAREES.get()) return null;
        return leggingsModels.computeIfAbsent(item, key -> new DungareesLeggingsModel(
                Minecraft.getInstance().getEntityModels().bakeLayer(DungareesLeggingsModel.LAYER_LOCATION)));
    }
}
