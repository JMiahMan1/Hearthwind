package dev.jmiahman.hearthwind.world.endrem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * 26.2 Modern port of End Remastered (Aged 3.1.2 parity):
 * Registers the 16 ancient eyes of ender under the authentic 'endrem:' namespace.
 */
public final class EndRemasteredItems {
    public static final String NAMESPACE = "endrem";

    public static final Map<String, Item> EYES = new LinkedHashMap<>();

    public static final String[] EYE_IDS = {
            "black_eye",
            "cold_eye",
            "corrupted_eye",
            "cryptic_eye",
            "cursed_eye",
            "evil_eye",
            "exotic_eye",
            "guardian_eye",
            "lost_eye",
            "magical_eye",
            "nether_eye",
            "old_eye",
            "rogue_eye",
            "undead_eye",
            "witch_eye",
            "wither_eye"
    };

    /** Crafting parts shipped by the mod (undead soul, witch pupil). */
    public static final String[] PART_IDS = {"undead_soul", "witch_pupil"};

    private EndRemasteredItems() {}

    public static void registerAll(Consumer<String> logger) {
        for (String id : EYE_IDS) {
            ResourceKey<Item> key = ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(NAMESPACE, id));

            Item.Properties props = new Item.Properties()
                    .setId(key)
                    .stacksTo(16)
                    .rarity(Rarity.RARE);

            Item item = new AncientEyeItem(props);
            Registry.register(BuiltInRegistries.ITEM, key, item);
            EYES.put(id, item);
        }

        for (String id : PART_IDS) {
            ResourceKey<Item> key = ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(NAMESPACE, id));
            Item item = new Item(new Item.Properties()
                    .setId(key)
                    .stacksTo(16)
                    .rarity(Rarity.COMMON));
            Registry.register(BuiltInRegistries.ITEM, key, item);
        }

        logger.accept("End Remastered initialized: registered " + EYES.size()
                + " ancient eyes and " + PART_IDS.length + " parts under 'endrem:' namespace");
    }
}
