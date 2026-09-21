package net.satisfy.candlelight.core.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.satisfy.candlelight.Candlelight;

import java.util.Set;

public class StorageTypeRegistry {
    public static final Identifier SHELF = Candlelight.identifier("shelf");
    public static final Identifier TABLE_SET = Candlelight.identifier("table_set");
    public static final Identifier JEWELRY_BOX = Candlelight.identifier("jewelry_box");

    public static Set<Block> registerBlocks(Set<Block> blocks) {
        blocks.add(ObjectRegistry.OAK_SHELF.get());
        blocks.add(ObjectRegistry.SPRUCE_SHELF.get());
        blocks.add(ObjectRegistry.BIRCH_SHELF.get());
        blocks.add(ObjectRegistry.ACACIA_SHELF.get());
        blocks.add(ObjectRegistry.JUNGLE_SHELF.get());
        blocks.add(ObjectRegistry.DARK_OAK_SHELF.get());
        blocks.add(ObjectRegistry.MANGROVE_SHELF.get());
        blocks.add(ObjectRegistry.CRIMSON_SHELF.get());
        blocks.add(ObjectRegistry.WARPED_SHELF.get());
        blocks.add(ObjectRegistry.BAMBOO_SHELF.get());
        blocks.add(ObjectRegistry.CHERRY_SHELF.get());
        blocks.add(ObjectRegistry.JEWELRY_BOX.get());
        blocks.add(ObjectRegistry.TABLE_SET.get());
        return blocks;
    }
}
