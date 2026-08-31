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
 * 26.2 Port of Let's Do: HerbalBrews content registered under original 'herbalbrews:' namespace.
 */
public final class HerbalBrewsContent {
    public static final String MOD_ID = "herbalbrews";
    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static final Map<String, Block> BLOCKS = new HashMap<>();

    public static final FoodProperties TEA_FOOD = new FoodProperties.Builder().nutrition(2).saturationModifier(0.4f).alwaysEdible().build();

    public static void registerAll() {
        // Herbs & Cutout Plants (with ground checks and no water placement)
        registerPlant("lavender");
        registerPlant("wild_coffee");
        registerPlant("wild_yerba_mate");
        registerPlant("wild_rooibos");
        registerPlant("hibiscus");

        // Leaves & Prepared Teas
        registerItem("green_tea_leaf");
        registerItem("black_tea_leaf");
        registerItem("oolong_tea_leaf");
        registerItem("rooibos_leaf");
        registerItem("yerba_mate_leaf");
        registerItem("coffee_beans");

        registerFood("green_tea", TEA_FOOD);
        registerFood("black_tea", TEA_FOOD);
        registerFood("oolong_tea", TEA_FOOD);
        registerFood("rooibos_tea", TEA_FOOD);
        registerFood("yerba_mate_tea", TEA_FOOD);
        registerFood("coffee", TEA_FOOD);
        registerFood("milk_coffee", TEA_FOOD);
        registerFood("lavender_tea", TEA_FOOD);
        registerFood("hibiscus_tea", TEA_FOOD);

        // Workstations & Blocks
        registerBlock("tea_kettle", SoundType.METAL, 2.0f);
        registerBlock("copper_tea_kettle", SoundType.COPPER, 2.0f);
        registerBlock("cauldron", SoundType.METAL, 2.0f);
        registerBlock("stove", SoundType.STONE, 3.0f);
        registerItem("flask");
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

    private static void registerBlock(String name, SoundType sound, float hardness) {
        ResourceKey<Block> bKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = new Block(BlockBehaviour.Properties.of().setId(bKey).sound(sound).strength(hardness));
        Registry.register(BuiltInRegistries.BLOCK, bKey, block);
        BLOCKS.put(name, block);

        ResourceKey<Item> iKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        BlockItem item = new BlockItem(block, new Item.Properties().setId(iKey));
        Registry.register(BuiltInRegistries.ITEM, iKey, item);
        ITEMS.put(name, item);
    }
}
