package dev.jmiahman.hearthwind.survival;

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
    public static final HotWaterBowlItem HOT_WATER_BOWL =
            new HotWaterBowlItem(new Item.Properties().setId(key("hot_water_bowl")), false);
    public static final HotWaterBowlItem HOT_PURIFIED_WATER_BOWL =
            new HotWaterBowlItem(new Item.Properties().setId(key("hot_purified_water_bowl")), true);
    public static final ColdWaterBowlItem COLD_WATER_BOWL =
            new ColdWaterBowlItem(new Item.Properties().setId(key("cold_water_bowl")), false);
    public static final ColdWaterBowlItem COLD_PURIFIED_WATER_BOWL =
            new ColdWaterBowlItem(new Item.Properties().setId(key("cold_purified_water_bowl")), true);

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("dehydration", path));
    }

    public static void registerAll(Consumer<String> log) {
        Registry.register(BuiltInRegistries.ITEM, key("water_bowl"), WATER_BOWL);
        Registry.register(BuiltInRegistries.ITEM, key("purified_water_bowl"), PURIFIED_WATER_BOWL);
        Registry.register(BuiltInRegistries.ITEM, key("hot_water_bowl"), HOT_WATER_BOWL);
        Registry.register(BuiltInRegistries.ITEM, key("hot_purified_water_bowl"), HOT_PURIFIED_WATER_BOWL);
        Registry.register(BuiltInRegistries.ITEM, key("cold_water_bowl"), COLD_WATER_BOWL);
        Registry.register(BuiltInRegistries.ITEM, key("cold_purified_water_bowl"), COLD_PURIFIED_WATER_BOWL);
        log.accept("[aged-survival] dehydration items registered");
    }

    private DehydrationItems() {}
}
