package dev.jmiahman.hearthwind.primitive;

import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class HearthwindPrimitiveBlocks {
    private HearthwindPrimitiveBlocks() {}

    private static ResourceKey<Block> blockKey(String ns, String path) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ns, path));
    }

    private static ResourceKey<Item> itemKey(String ns, String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ns, path));
    }

    private static ResourceKey<BlockEntityType<?>> entityKey(String ns, String path) {
        return ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(ns, path));
    }

    public static final Block STEEL_BLOCK = Registry.register(BuiltInRegistries.BLOCK, blockKey("earlystage", "steel_block"),
            new Block(BlockBehaviour.Properties.of().strength(5.0f, 6.0f)
                    .requiresCorrectToolForDrops().setId(blockKey("earlystage", "steel_block"))));

    public static final Block SIEVE = Registry.register(BuiltInRegistries.BLOCK, blockKey("earlystage", "sieve"),
            new SieveBlock(BlockBehaviour.Properties.of()
                    .mapColor(net.minecraft.world.level.material.MapColor.WOOD)
                    .strength(0.5f, 0.3f)
                    .sound(SoundType.WOOD)
                    .setId(blockKey("earlystage", "sieve"))));

    public static final Block REDSTONE_SIEVE = Registry.register(BuiltInRegistries.BLOCK, blockKey("earlystage", "redstone_sieve"),
            new RedstoneSieveBlock(BlockBehaviour.Properties.of()
                    .mapColor(net.minecraft.world.level.material.MapColor.WOOD)
                    .strength(0.5f, 0.3f)
                    .sound(SoundType.WOOD)
                    .setId(blockKey("earlystage", "redstone_sieve"))));

    public static final Block CRAFTING_ROCK = Registry.register(BuiltInRegistries.BLOCK, blockKey("earlystage", "crafting_rock"),
            new CraftingRockBlock(BlockBehaviour.Properties.of()
                    .mapColor(net.minecraft.world.level.material.MapColor.STONE)
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.STONE)
                    .setId(blockKey("earlystage", "crafting_rock"))));

    public static final Block ROCK = Registry.register(BuiltInRegistries.BLOCK,
            blockKey("earlystage", "rock"),
            new HearthwindRockBlock(BlockBehaviour.Properties.of()
                    .mapColor(net.minecraft.world.level.material.MapColor.STONE)
                    .strength(0.1f, 0.3f)
                    .sound(net.minecraft.world.level.block.SoundType.STONE)
                    .setId(blockKey("earlystage", "rock"))));

    public static final Block FLINT = Registry.register(BuiltInRegistries.BLOCK,
            blockKey("earlystage", "flint"),
            new HearthwindFlintBlock(BlockBehaviour.Properties.of()
                    .mapColor(net.minecraft.world.level.material.MapColor.STONE)
                    .strength(0.1f, 0.3f)
                    .sound(net.minecraft.world.level.block.SoundType.STONE)
                    .setId(blockKey("earlystage", "flint"))));

    public static final BlockEntityType<SieveBlockEntity> SIEVE_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, entityKey("earlystage", "sieve_entity"),
            new BlockEntityType<>(SieveBlockEntity::new, Set.of(SIEVE, REDSTONE_SIEVE)));

    public static final BlockEntityType<CraftingRockBlockEntity> CRAFTING_ROCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, entityKey("earlystage", "crafting_rock_entity"),
            new BlockEntityType<>(CraftingRockBlockEntity::new, Set.of(CRAFTING_ROCK)));

    static {
        Registry.register(BuiltInRegistries.ITEM, itemKey("earlystage", "rock"),
                new BlockItem(ROCK, new Item.Properties().setId(itemKey("earlystage", "rock"))));
        Registry.register(BuiltInRegistries.ITEM, itemKey("earlystage", "flint"),
                new BlockItem(FLINT, new Item.Properties().setId(itemKey("earlystage", "flint"))));
        Registry.register(BuiltInRegistries.ITEM, itemKey("earlystage", "steel_block"),
                new BlockItem(STEEL_BLOCK, new Item.Properties().setId(itemKey("earlystage", "steel_block"))));
        Registry.register(BuiltInRegistries.ITEM, itemKey("earlystage", "sieve"),
                new BlockItem(SIEVE, new Item.Properties().setId(itemKey("earlystage", "sieve"))));
        Registry.register(BuiltInRegistries.ITEM, itemKey("earlystage", "redstone_sieve"),
                new BlockItem(REDSTONE_SIEVE, new Item.Properties().setId(itemKey("earlystage", "redstone_sieve"))));
        Registry.register(BuiltInRegistries.ITEM, itemKey("earlystage", "crafting_rock"),
                new CraftingRockBlock.CraftingRockItem(CRAFTING_ROCK,
                        new Item.Properties().setId(itemKey("earlystage", "crafting_rock"))));
    }

    public static void init() {
        HearthwindPrimitive.LOGGER.info("hearthwind-primitive: blocks registered (steel_block, sieve, redstone_sieve, crafting_rock, rock, flint)");
    }
}
