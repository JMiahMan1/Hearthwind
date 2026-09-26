package dev.jmiahman.hearthwind.survival.hydration;

import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

/**
 * Registers the Dehydration hydration blocks under their original ids:
 * {@code campfire_cauldron} (with its block entity),
 * {@code copper_cauldron}, {@code water_copper_cauldron},
 * {@code powder_snow_copper_cauldron} and
 * {@code purified_water_copper_cauldron}.
 */
public final class HydrationBlocks {
    public static CampfireCauldronBlock CAMPFIRE_CAULDRON;
    public static CopperCauldronBlock COPPER_CAULDRON;
    public static CopperLeveledCauldronBlock COPPER_WATER_CAULDRON;
    public static CopperLeveledCauldronBlock COPPER_POWDERED_CAULDRON;
    public static CopperLeveledCauldronBlock COPPER_PURIFIED_WATER_CAULDRON;
    public static BlockEntityType<CampfireCauldronBlockEntity> CAMPFIRE_CAULDRON_ENTITY;

    private HydrationBlocks() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dehydration", path);
    }

    private static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK, id(path));
    }

    private static ResourceKey<Item> itemKey(String path) {
        return ResourceKey.create(Registries.ITEM, id(path));
    }

    private static ResourceKey<BlockEntityType<?>> entityKey(String path) {
        return ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id(path));
    }

    private static void registerBlockItem(Block block, String path) {
        Registry.register(BuiltInRegistries.ITEM, id(path), new BlockItem(block,
                new Item.Properties().useBlockDescriptionPrefix().setId(itemKey(path))));
    }

    public static void registerAll(Consumer<String> log) {
        CopperCauldronBehavior.registerBehavior();

        CAMPFIRE_CAULDRON = Registry.register(BuiltInRegistries.BLOCK, id("campfire_cauldron"),
                new CampfireCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                        .pushReaction(PushReaction.DESTROY)
                        .setId(blockKey("campfire_cauldron"))));
        COPPER_CAULDRON = Registry.register(BuiltInRegistries.BLOCK, id("copper_cauldron"),
                new CopperCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                        .setId(blockKey("copper_cauldron"))));
        COPPER_WATER_CAULDRON = Registry.register(BuiltInRegistries.BLOCK, id("water_copper_cauldron"),
                new CopperLeveledCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                        .setId(blockKey("water_copper_cauldron")),
                        CopperLeveledCauldronBlock.RAIN_PREDICATE,
                        CopperCauldronBehavior.WATER_COPPER_CAULDRON_BEHAVIOR));
        COPPER_POWDERED_CAULDRON = Registry.register(BuiltInRegistries.BLOCK,
                id("powder_snow_copper_cauldron"),
                new CopperLeveledCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                        .setId(blockKey("powder_snow_copper_cauldron")),
                        CopperLeveledCauldronBlock.SNOW_PREDICATE,
                        CopperCauldronBehavior.POWDER_SNOW_COPPER_CAULDRON_BEHAVIOR));
        COPPER_PURIFIED_WATER_CAULDRON = Registry.register(BuiltInRegistries.BLOCK,
                id("purified_water_copper_cauldron"),
                new CopperLeveledCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                        .setId(blockKey("purified_water_copper_cauldron")),
                        CopperLeveledCauldronBlock.RAIN_PREDICATE,
                        CopperCauldronBehavior.PURIFIED_WATER_COPPER_CAULDRON_BEHAVIOR));

        CAMPFIRE_CAULDRON_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                entityKey("campfire_cauldron_entity"),
                new BlockEntityType<>(CampfireCauldronBlockEntity::new, Set.of(CAMPFIRE_CAULDRON)));

        registerBlockItem(CAMPFIRE_CAULDRON, "campfire_cauldron");
        registerBlockItem(COPPER_CAULDRON, "copper_cauldron");

        log.accept("dehydration hydration blocks registered (campfire_cauldron, copper_cauldron family)");
    }
}
