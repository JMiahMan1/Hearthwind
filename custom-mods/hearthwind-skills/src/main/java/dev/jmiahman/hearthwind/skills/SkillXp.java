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
 * Level curve: reaching level L costs <code>baseXpPerLevel * L</code> XP,
 * so cumulative XP for level L is base * L*(L+1)/2 (triangular numbers).
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
        map.merge(skill.id, amount, Double::sum);
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

    /** Cumulative XP needed to reach {@code level}. */
    public static long xpForLevel(int level) {
        long b = SkillsConfig.get().levels.baseXpPerLevel;
        return b * (long) level * (level + 1) / 2;
    }

    /** Current level derived from total XP; capped at maxLevel. */
    public static int level(Entity entity, Skill skill) {
        return levelFor(xp(entity, skill));
    }

    public static int levelFor(double totalXp) {
        // invert triangular sum: L = floor((sqrt(8*x/b + 1) - 1) / 2)
        double b = SkillsConfig.get().levels.baseXpPerLevel;
        int level = (int) ((Math.sqrt(8.0 * totalXp / b + 1.0) - 1.0) / 2.0);
        return Math.max(0, Math.min(maxLevel(), level));
    }

    public static int maxLevel() {
        return SkillsConfig.get().levels.maxLevel;
    }
}
