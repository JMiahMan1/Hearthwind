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
 * 26.2 Port of Let's Do: Candlelight content registered under original 'candlelight:' namespace.
 */
public final class CandlelightContent {
    public static final String MOD_ID = "candlelight";
    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static final Map<String, Block> BLOCKS = new HashMap<>();

    public static final FoodProperties VEG_FOOD = new FoodProperties.Builder().nutrition(3).saturationModifier(0.6f).build();
    public static final FoodProperties MEAL_FOOD = new FoodProperties.Builder().nutrition(8).saturationModifier(0.8f).build();

    public static void registerAll() {
        // Produce
        registerFood("tomato", VEG_FOOD);
        registerFood("lettuce", VEG_FOOD);
        registerFood("broccoli", VEG_FOOD);
        registerFood("mozzarella", VEG_FOOD);
        registerItem("tomato_seeds");
        registerItem("lettuce_seeds");
        registerItem("broccoli_seeds");

        // Meals
        registerFood("tomato_soup", MEAL_FOOD);
        registerFood("pasta", MEAL_FOOD);
        registerFood("lasagna", MEAL_FOOD);
        registerFood("pizza", MEAL_FOOD);
        registerFood("beef_tartare", MEAL_FOOD);
        registerFood("roastbeef_with_carrots", MEAL_FOOD);

        // Blocks
        registerBlock("cooking_pan", SoundType.METAL, 2.0f);
        registerBlock("cooking_pot", SoundType.METAL, 2.0f);
        registerBlock("side_table", SoundType.WOOD, 2.0f);
        registerBlock("chair", SoundType.WOOD, 2.0f);
        registerBlock("table", SoundType.WOOD, 2.0f);
        registerBlock("counter", SoundType.WOOD, 2.0f);
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
