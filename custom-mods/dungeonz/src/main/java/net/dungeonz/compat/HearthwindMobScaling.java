package net.dungeonz.compat;

import net.minecraft.world.entity.Mob;

public final class HearthwindMobScaling {

    private HearthwindMobScaling() {}

    public static void setMobHealthMultiplier(Mob mob, float factor) {
        if (!Float.isFinite(factor) || factor <= 0.0f) {
            throw new IllegalArgumentException("Dungeon health multiplier must be positive and finite");
        }
    }
}
