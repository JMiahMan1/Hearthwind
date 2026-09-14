package net.dungeonz.init;

import net.dungeonz.dungeon.DungeonChunkGenerator;
import net.minecraft.class_1937;
import net.minecraft.class_2378;
import net.minecraft.class_2874;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_7923;
import net.minecraft.class_7924;

public class DimensionInit {

    public static final class_5321<class_1937> DUNGEON_WORLD = class_5321.method_29179(class_7924.field_41223, class_2960.method_60655("dungeonz", "dungeon"));
    public static final class_5321<class_2874> DUNGEON_DIMENSION_TYPE_KEY = class_5321.method_29179(class_7924.field_41241, class_2960.method_60655("dungeonz", "dungeon"));

    public static void init() {
        class_2378.method_10230(class_7923.field_41157, class_2960.method_60655("dungeonz", "dungeon"), DungeonChunkGenerator.CODEC);
    }

}