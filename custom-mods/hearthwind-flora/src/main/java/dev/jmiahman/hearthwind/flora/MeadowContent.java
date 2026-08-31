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
 * 26.2 Port of Let's Do: Meadow content registered under original 'meadow:' namespace.
 */
public final class MeadowContent {
    public static final String MOD_ID = "meadow";
    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static final Map<String, Block> BLOCKS = new HashMap<>();

    public static final FoodProperties CHEESE_FOOD = new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build();

    public static void registerAll() {
        // Alpine Flora (Cutout flower blocks with ground checks)
        registerPlant("edelweiss");
        registerPlant("alpine_poppy");
        registerPlant("gentian");
        registerPlant("delphinium");
        registerPlant("fire_lily");
        registerPlant("saxifrage");
        registerPlant("eriophorum");

        registerFood("piece_of_cheese", CHEESE_FOOD);
        registerFood("piece_of_sheep_cheese", CHEESE_FOOD);
        registerFood("piece_of_goat_cheese", CHEESE_FOOD);
        registerFood("piece_of_grain_cheese", CHEESE_FOOD);
        registerFood("piece_of_amethyst_cheese", CHEESE_FOOD);
        registerFood("cheesecake", CHEESE_FOOD);

        // Alpine Salt & Ores
        registerItem("alpine_salt");
        registerBlock("alpine_salt_ore", SoundType.STONE, 3.0f);
        registerBlock("alpine_coal_ore", SoundType.STONE, 3.0f);
        registerBlock("alpine_copper_ore", SoundType.STONE, 3.0f);
        registerBlock("alpine_iron_ore", SoundType.STONE, 3.0f);
        registerBlock("alpine_gold_ore", SoundType.STONE, 3.0f);
        registerBlock("alpine_lapis_ore", SoundType.STONE, 3.0f);
        registerBlock("alpine_redstone_ore", SoundType.STONE, 3.0f);
        registerBlock("alpine_diamond_ore", SoundType.STONE, 3.0f);
        registerBlock("alpine_emerald_ore", SoundType.STONE, 3.0f);

        // Blocks
        registerBlock("cheese_form", SoundType.WOOD, 2.0f);
        registerBlock("wooden_cauldron", SoundType.WOOD, 2.0f);
        registerBlock("fondue_pot", SoundType.STONE, 2.5f);
        registerBlock("woodcutter", SoundType.WOOD, 2.0f);
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
