package dev.jmiahman.hearthwind.flora;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 26.2 Port of Let's Do: Vinery content registered under original 'vinery:' namespace.
 */
public final class VineryContent {
    public static final String MOD_ID = "vinery";
    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static final Map<String, Block> BLOCKS = new HashMap<>();

    public static final FoodProperties GRAPE_FOOD = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3f).build();
    public static final FoodProperties WINE_FOOD = new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f).alwaysEdible().build();

    public static void registerAll() {
        // Items (Grapes, seeds, wine, juice)
        registerFood("red_grape", GRAPE_FOOD);
        registerFood("white_grape", GRAPE_FOOD);
        registerFood("taiga_grapes", GRAPE_FOOD);
        registerFood("savanna_grapes", GRAPE_FOOD);
        registerFood("jungle_grapes", GRAPE_FOOD);
        registerItem("red_grape_seeds");
        registerItem("white_grape_seeds");
        registerFood("grape_juice", WINE_FOOD);
        registerFood("apple_juice", WINE_FOOD);
        registerFood("mead", WINE_FOOD);
        registerFood("chenet_wine", WINE_FOOD);
        registerFood("bolvar_wine", WINE_FOOD);
        registerFood("cherry_wine", WINE_FOOD);
        registerFood("kelp_cider", WINE_FOOD);
        registerFood("solaris_wine", WINE_FOOD);
        registerFood("clark_wine", WINE_FOOD);
        registerFood("apple_wine", WINE_FOOD);
        registerFood("villagers_fright", WINE_FOOD);

        // Blocks
        registerBlock("grapevine_pot", SoundType.STONE, 1.0f);
        registerBlock("fermentation_barrel", SoundType.WOOD, 2.0f);
        registerBlock("apple_press", SoundType.WOOD, 2.0f);
        registerLeaves("dark_cherry_leaves");
        registerLeaves("apple_leaves");
        registerLog("dark_cherry_log");
        registerLog("apple_log");
        registerLog("dark_cherry_wood");
        registerLog("apple_wood");
        registerBlock("dark_cherry_planks", SoundType.WOOD, 2.0f);
        registerBlock("wine_box", SoundType.WOOD, 2.0f);
        registerPlant("apple_tree_sapling");
        registerPlant("dark_cherry_sapling");
    }

    private static void registerPlant(String name) {
        ResourceKey<Block> bKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = new FloraPlantBlock(BlockBehaviour.Properties.of()
                .setId(bKey)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ));
        Registry.register(BuiltInRegistries.BLOCK, bKey, block);
        BLOCKS.put(name, block);

        ResourceKey<Item> iKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        BlockItem item = new BlockItem(block, new Item.Properties().setId(iKey));
        Registry.register(BuiltInRegistries.ITEM, iKey, item);
        ITEMS.put(name, item);
    }

    private static Item registerItem(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(new Item.Properties().setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(name, item);
        return item;
    }

    private static Item registerFood(String name, FoodProperties food) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(new Item.Properties().setId(key).component(DataComponents.FOOD, food));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(name, item);
        return item;
    }

    private static Block registerBlock(String name, SoundType sound, float hardness) {
        ResourceKey<Block> bKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        ResourceKey<Item> iKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = new Block(BlockBehaviour.Properties.of().sound(sound).strength(hardness).setId(bKey));
        Registry.register(BuiltInRegistries.BLOCK, bKey, block);
        Item item = new BlockItem(block, new Item.Properties().setId(iKey).useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, iKey, item);
        BLOCKS.put(name, block);
        ITEMS.put(name, item);
        return block;
    }

    private static Block registerLog(String name) {
        ResourceKey<Block> bKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        ResourceKey<Item> iKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = new RotatedPillarBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(2.0f).setId(bKey));
        Registry.register(BuiltInRegistries.BLOCK, bKey, block);
        Item item = new BlockItem(block, new Item.Properties().setId(iKey).useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, iKey, item);
        BLOCKS.put(name, block);
        ITEMS.put(name, item);
        return block;
    }

    private static Block registerLeaves(String name) {
        ResourceKey<Block> bKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        ResourceKey<Item> iKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = new FloraLeavesBlock(BlockBehaviour.Properties.of().sound(SoundType.GRASS).strength(0.2f).noOcclusion().setId(bKey));
        Registry.register(BuiltInRegistries.BLOCK, bKey, block);
        Item item = new BlockItem(block, new Item.Properties().setId(iKey).useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, iKey, item);
        BLOCKS.put(name, block);
        ITEMS.put(name, item);
        return block;
    }
}
