package dev.jmiahman.hearthwind.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;

/**
 * Client-side skill level-up toast state.
 * Updated via hearthwind:skillup payload from server.
 * Toast displays for 3 seconds (60 ticks).
 */
public final class ClientSkillData {
    private static String skillId = "";
    private static int level = 0;
    private static long expireTick = 0;
    /** Skill id -> highest level seen via skill-up payloads (skills tab). */
    private static final Map<String, Integer> KNOWN_LEVELS = new ConcurrentHashMap<>();

    private ClientSkillData() {}

    public static void onSkillUp(String skill, int lvl) {
        skillId = skill;
        level = lvl;
        KNOWN_LEVELS.merge(skill, lvl, Math::max);
        Minecraft mc = Minecraft.getInstance();
        expireTick = mc.level != null ? mc.level.getGameTime() + 60 : 0;
    }

    /** Full-sync entry point: replaces the entire known-levels map. */
    public static void replaceAll(Map<String, Integer> levels) {
        KNOWN_LEVELS.clear();
        KNOWN_LEVELS.putAll(levels);
    }

    public static String skillId() { return skillId; }
    public static Map<String, Integer> knownLevels() { return KNOWN_LEVELS; }
    public static int level() { return level; }
    public static boolean isActive() {
        Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        return !skillId.isEmpty() && mc.level != null && mc.level.getGameTime() < expireTick;
    }

    public static float alpha() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 0f;
        long remaining = expireTick - mc.level.getGameTime();
        if (remaining <= 0) return 0f;
        if (remaining < 20) return remaining / 20f;
        return 1f;
    }
}
