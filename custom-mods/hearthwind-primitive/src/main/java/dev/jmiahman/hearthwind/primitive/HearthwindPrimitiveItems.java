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
 * (conversion/datapacks/aged-server) resolves its ~170 references unchanged.
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

    private static final Map<ResourceKey<Item>, Item> REGISTERED = new LinkedHashMap<>();

    private static ResourceKey<Item> key(String namespace, String path) {
        return ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(namespace, path));
    }

    public static final Item ROCK = new Item(new Item.Properties()
            .setId(key("earlystage", "rock")));
    public static final Item FLINT_ITEM = new Item(new Item.Properties()
            .setId(key("earlystage", "flint")));

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

    static {
        register("earlystage", "rock", ROCK);
        register("earlystage", "flint", FLINT_ITEM);
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
    }

    private static void register(String namespace, String path, Item item) {
        Registry.register(BuiltInRegistries.ITEM, key(namespace, path), item);
        REGISTERED.put(key(namespace, path), item);
    }

    public static void init() {
        HearthwindPrimitive.LOGGER.info("aged-primitive: {} items registered "
                + "(primitive progression + agedaddition pieces)", REGISTERED.size());
    }

    private HearthwindPrimitiveItems() {
    }
}
