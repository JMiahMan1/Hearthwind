package net.dungeonz.init;

import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_7923;

public class SoundInit {

    public static class_3414 DUNGEON_COMPLETION_EVENT = register("dungeonz:dungeon_completion");
    public static class_3414 DUNGEON_GATE_UNLOCK_EVENT = register("dungeonz:dungeon_gate_unlock");
    public static class_3414 DUNGEON_COUNTDOWN_EVENT = register("dungeonz:dungeon_countdown");

    private static class_3414 register(String id) {
        return class_2378.method_10226(class_7923.field_41172, id, class_3414.method_47908(class_2960.method_60654(id)));
    }

    public static void init() {
    }

}
