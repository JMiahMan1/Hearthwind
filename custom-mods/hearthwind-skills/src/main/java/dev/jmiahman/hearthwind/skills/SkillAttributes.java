package dev.jmiahman.hearthwind.skills;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

/**
 * Applies per-level attribute bonuses as transient modifiers keyed by
 * <code>aged_skills:&lt;skill&gt;</code> so they never persist into saved
 * entity data and are recomputed from XP on every change/login.
 */
public final class SkillAttributes {
    private static final Map<String, Identifier> IDS = new ConcurrentHashMap<>();

    private SkillAttributes() {}

    private static Identifier id(Skill skill) {
        return IDS.computeIfAbsent(skill.id,
                s -> Identifier.fromNamespaceAndPath("hearthwind_skills", s));
    }

    /** Bonus amount for the entity's current level of {@code skill}. */
    public static double bonusFor(LivingEntity entity, Skill skill) {
        SkillsConfig.Bonuses b = SkillsConfig.get().bonuses;
        int level = SkillXp.level(entity, skill);
        return switch (skill) {
            case HEALTH -> level * b.healthHpPerLevel;
            case STRENGTH -> level * b.strengthDamagePerLevel;
            case AGILITY -> level * b.agilitySpeedFractionPerLevel;
            case DEFENSE -> level * b.defenseArmorPerLevel;
            case MINING -> level * b.miningSpeedFractionPerLevel;
            case LUCK -> level * b.luckPerLevel;
            default -> 0.0;
        };
    }

    private static Operation operation(Skill skill) {
        // speed and mining efficiency are fractional; the rest are additive
        return skill == Skill.AGILITY || skill == Skill.MINING
                ? Operation.ADD_MULTIPLIED_BASE
                : Operation.ADD_VALUE;
    }

    /**
     * Re-apply the modifier for one skill after its level changed.
     * Server-side only; no-op otherwise.
     */
    public static void onLevelChanged(net.minecraft.world.entity.Entity entity, Skill skill) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        applyAll(player, skill);
    }

    /** Re-applies every numeric-skill modifier (login, respawn). */
    public static void applyAll(ServerPlayer player, Skill... skills) {
        for (Skill skill : skills) {
            Holder<Attribute> attr = skill.attribute;
            if (attr == null) {
                continue;
            }
            AttributeInstance instance = player.getAttribute(attr);
            if (instance == null) {
                continue;
            }
            instance.removeModifier(id(skill));
            double bonus = bonusFor(player, skill);
            if (bonus != 0.0) {
                instance.addTransientModifier(
                        new AttributeModifier(id(skill), bonus, operation(skill)));
            }
        }
    }

    public static void applyAll(ServerPlayer player) {
        applyAll(player, Skill.values());
    }
}
