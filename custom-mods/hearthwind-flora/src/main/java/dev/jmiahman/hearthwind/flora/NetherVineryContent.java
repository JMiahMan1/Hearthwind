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
 * 26.2 Port of Let's Do: NetherVinery content registered under original 'nethervinery:' namespace.
 */
public final class NetherVineryContent {
    public static final String MOD_ID = "nethervinery";
    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static final Map<String, Block> BLOCKS = new HashMap<>();

    public static final FoodProperties GRAPE_FOOD = new FoodProperties.Builder().nutrition(3).saturationModifier(0.4f).build();
    public static final FoodProperties WINE_FOOD = new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).alwaysEdible().build();

    public static void registerAll() {
        // Nether Grapes & Wines
        registerBlock("crimson_grapevine_pot", SoundType.STONE, 1.5f);
        registerBlock("warped_grapevine_pot", SoundType.STONE, 1.5f);

        registerFood("crimson_grape", GRAPE_FOOD);
        registerFood("warped_grape", GRAPE_FOOD);
        registerItem("crimson_grape_seeds");
        registerItem("warped_grape_seeds");

        registerFood("ghast_wine", WINE_FOOD);
        registerFood("nether_wine", WINE_FOOD);
        registerFood("crimson_cider", WINE_FOOD);
        registerFood("warped_wine", WINE_FOOD);
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
