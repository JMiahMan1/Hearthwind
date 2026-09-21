package net.satisfy.farm_and_charm.client;

import net.minecraft.resources.Identifier;
import net.satisfy.farm_and_charm.client.renderer.block.*;
import net.satisfy.farm_and_charm.core.registry.StorageTypeRegistry;

public class ClientStorageTypes {
    public static void registerStorageType(Identifier location, StorageTypeRenderer renderer) {
        StorageBlockEntityRenderer.registerStorageType(location, renderer);
    }

    public static void init() {
        registerStorageType(StorageTypeRegistry.TOOL_RACK, new ToolRackRenderer());
        registerStorageType(StorageTypeRegistry.WINDOW_SILL, new WindowSillRenderer());
        registerStorageType(StorageTypeRegistry.CHICKEN_NEST, new ChickenNestRenderer());
    }
}
