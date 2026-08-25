package dev.jmiahman.hearthwind.primitive;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class HearthwindPrimitiveBlocks {
    private HearthwindPrimitiveBlocks() {}

    private static ResourceKey<Block> blockKey(String ns, String path) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ns, path));
    }

    private static ResourceKey<Item> itemKey(String ns, String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ns, path));
    }

    public static final Block STEEL_BLOCK = Registry.register(BuiltInRegistries.BLOCK, blockKey("earlystage", "steel_block"),
            new Block(BlockBehaviour.Properties.of().strength(5.0f, 6.0f)
                    .requiresCorrectToolForDrops().setId(blockKey("earlystage", "steel_block"))));

    static {
        Registry.register(BuiltInRegistries.ITEM, itemKey("earlystage", "steel_block"),
                new BlockItem(STEEL_BLOCK, new Item.Properties().setId(itemKey("earlystage", "steel_block"))));
    }

    public static void init() {
        HearthwindPrimitive.LOGGER.info("hearthwind-primitive: blocks registered (steel_block)");
    }
}
