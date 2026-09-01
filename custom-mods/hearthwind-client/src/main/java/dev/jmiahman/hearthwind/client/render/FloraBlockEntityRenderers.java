package dev.jmiahman.hearthwind.client.render;

import dev.jmiahman.hearthwind.flora.blockentity.FloraBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.Identifier;

/**
 * Registers all authentic 3D Block Entity Renderers for Let's Do mods.
 */
public final class FloraBlockEntityRenderers {

    private FloraBlockEntityRenderers() {}

    public static void registerAll() {
        // Register Storage Block Entity Renderer
        BlockEntityRenderers.register(FloraBlockEntities.STORAGE, StorageBlockEntityRenderer::new);

        // Vinery Wine Racks & Shelves
        NineBottleRenderer nineBottle = new NineBottleRenderer();
        FourBottleRenderer fourBottle = new FourBottleRenderer();

        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("vinery", "wine_rack_small"), fourBottle);
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("vinery", "wine_rack_mid"), fourBottle);
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("vinery", "wine_rack_big"), nineBottle);
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("vinery", "wine_box"), nineBottle);
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("vinery", "shelf"), fourBottle);

        // Meadow Cheese Racks
        CheeseRackRenderer cheeseRack = new CheeseRackRenderer();
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("meadow", "cheese_rack"), cheeseRack);
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("meadow", "cheese_form"), cheeseRack);

        // Candlelight Dining Table Sets & Sideboards
        TableSetRenderer tableSet = new TableSetRenderer();
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("candlelight", "side_table"), tableSet);
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("candlelight", "table"), tableSet);
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("candlelight", "tray"), tableSet);

        // Bakery Cake Stands & Trays
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("bakery", "cake_stand"), tableSet);
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("bakery", "tray"), tableSet);
        StorageBlockEntityRenderer.registerStorageType(Identifier.fromNamespaceAndPath("bakery", "bread_box"), fourBottle);
    }
}
