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
 * 26.2 Port of Let's Do: Farm & Charm content registered under original 'farm_and_charm:' namespace.
 */
public final class FarmAndCharmContent {
    public static final String MOD_ID = "farm_and_charm";
    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static final Map<String, Block> BLOCKS = new HashMap<>();

    public static final FoodProperties VEG_FOOD = new FoodProperties.Builder().nutrition(3).saturationModifier(0.6f).build();
    public static final FoodProperties FRUIT_FOOD = new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f).build();
    public static final FoodProperties MEAL_FOOD = new FoodProperties.Builder().nutrition(7).saturationModifier(0.8f).build();

    public static void registerAll() {
        // Wild Crops (Plants with proper cutout, no collision, and ground checks)
        registerPlant("wild_barley");
        registerPlant("wild_corn");
        registerPlant("wild_strawberries");
        registerPlant("wild_onions");
        registerPlant("wild_garlic");
        registerPlant("wild_tomatoes");
        registerPlant("wild_lettuce");
        registerPlant("wild_oat");
        registerPlant("wild_ribs");

        // Domesticated Foods & Seeds
        registerFood("barley", VEG_FOOD);
        registerFood("corn", VEG_FOOD);
        registerFood("strawberry", FRUIT_FOOD);
        registerFood("strawberries", FRUIT_FOOD);
        registerFood("onion", VEG_FOOD);
        registerFood("garlic", VEG_FOOD);

        registerItem("barley_seeds");
        registerItem("corn_seeds");
        registerItem("strawberry_seeds");
        registerItem("onion_seeds");
        registerItem("garlic_seeds");

        // Ingredients & Cooking Products
        registerItem("flour");
        registerItem("dough");
        registerFood("oatmeal", MEAL_FOOD);
        registerFood("ribs", MEAL_FOOD);
        registerFood("roast_ribs", MEAL_FOOD);
        registerFood("soup", MEAL_FOOD);
        registerFood("corn_soup", MEAL_FOOD);
        registerFood("strawberry_pie", MEAL_FOOD);
        registerFood("strawberry_jam", FRUIT_FOOD);
        registerFood("cornbread", MEAL_FOOD);
        registerItem("fertilizer");

        // Workstations & Farm Blocks
        registerCustomBlock("cooking_pot", new dev.jmiahman.hearthwind.flora.block.CookingPotBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "cooking_pot")))
                .sound(SoundType.METAL)
                .strength(2.0f)));
        registerCustomBlock("stove", new dev.jmiahman.hearthwind.flora.block.StoveBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "stove")))
                .sound(SoundType.STONE)
                .strength(2.5f)));
        registerBlock("silo", SoundType.WOOD, 2.0f);
        registerBlock("water_sprinkler", SoundType.METAL, 2.0f);
        registerBlock("feeding_trough", SoundType.WOOD, 2.0f);
        registerBlock("chicken_coop", SoundType.WOOD, 2.0f);
        registerBlock("mincer", SoundType.METAL, 2.0f);
        registerBlock("crafting_bowl", SoundType.WOOD, 1.0f);
        registerBlock("scarecrow", SoundType.WOOD, 1.0f);
        registerBlock("roaster", SoundType.STONE, 2.5f);
        registerBlock("butter_churn", SoundType.WOOD, 2.0f);
        registerBlock("plow", SoundType.WOOD, 2.0f);
        registerBlock("supply_cart", SoundType.WOOD, 2.0f);
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

    private static void registerItem(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(new Item.Properties().setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(name, item);
    }

    private static void registerFood(String name, FoodProperties foodProps) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(new Item.Properties().setId(key).food(foodProps));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(name, item);
    }

    private static Block registerBlock(String name, SoundType sound, float hardness) {
        ResourceKey<Block> bKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = new Block(BlockBehaviour.Properties.of().setId(bKey).sound(sound).strength(hardness));
        Registry.register(BuiltInRegistries.BLOCK, bKey, block);
        BLOCKS.put(name, block);

        ResourceKey<Item> iKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        BlockItem item = new BlockItem(block, new Item.Properties().setId(iKey));
        Registry.register(BuiltInRegistries.ITEM, iKey, item);
        ITEMS.put(name, item);
        return block;
    }
}
