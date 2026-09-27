package dev.jmiahman.hearthwind.skills;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * Skill XP, points and purchased levels, mirroring LevelZ's
 * {@code PlayerStatsManager} model that Aged ships:
 *
 * <ul>
 *   <li>Action XP (mining, farming, kills, ...) accumulates per skill and
 *       feeds the progress bar only. It never raises a skill level by
 *       itself.</li>
 *   <li>The total action XP derives the overall level
 *       ({@code totalLevelExperience}/{@code overallLevel}); each overall
 *       level gained banks one <b>skill point</b>.</li>
 *   <li>Skill levels are bought with those points ({@link #skillUp}) and
 *       are stored - attribute bonuses (hearts, damage, armor, ...) follow
 *       the purchased level, exactly like LevelZ.</li>
 * </ul>
 *
 * 0.1.x saves stored only the per-skill XP map with derived levels; the
 * first touch migrates them to stored levels so existing upgrades survive.
 */
public final class SkillXp {
    /** Per-skill action XP (progress bar; legacy saves' only data). */
    public static final AttachmentType<Map<String, Double>> XP =
            AttachmentRegistry.<Map<String, Double>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.DOUBLE))
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("levelz", "xp"));

    /** Purchased skill levels (LevelZ {@code skillLevel}). */
    public static final AttachmentType<Map<String, Integer>> LEVELS =
            AttachmentRegistry.<Map<String, Integer>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.INT))
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("levelz", "skill_levels"));

    /** Total action XP across skills (LevelZ {@code totalLevelExperience}). */
    public static final AttachmentType<Double> TOTAL_XP =
            AttachmentRegistry.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("levelz", "total_experience"));

    /** Unspent skill points (LevelZ {@code skillPoints}). */
    public static final AttachmentType<Integer> POINTS =
            AttachmentRegistry.<Integer>builder()
                    .persistent(Codec.INT)
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("levelz", "skill_points"));

    private SkillXp() {}

    /**
     * One-time migration from the 0.1.x model (levels derived from action
     * XP) to stored levels: the player keeps every level they had and the
     * XP total continues from the same value, but no points are granted.
     */
    public static void migrate(Entity entity) {
        if (entity.getAttached(LEVELS) != null) {
            return;
        }
        Map<String, Double> xpMap = entity.getAttached(XP);
        Map<String, Integer> levels = new HashMap<>();
        double total = 0.0;
        for (Skill skill : Skill.values()) {
            double xp = xpMap != null && xpMap.containsKey(skill.id) ? xpMap.get(skill.id) : 0.0;
            total += xp;
            levels.put(skill.id, levelFor(xp));
        }
        entity.setAttached(LEVELS, levels);
        entity.setAttached(TOTAL_XP, total);
        entity.setAttached(POINTS, 0);
    }

    public static double xp(Entity entity, Skill skill) {
        Map<String, Double> map = entity.getAttached(XP);
        if (map == null || !map.containsKey(skill.id)) {
            return 0.0;
        }
        return map.get(skill.id);
    }

    public static double totalXp(Entity entity) {
        Double total = entity.getAttached(TOTAL_XP);
        return total == null ? 0.0 : total;
    }

    public static void award(Entity entity, Skill skill, double amount) {
        addXp(entity, skill, amount);
    }

    /**
     * Adds action XP: the skill's progress bar and the overall level total.
     * Each overall level gained banks one skill point; skill levels are
     * untouched.
     */
    public static void addXp(Entity entity, Skill skill, double amount) {
        migrate(entity);
        if (amount <= 0 || level(entity, skill) >= maxLevel()) {
            return;
        }
        // Codec-unboundedMap decodes to ImmutableMap on load - must copy to
        // mutable before mutating, otherwise merge throws UOE (see diet fix).
        Map<String, Double> existing = entity.getAttached(XP);
        Map<String, Double> map = existing == null
                ? new HashMap<>()
                : new HashMap<>(existing);
        double newXp = map.getOrDefault(skill.id, 0.0) + amount;
        // Clamp at max level XP so future maxLevel bumps don't cause instant level-ups
        double cap = (double) xpForLevel(maxLevel());
        map.put(skill.id, Math.min(newXp, cap));
        entity.setAttached(XP, map);

        int before = overallLevel(entity);
        entity.setAttached(TOTAL_XP, totalXp(entity) + amount);
        int after = overallLevel(entity);
        if (after > before) {
            entity.setAttached(POINTS, points(entity) + (after - before));
        }
        if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
            if (sp.connection != null) {
                SkillsSync.send(sp);
            }
        }
    }

    /**
     * Spends one skill point to raise a skill by one level (LevelZ's "+"
     * stepper). Returns false when the player has no point or the skill is
     * already at the cap.
     */
    public static boolean skillUp(Entity entity, Skill skill) {
        migrate(entity);
        if (points(entity) < 1 || level(entity, skill) >= maxLevel()) {
            return false;
        }
        setLevel(entity, skill, level(entity, skill) + 1);
        entity.setAttached(POINTS, points(entity) - 1);
        return true;
    }

    public static int points(Entity entity) {
        migrate(entity);
        Integer points = entity.getAttached(POINTS);
        return points == null ? 0 : points;
    }

    public static void grantPoints(Entity entity, int amount) {
        migrate(entity);
        entity.setAttached(POINTS, Math.max(0, points(entity) + amount));
    }

    /** Directly sets a purchased level (commands, tests, migration). */
    public static void setLevel(Entity entity, Skill skill, int targetLevel) {
        migrate(entity);
        int clamped = Math.max(0, Math.min(maxLevel(), targetLevel));
        Map<String, Integer> existing = entity.getAttached(LEVELS);
        Map<String, Integer> map = existing == null
                ? new HashMap<>()
                : new HashMap<>(existing);
        map.put(skill.id, clamped);
        entity.setAttached(LEVELS, map);
        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
            SkillAttributes.onLevelChanged(living, skill);
        }
        if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
            if (sp.connection == null) {
                return;
            }
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

    /** Purchased level of {@code skill} (0 until bought). */
    public static int level(Entity entity, Skill skill) {
        migrate(entity);
        Map<String, Integer> map = entity.getAttached(LEVELS);
        if (map == null || !map.containsKey(skill.id)) {
            return 0;
        }
        return map.get(skill.id);
    }

    /** Overall level derived from the total action XP (auto, LevelZ-style). */
    public static int overallLevel(Entity entity) {
        return levelFor(totalXp(entity));
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
