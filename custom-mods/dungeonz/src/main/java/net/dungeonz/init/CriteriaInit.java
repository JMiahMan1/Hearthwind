package net.dungeonz.init;

import net.dungeonz.criteria.DungeonBossCriterion;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class CriteriaInit {

    // 26.x: CriteriaTriggers.register is private; custom triggers register
    // directly against TRIGGER_TYPES (same call the vanilla helper makes).
    public static final DungeonBossCriterion DUNGEON_COMPLETION = Registry.register(BuiltInRegistries.TRIGGER_TYPES, "dungeonz:dungeon_completion", new DungeonBossCriterion());

    public static void init() {
    }
}
