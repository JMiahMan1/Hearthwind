package dev.jmiahman.hearthwind.skills;

import java.util.Map;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * XP storage and level math for all skills. XP is persisted per entity in
 * a data attachment under the original levelz namespace so any migrated
 * tuning that references levelz keeps working.
 *
 * Level curve (Aged/LevelZ): the cost to go from level L to L+1 is
 * <code>(int)(xpBaseCost + xpCostMultiplicator * L^xpExponent)</code> -
 * 25, 26, 28, 29, 31 ... with the Aged config.
 */
public final class SkillXp {
    public static final AttachmentType<Map<String, Double>> XP =
            AttachmentRegistry.<Map<String, Double>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.DOUBLE))
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("levelz", "xp"));

    private SkillXp() {}

    public static double xp(Entity entity, Skill skill) {
        Map<String, Double> map = entity.getAttached(XP);
        if (map == null || !map.containsKey(skill.id)) {
            return 0.0;
        }
        return map.get(skill.id);
    }

    public static void award(Entity entity, Skill skill, double amount) {
        addXp(entity, skill, amount);
    }

    public static void addXp(Entity entity, Skill skill, double amount) {
        if (amount <= 0 || level(entity, skill) >= maxLevel()) {
            return;
        }
        // Codec-unboundedMap decodes to ImmutableMap on load - must copy to
        // mutable before mutating, otherwise merge throws UOE (see diet fix).
        Map<String, Double> existing = entity.getAttached(XP);
        Map<String, Double> map = existing == null
                ? new java.util.HashMap<>()
                : new java.util.HashMap<>(existing);
        int before = level(entity, skill);
        double newXp = map.getOrDefault(skill.id, 0.0) + amount;
        // Clamp at max level XP so future maxLevel bumps don't cause instant level-ups
        double cap = (double) xpForLevel(maxLevel());
        map.put(skill.id, Math.min(newXp, cap));
        entity.setAttached(XP, map);
        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
            SkillAttributes.onLevelChanged(living, skill);
        }
        if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
            int after = level(sp, skill);
            if (after > before) {
                dev.jmiahman.hearthwind.survival.SkillUpPayload payload =
                        new dev.jmiahman.hearthwind.survival.SkillUpPayload(skill.id, after);
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                        sp, payload);
                SkillsSync.send(sp);
            }
        }
    }

    public static void setLevel(Entity entity, Skill skill, int targetLevel) {
        int clamped = Math.max(0, Math.min(maxLevel(), targetLevel));
        long neededXp = xpForLevel(clamped);
        Map<String, Double> existing = entity.getAttached(XP);
        Map<String, Double> map = existing == null
                ? new java.util.HashMap<>()
                : new java.util.HashMap<>(existing);
        map.put(skill.id, (double) neededXp);
        entity.setAttached(XP, map);
        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
            SkillAttributes.onLevelChanged(living, skill);
        }
        if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
            dev.jmiahman.hearthwind.survival.SkillUpPayload payload =
                    new dev.jmiahman.hearthwind.survival.SkillUpPayload(skill.id, clamped);
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp, payload);
            SkillsSync.send(sp);
        }
    }

    /** Cost to advance from {@code currentLevel} to the next (LevelZ formula). */
    public static int nextCost(int currentLevel) {
        SkillsConfig.Levels c = SkillsConfig.get().levels;
        return (int) (c.xpBaseCost
                + c.xpCostMultiplicator * Math.pow(currentLevel, c.xpExponent));
    }

    /** Cumulative XP needed to reach {@code level} (exact per-level costs). */
    public static long xpForLevel(int level) {
        long total = 0L;
        for (int l = 0; l < level && l < maxLevel(); l++) {
            total += nextCost(l);
        }
        return total;
    }

    /** Current level derived from total XP; capped at maxLevel. */
    public static int level(Entity entity, Skill skill) {
        return levelFor(xp(entity, skill));
    }

    public static int levelFor(double totalXp) {
        long accumulated = 0L;
        int level = 0;
        while (level < maxLevel()) {
            int cost = nextCost(level);
            if (totalXp < accumulated + cost) {
                break;
            }
            accumulated += cost;
            level++;
        }
        return level;
    }

    public static int maxLevel() {
        return SkillsConfig.get().levels.maxLevel;
    }
}
