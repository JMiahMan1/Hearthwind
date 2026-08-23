package dev.jmiahman.aged.survival;

import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class DehydrationItems {
    public static final WaterBowlItem WATER_BOWL =
            new WaterBowlItem(new Item.Properties().setId(key("water_bowl")), false);
    public static final WaterBowlItem PURIFIED_WATER_BOWL =
            new WaterBowlItem(new Item.Properties().setId(key("purified_water_bowl")), true);

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("dehydration", path));
    }

    public static void registerAll(Consumer<String> log) {
        Registry.register(BuiltInRegistries.ITEM, key("water_bowl"), WATER_BOWL);
        Registry.register(BuiltInRegistries.ITEM, key("purified_water_bowl"), PURIFIED_WATER_BOWL);
        log.accept("[aged-survival] dehydration items registered");
    }

    private DehydrationItems() {}
}
