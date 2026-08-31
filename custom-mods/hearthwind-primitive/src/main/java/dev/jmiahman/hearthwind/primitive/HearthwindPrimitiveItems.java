package dev.jmiahman.hearthwind.primitive;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ToolMaterial;

/**
 * Items for the primitive-start progression, registered under the ORIGINAL
 * earlystage:/agedaddition: namespaces so the migrated server datapack
 * (conversion/datapacks/hearthwind) resolves its ~170 references unchanged.
 *
 * Progression intent (parity with earlystage + agedaddition):
 *  - stone is not hand-mineable; punch/break loose "rock" items instead
 *  - rocks craft cobblestone (datapack recipe), flint tools gate wood/stone
 *  - ore "pieces" reintroduce small-drop economy for rare ores
 */
public final class HearthwindPrimitiveItems {

    public static final TagKey<Item> FLINT_REPAIR_ITEMS = TagKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath("earlystage", "flint_tool_repair"));

    /** Between wood and stone: fast to get, modest durability. */
    public static final ToolMaterial FLINT = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            131,
            5.0f,
            1.5f,
            5,
            FLINT_REPAIR_ITEMS);

    /** Earlystage steel tier: iron mining level, reference tuning (500 dur, 7.0 speed, +2 dmg, 14 ench). */
    public static final TagKey<Item> STEEL_REPAIR_ITEMS = TagKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath("earlystage", "steel_repair"));
    public static final ToolMaterial STEEL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            500,
            7.0f,
            2.0f,
            14,
            STEEL_REPAIR_ITEMS);

    private static final Map<ResourceKey<Item>, Item> REGISTERED = new LinkedHashMap<>();

    private static ResourceKey<Item> key(String namespace, String path) {
        return ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(namespace, path));
    }

    // earlystage rock/flint material items are the BlockItems of the
    // earlystage:rock / earlystage:flint ground blocks (parity: original
    // earlystage registers them together, see HearthwindPrimitiveBlocks)

    public static final Item FLINT_PICKAXE = new Item(new Item.Properties()
            .pickaxe(FLINT, 1.0f, -2.8f)
            .setId(key("earlystage", "flint_pickaxe")));
    public static final Item FLINT_AXE = new Item(new Item.Properties()
            .axe(FLINT, 5.5f, -3.0f)
            .setId(key("earlystage", "flint_axe")));
    public static final Item FLINT_SHOVEL = new Item(new Item.Properties()
            .shovel(FLINT, 1.5f, -3.0f)
            .setId(key("earlystage", "flint_shovel")));
    public static final Item FLINT_HOE = new Item(new Item.Properties()
            .hoe(FLINT, 0.0f, -1.0f)
            .setId(key("earlystage", "flint_hoe")));
    public static final Item FLINT_SWORD = new Item(new Item.Properties()
            .sword(FLINT, 2.0f, -2.4f)
            .setId(key("earlystage", "flint_sword")));
    public static final Item STONE_SHEARS = new ShearsItem(new Item.Properties()
            .durability(180)
            .setId(key("earlystage", "stone_shears")));

    // agedaddition: small-drop pieces (recipes live in the migrated datapack)
    public static final Item COAL_PIECE = new Item(new Item.Properties()
            .setId(key("agedaddition", "coal_piece")));
    public static final Item COPPER_NUGGET = new Item(new Item.Properties()
            .setId(key("agedaddition", "copper_nugget")));
    public static final Item DIAMOND_PIECE = new Item(new Item.Properties()
            .setId(key("agedaddition", "diamond_piece")));
    public static final Item EMERALD_PIECE = new Item(new Item.Properties()
            .setId(key("agedaddition", "emerald_piece")));
    public static final Item LAPIS_LAZULI_PIECE = new Item(new Item.Properties()
            .setId(key("agedaddition", "lapis_lazuli_piece")));
    public static final Item NETHER_STAR_PIECE = new Item(new Item.Properties()
            .setId(key("agedaddition", "nether_star_piece")));
    public static final Item NETHERITE_SCRAP_PIECE = new Item(new Item.Properties()
            .setId(key("agedaddition", "netherite_scrap_piece")));
    public static final Item QUARTZ_PIECE = new Item(new Item.Properties()
            .setId(key("agedaddition", "quartz_piece")));
    public static final Item RAW_COPPER_NUGGET = new Item(new Item.Properties()
            .setId(key("agedaddition", "raw_copper_nugget")));
    public static final Item RAW_GOLD_NUGGET = new Item(new Item.Properties()
            .setId(key("agedaddition", "raw_gold_nugget")));
    public static final Item RAW_IRON_NUGGET = new Item(new Item.Properties()
            .setId(key("agedaddition", "raw_iron_nugget")));

    // hearthwind clay cups - pottery items (2x2 craftable); water storage
    // moved to the dehydration leather flasks (hearthwind-survival).
    public static final Item CLAY_CUP_UNFIRED = new Item(new Item.Properties()
            .setId(key("hearthwind", "clay_cup_unfired")));
    public static final Item CLAY_CUP = new Item(new Item.Properties()
            .setId(key("hearthwind", "clay_cup")));

    // earlystage steel tier (parity: earlystage steel ingot/nugget/block)
    public static final Item STEEL_NUGGET = new Item(new Item.Properties()
            .setId(key("earlystage", "steel_nugget")));
    public static final Item STEEL_INGOT = new Item(new Item.Properties()
            .setId(key("earlystage", "steel_ingot")));

    // earlystage steel tools/armor (Iron Age gate: mining 7 + smithing 14)
    public static final Item STEEL_PICKAXE = new Item(new Item.Properties()
            .pickaxe(STEEL, 1.0f, -2.8f)
            .setId(key("earlystage", "steel_pickaxe")));
    public static final Item STEEL_AXE = new Item(new Item.Properties()
            .axe(STEEL, 5.5f, -3.0f)
            .setId(key("earlystage", "steel_axe")));
    public static final Item STEEL_SHOVEL = new Item(new Item.Properties()
            .shovel(STEEL, 1.5f, -3.0f)
            .setId(key("earlystage", "steel_shovel")));
    public static final Item STEEL_HOE = new Item(new Item.Properties()
            .hoe(STEEL, 0.0f, -1.0f)
            .setId(key("earlystage", "steel_hoe")));
    public static final Item STEEL_SWORD = new Item(new Item.Properties()
            .sword(STEEL, 2.0f, -2.4f)
            .setId(key("earlystage", "steel_sword")));

    // earlystage steel armor: reference protection {3,7,5,2} (= iron), dur
    // multiplier 20, ench 9, iron equip sound, steel repair tag
    public static final net.minecraft.world.item.equipment.ArmorMaterial STEEL_ARMOR =
            new net.minecraft.world.item.equipment.ArmorMaterial(
                    20,
                    java.util.Map.of(
                            net.minecraft.world.item.equipment.ArmorType.HELMET, 3,
                            net.minecraft.world.item.equipment.ArmorType.CHESTPLATE, 7,
                            net.minecraft.world.item.equipment.ArmorType.LEGGINGS, 5,
                            net.minecraft.world.item.equipment.ArmorType.BOOTS, 2),
                    9,
                    net.minecraft.world.item.equipment.ArmorMaterials.IRON.equipSound(),
                    0.0f,
                    0.0f,
                    STEEL_REPAIR_ITEMS,
                    ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID,
                            Identifier.fromNamespaceAndPath("earlystage", "steel")));
    public static final Item STEEL_HELMET = new Item(new Item.Properties()
            .humanoidArmor(STEEL_ARMOR, net.minecraft.world.item.equipment.ArmorType.HELMET)
            .setId(key("earlystage", "steel_helmet")));
    public static final Item STEEL_CHESTPLATE = new Item(new Item.Properties()
            .humanoidArmor(STEEL_ARMOR, net.minecraft.world.item.equipment.ArmorType.CHESTPLATE)
            .setId(key("earlystage", "steel_chestplate")));
    public static final Item STEEL_LEGGINGS = new Item(new Item.Properties()
            .humanoidArmor(STEEL_ARMOR, net.minecraft.world.item.equipment.ArmorType.LEGGINGS)
            .setId(key("earlystage", "steel_leggings")));
    public static final Item STEEL_BOOTS = new Item(new Item.Properties()
            .humanoidArmor(STEEL_ARMOR, net.minecraft.world.item.equipment.ArmorType.BOOTS)
            .setId(key("earlystage", "steel_boots")));

    // earlystage barks (stripping drops, restore logs, campfire kindling).
    // cookTimes are vanilla furnace fuel burn ticks (parity with reference).
    public static final Item OAK_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "oak_bark")),
            net.minecraft.world.level.block.Blocks.OAK_LOG, net.minecraft.world.level.block.Blocks.OAK_WOOD);
    public static final Item DARK_OAK_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "dark_oak_bark")),
            net.minecraft.world.level.block.Blocks.DARK_OAK_LOG, net.minecraft.world.level.block.Blocks.DARK_OAK_WOOD);
    public static final Item ACACIA_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "acacia_bark")),
            net.minecraft.world.level.block.Blocks.ACACIA_LOG, net.minecraft.world.level.block.Blocks.ACACIA_WOOD);
    public static final Item CHERRY_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "cherry_bark")),
            net.minecraft.world.level.block.Blocks.CHERRY_LOG, net.minecraft.world.level.block.Blocks.CHERRY_WOOD);
    public static final Item BIRCH_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "birch_bark")),
            net.minecraft.world.level.block.Blocks.BIRCH_LOG, net.minecraft.world.level.block.Blocks.BIRCH_WOOD);
    public static final Item JUNGLE_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "jungle_bark")),
            net.minecraft.world.level.block.Blocks.JUNGLE_LOG, net.minecraft.world.level.block.Blocks.JUNGLE_WOOD);
    public static final Item SPRUCE_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "spruce_bark")),
            net.minecraft.world.level.block.Blocks.SPRUCE_LOG, net.minecraft.world.level.block.Blocks.SPRUCE_WOOD);
    public static final Item WARPED_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "warped_bark")),
            net.minecraft.world.level.block.Blocks.WARPED_STEM, net.minecraft.world.level.block.Blocks.WARPED_HYPHAE);
    public static final Item CRIMSON_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "crimson_bark")),
            net.minecraft.world.level.block.Blocks.CRIMSON_STEM, net.minecraft.world.level.block.Blocks.CRIMSON_HYPHAE);
    public static final Item MANGROVE_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "mangrove_bark")),
            net.minecraft.world.level.block.Blocks.MANGROVE_LOG, net.minecraft.world.level.block.Blocks.MANGROVE_WOOD);
    public static final Item BAMBOO_BARK = new BarkItem(new Item.Properties()
            .setId(key("earlystage", "bamboo_bark")),
            net.minecraft.world.level.block.Blocks.BAMBOO_BLOCK, null);

    // earlystage buckets (see HearthwindBuckets for mechanics)
    // earlystage buckets (see HearthwindBuckets for mechanics)
    public static final Item WOODEN_BUCKET = new Buckets.WoodenBucketItem(new Item.Properties()
            .stacksTo(16).setId(key("earlystage", "wooden_bucket")));
    public static final Item WATER_WOODEN_BUCKET = new Buckets.WaterWoodenBucketItem(new Item.Properties()
            .stacksTo(1).setId(key("earlystage", "water_wooden_bucket")));
    public static final Item CLAY_BUCKET = new Buckets.ClayBucketItem(new Item.Properties()
            .stacksTo(16).setId(key("earlystage", "clay_bucket")));
    public static final Item BRICK_BUCKET = new Buckets.BrickBucketItem(net.minecraft.world.level.material.Fluids.EMPTY,
            new Item.Properties().stacksTo(16).setId(key("earlystage", "brick_bucket")));
    public static final Item WATER_BRICK_BUCKET = new Buckets.BrickBucketItem(
            net.minecraft.world.level.material.Fluids.WATER,
            new Item.Properties().stacksTo(1).setId(key("earlystage", "water_brick_bucket")));
    public static final Item LAVA_BRICK_BUCKET = new Buckets.BrickBucketItem(
            net.minecraft.world.level.material.Fluids.LAVA,
            new Item.Properties().stacksTo(1).setId(key("earlystage", "lava_brick_bucket")));
    // earlystage wooden shield: vanilla ShieldItem behavior, wooden durability
    public static final Item WOODEN_SHIELD = new net.minecraft.world.item.ShieldItem(new Item.Properties()
            .durability(69)
            .setId(key("earlystage", "wooden_shield")));

    static {
        register("earlystage", "flint_pickaxe", FLINT_PICKAXE);
        register("earlystage", "flint_axe", FLINT_AXE);
        register("earlystage", "flint_shovel", FLINT_SHOVEL);
        register("earlystage", "flint_hoe", FLINT_HOE);
        register("earlystage", "flint_sword", FLINT_SWORD);
        register("earlystage", "stone_shears", STONE_SHEARS);
        register("agedaddition", "coal_piece", COAL_PIECE);
        register("agedaddition", "copper_nugget", COPPER_NUGGET);
        register("agedaddition", "diamond_piece", DIAMOND_PIECE);
        register("agedaddition", "emerald_piece", EMERALD_PIECE);
        register("agedaddition", "lapis_lazuli_piece", LAPIS_LAZULI_PIECE);
        register("agedaddition", "nether_star_piece", NETHER_STAR_PIECE);
        register("agedaddition", "netherite_scrap_piece", NETHERITE_SCRAP_PIECE);
        register("agedaddition", "quartz_piece", QUARTZ_PIECE);
        register("agedaddition", "raw_copper_nugget", RAW_COPPER_NUGGET);
        register("agedaddition", "raw_gold_nugget", RAW_GOLD_NUGGET);
        register("agedaddition", "raw_iron_nugget", RAW_IRON_NUGGET);
        register("earlystage", "steel_nugget", STEEL_NUGGET);
        register("earlystage", "steel_ingot", STEEL_INGOT);
        register("earlystage", "steel_pickaxe", STEEL_PICKAXE);
        register("earlystage", "steel_axe", STEEL_AXE);
        register("earlystage", "steel_shovel", STEEL_SHOVEL);
        register("earlystage", "steel_hoe", STEEL_HOE);
        register("earlystage", "steel_sword", STEEL_SWORD);
        register("earlystage", "steel_helmet", STEEL_HELMET);
        register("earlystage", "steel_chestplate", STEEL_CHESTPLATE);
        register("earlystage", "steel_leggings", STEEL_LEGGINGS);
        register("earlystage", "steel_boots", STEEL_BOOTS);
        register("earlystage", "oak_bark", OAK_BARK);
        register("earlystage", "dark_oak_bark", DARK_OAK_BARK);
        register("earlystage", "acacia_bark", ACACIA_BARK);
        register("earlystage", "cherry_bark", CHERRY_BARK);
        register("earlystage", "birch_bark", BIRCH_BARK);
        register("earlystage", "jungle_bark", JUNGLE_BARK);
        register("earlystage", "spruce_bark", SPRUCE_BARK);
        register("earlystage", "warped_bark", WARPED_BARK);
        register("earlystage", "crimson_bark", CRIMSON_BARK);
        register("earlystage", "mangrove_bark", MANGROVE_BARK);
        register("earlystage", "bamboo_bark", BAMBOO_BARK);
        register("earlystage", "wooden_bucket", WOODEN_BUCKET);
        register("earlystage", "water_wooden_bucket", WATER_WOODEN_BUCKET);
        register("earlystage", "clay_bucket", CLAY_BUCKET);
        register("earlystage", "brick_bucket", BRICK_BUCKET);
        register("earlystage", "water_brick_bucket", WATER_BRICK_BUCKET);
        register("earlystage", "lava_brick_bucket", LAVA_BRICK_BUCKET);
        register("earlystage", "wooden_shield", WOODEN_SHIELD);
        register("hearthwind", "clay_cup_unfired", CLAY_CUP_UNFIRED);
        register("hearthwind", "clay_cup", CLAY_CUP);
    }

    private static void register(String namespace, String path, Item item) {
        Registry.register(BuiltInRegistries.ITEM, key(namespace, path), item);
        REGISTERED.put(key(namespace, path), item);
    }

    public static void init() {
        registerBarkFuels();
        HearthwindPrimitive.LOGGER.info("aged-primitive: {} items registered "
                + "(primitive progression + agedaddition pieces)", REGISTERED.size());
    }

    private static void registerBarkFuels() {
        // vanilla FuelValues is built once at reload; the fabric event lets
        // us extend it (cookTimes = burn ticks, earlystage parity)
        net.fabricmc.fabric.api.registry.FuelValueEvents.BUILD.register((builder, context) -> {
            builder.add(OAK_BARK, 150);
            builder.add(DARK_OAK_BARK, 150);
            builder.add(ACACIA_BARK, 150);
            builder.add(CHERRY_BARK, 150);
            builder.add(BIRCH_BARK, 150);
            builder.add(JUNGLE_BARK, 150);
            builder.add(SPRUCE_BARK, 150);
            builder.add(WARPED_BARK, 150);
            builder.add(CRIMSON_BARK, 200);
            builder.add(MANGROVE_BARK, 200);
            builder.add(BAMBOO_BARK, 100);
        });
    }

    private HearthwindPrimitiveItems() {
    }
}
