package dev.jmiahman.hearthwind.world.exploration;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * 26.2 In-house ports of exploration & progression identity items from Aged 3.1.2:
 * 1. Antique Atlas ('antiqueatlas:' namespace) - interactive parchment atlas.
 * 2. Exposure ('exposure:' namespace) - camera and photography mechanics.
 * 3. Inmis Backpacks ('inmis:' namespace) - baby, frayed, plated, gilded, bejeweled, withered, endless backpacks.
 * 4. AdventureZ materials ('adventurez:' namespace) - warthog shell piece.
 */
public final class ExplorationItems {
    public static final Map<String, Item> ITEMS = new HashMap<>();

    private ExplorationItems() {}

    public static void registerAll() {
        // 1. Antique Atlas
        registerAtlasItem("antiqueatlas", "antique_atlas");
        registerAtlasItem("antiqueatlas", "empty_antique_atlas");

        // 2. Exposure Photography
        registerCameraItem("exposure", "camera");
        registerItem("exposure", "photograph");
        registerItem("exposure", "black_and_white_film");
        registerItem("exposure", "color_film");
        registerItem("exposure", "album");

        // 3. Inmis Backpacks
        registerItem("inmis", "baby_backpack");
        registerItem("inmis", "frayed_backpack");
        registerItem("inmis", "plated_backpack");
        registerItem("inmis", "gilded_backpack");
        registerItem("inmis", "bejeweled_backpack");
        registerItem("inmis", "withered_backpack");
        registerItem("inmis", "endless_backpack");

        // 4. AdventureZ
        registerItem("adventurez", "warthog_shell_piece");
    }

    private static Item registerItem(String namespace, String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, name));
        Item item = new Item(new Item.Properties().setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(namespace + ":" + name, item);
        return item;
    }

    private static Item registerAtlasItem(String namespace, String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, name));
        Item item = new Item(new Item.Properties().setId(key).stacksTo(1)) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.SUCCESS_SERVER;
            }
        };
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(namespace + ":" + name, item);
        return item;
    }

    private static Item registerCameraItem(String namespace, String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, name));
        Item item = new Item(new Item.Properties().setId(key).stacksTo(1)) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }
                player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.2f);
                return InteractionResult.SUCCESS_SERVER;
            }
        };
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(namespace + ":" + name, item);
        return item;
    }
}
