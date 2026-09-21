package net.satisfy.bakery.core.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.satisfy.bakery.Bakery;

import java.util.Set;

public class StorageTypeRegistry {
    public static final Identifier CAKE_STAND = Bakery.identifier("cake_stand");
    public static final Identifier TRAY = Bakery.identifier("tray");
    public static final Identifier BREADBOX = Bakery.identifier("breadbox");
    public static final Identifier CAKE_DISPLAY = Bakery.identifier("cake_display");
    public static final Identifier CUPCAKE_DISPLAY = Bakery.identifier("cupcake_display");
    public static final Identifier WALL_DISPLAY = Bakery.identifier("wall_display");

    public static Set<Block> registerBlocks(Set<Block> blocks) {
        blocks.add(ObjectRegistry.CAKE_STAND.get());
        blocks.add(ObjectRegistry.TRAY.get());
        blocks.add(ObjectRegistry.BREADBOX.get());
        blocks.add(ObjectRegistry.CAKE_DISPLAY.get());
        blocks.add(ObjectRegistry.CUPCAKE_DISPLAY.get());
        blocks.add(ObjectRegistry.WALL_DISPLAY.get());
        return blocks;
    }
}