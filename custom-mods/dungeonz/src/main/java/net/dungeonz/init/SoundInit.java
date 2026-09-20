package net.dungeonz.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.BuiltInRegistries;

public class SoundInit {

    public static SoundEvent DUNGEON_COMPLETION_EVENT = register("dungeonz:dungeon_completion");
    public static SoundEvent DUNGEON_GATE_UNLOCK_EVENT = register("dungeonz:dungeon_gate_unlock");
    public static SoundEvent DUNGEON_COUNTDOWN_EVENT = register("dungeonz:dungeon_countdown");

    private static SoundEvent register(String id) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(Identifier.parse(id)));
    }

    public static void init() {
    }

}
