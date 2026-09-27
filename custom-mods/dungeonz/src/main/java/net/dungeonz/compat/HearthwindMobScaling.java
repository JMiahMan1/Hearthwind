package net.dungeonz.compat;

import net.minecraft.world.entity.Mob;

public final class HearthwindMobScaling {

    private HearthwindMobScaling() {}

    public static void setMobHealthMultiplier(Mob mob, float factor) {
        if (!Float.isFinite(factor) || factor <= 0.0f) {
            throw new IllegalArgumentException("Dungeon health multiplier must be positive and finite");
        }
    }

    /**
     * Toggles the hearthwind-skills distance/height scaling. DungeonZ's own
     * scaling tests must see an untouched base health: the game-test world
     * sits far below the config's starting height, so the skills mod would
     * otherwise buff every spawned mob before the dungeon does.
     */
    public static void setScalingEnabled(boolean enabled) {
        try {
            Class<?> configClass = Class.forName("dev.jmiahman.hearthwind.skills.SkillsConfig");
            Object config = configClass.getMethod("get").invoke(null);
            Object mobScaling = configClass.getField("mobScaling").get(config);
            mobScaling.getClass().getField("enabled").setBoolean(mobScaling, enabled);
        } catch (ReflectiveOperationException ignored) {
            // skills mod absent: nothing to toggle
        }
    }
}
