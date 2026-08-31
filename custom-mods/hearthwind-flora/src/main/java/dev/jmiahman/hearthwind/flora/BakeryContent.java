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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 26.2 Port of Let's Do: Bakery content registered under original 'bakery:' namespace.
 */
public final class BakeryContent {
    public static final String MOD_ID = "bakery";
    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static final Map<String, Block> BLOCKS = new HashMap<>();

    public static final FoodProperties BREAD_FOOD = new FoodProperties.Builder().nutrition(5).saturationModifier(0.6f).build();
    public static final FoodProperties SWEET_FOOD = new FoodProperties.Builder().nutrition(4).saturationModifier(0.4f).build();

    public static void registerAll() {
        // Grains & Pastries
        registerFood("baguette", BREAD_FOOD);
        registerFood("croissant", BREAD_FOOD);
        registerFood("crusty_bread", BREAD_FOOD);
        registerFood("braided_bread", BREAD_FOOD);
        registerFood("toast", BREAD_FOOD);
        registerFood("bundt_cake", SWEET_FOOD);
        registerFood("strawberry_cake", SWEET_FOOD);
        registerFood("apple_pie", SWEET_FOOD);
        registerFood("strawberry", SWEET_FOOD);
        registerItem("oat");
        registerItem("oat_seeds");
        registerItem("strawberry_seeds");

        // Blocks
        registerBlock("brick_oven", SoundType.STONE, 3.5f);
        registerBlock("baker_station", SoundType.WOOD, 2.5f);
        registerBlock("tray", SoundType.WOOD, 2.0f);
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
}
