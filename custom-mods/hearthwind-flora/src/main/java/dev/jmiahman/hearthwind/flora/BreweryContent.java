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
 * 26.2 Port of Let's Do: Brewery content registered under original 'brewery:' namespace.
 */
public final class BreweryContent {
    public static final String MOD_ID = "brewery";
    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static final Map<String, Block> BLOCKS = new HashMap<>();

    public static final FoodProperties DRINK_FOOD = new FoodProperties.Builder().nutrition(3).saturationModifier(0.5f).alwaysEdible().build();

    public static void registerAll() {
        // Hops & Brewing
        registerBlock("wild_hops", SoundType.GRASS, 0.0f);
        registerItem("hops");
        registerItem("hops_seeds");
        registerItem("dried_hops");

        registerFood("beer", DRINK_FOOD);
        registerFood("dark_beer", DRINK_FOOD);
        registerFood("whiskey", DRINK_FOOD);
        registerFood("vodka", DRINK_FOOD);
        registerFood("ginger_beer", DRINK_FOOD);

        // Blocks
        registerBlock("brew_kettle", SoundType.METAL, 2.5f);
        registerCustomBlock("beer_barrel", new dev.jmiahman.hearthwind.flora.block.BrewstationBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "beer_barrel")))
                .sound(SoundType.WOOD).strength(2.0f)));
        registerBlock("copper_brew_kettle", SoundType.METAL, 2.5f);
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

    public static Block registerCustomBlock(String name, Block block) {
        ResourceKey<Block> bKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        ResourceKey<Item> iKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Registry.register(BuiltInRegistries.BLOCK, bKey, block);
        Item item = new BlockItem(block, new Item.Properties().setId(iKey).useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, iKey, item);
        BLOCKS.put(name, block);
        ITEMS.put(name, item);
        return block;
    }
}
