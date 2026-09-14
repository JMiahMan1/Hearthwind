package net.dungeonz.init;

import java.util.List;
import java.util.function.UnaryOperator;

import net.dungeonz.item.*;
import net.dungeonz.item.component.DungeonCompassComponent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.class_1761;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2378;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.class_9331;

public class ItemInit {

    // Item Group
    public static final class_5321<class_1761> DUNGEONZ_ITEM_GROUP = class_5321.method_29179(class_7924.field_44688, class_2960.method_60655("dungeonz", "item_group"));

    // Component
    public static final class_9331<DungeonCompassComponent> DUNGEON_COMPASS_DATA = registerComponent("fill_level",
            builder -> builder.method_57881(DungeonCompassComponent.CODEC).method_57882(DungeonCompassComponent.PACKET_CODEC));

    public static final List<class_1799> REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS = List.of(new class_1799(class_1802.field_27063, 3));

    public static final class_1792 DUNGEON_COMPASS = new DungeonCompassItem(new class_1792.class_1793().method_7889(1));

    private static <T> class_9331<T> registerComponent(String id, UnaryOperator<class_9331.class_9332<T>> builderOperator) {
        return class_2378.method_10226(class_7923.field_49658, id, builderOperator.apply(class_9331.method_57873()).method_57880());
    }

    public static void init() {
        class_2378.method_39197(class_7923.field_44687, DUNGEONZ_ITEM_GROUP,
                FabricItemGroup.builder().method_47320(() -> new class_1799(DUNGEON_COMPASS)).method_47321(class_2561.method_43471("item.dungeonz.item_group")).method_47324());
        class_2378.method_10230(class_7923.field_41178, class_2960.method_60655("dungeonz", "dungeon_compass"), DUNGEON_COMPASS);
        ItemGroupEvents.modifyEntriesEvent(DUNGEONZ_ITEM_GROUP).register(entries -> entries.method_45421(DUNGEON_COMPASS));
    }

}
