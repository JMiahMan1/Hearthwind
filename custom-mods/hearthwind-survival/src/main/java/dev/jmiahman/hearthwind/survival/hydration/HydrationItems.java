package dev.jmiahman.hearthwind.survival.hydration;

import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;

/**
 * Dehydration water bowls, registered under the original {@code dehydration}
 * namespace so migrated datapack files resolve unchanged.
 *
 * <p>A bowl is a reusable vessel: drinking replaces the filled bowl with a
 * plain {@code minecraft:bowl}. The drink itself is the standard 32-tick
 * DRINK consumable; hydration and the dirty-water thirst roll live in
 * {@link WaterBowlItem} / {@code ThirstHelper} (Aged 1.3.6
 * {@code WaterBowlItem.finishUsing}).
 */
public final class HydrationItems {
    /** 1.6s = 32 ticks, DRINK animation, generic drink sound (upstream). */
    public static final Consumable BOWL_DRINK = Consumables.defaultDrink().build();

    public static WaterBowlItem WATER_BOWL;
    public static WaterBowlItem PURIFIED_WATER_BOWL;

    private HydrationItems() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dehydration", path);
    }

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM, id(path));
    }

    public static void registerAll(Consumer<String> log) {
        WATER_BOWL = Registry.register(BuiltInRegistries.ITEM, id("water_bowl"),
                new WaterBowlItem(new Item.Properties().stacksTo(1)
                        .component(DataComponents.CONSUMABLE, BOWL_DRINK)
                        .setId(key("water_bowl")), true));
        PURIFIED_WATER_BOWL = Registry.register(BuiltInRegistries.ITEM, id("purified_water_bowl"),
                new WaterBowlItem(new Item.Properties().stacksTo(1)
                        .component(DataComponents.CONSUMABLE, BOWL_DRINK)
                        .setId(key("purified_water_bowl")), false));
        log.accept("dehydration water bowls registered (water_bowl, purified_water_bowl)");
    }
}
