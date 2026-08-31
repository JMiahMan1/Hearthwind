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
            "blacksmith_eye",
            "nether_eye",
            "desert_eye",
            "ocean_eye",
            "old_eye",
            "rogue_eye",
            "cursed_eye",
            "evil_eye",
            "guardian_eye",
            "magical_eye",
            "wither_eye",
            "witch_eye",
            "undead_eye",
            "cryptic_eye",
            "corrupted_eye",
            "lost_eye"
    };

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

        logger.accept("End Remastered initialized: registered " + EYES.size() + " ancient eyes under 'endrem:' namespace");
    }
}
