package dev.jmiahman.hearthwind.skills;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

/**
 * rpgdifficulty parity: hostile mobs get stronger the farther they spawn
 * from world spawn ("the wilds are dangerous"). Applied once on entity
 * load as a transient modifier keyed <code>aged_skills:mob_scaling</code>,
 * so saved data never double-stacks.
 */
public final class MobScaling {
    public static final Identifier MODIFIER_ID =
            Identifier.fromNamespaceAndPath("hearthwind_skills", "mob_scaling");

    private MobScaling() {}

    /** Scaling steps earned at {@code distanceFromSpawn} (pure math; gametested). */
    public static int stepsFor(double distanceFromSpawn) {
        SkillsConfig.MobScaling cfg = SkillsConfig.get().mobScaling;
        if (!cfg.enabled || distanceFromSpawn <= cfg.graceDistance) {
            return 0;
        }
        return Math.min(cfg.maxSteps,
                (int) ((distanceFromSpawn - cfg.graceDistance) / cfg.stepBlocks));
    }

    public static double healthBonus(int steps) {
        return steps * SkillsConfig.get().mobScaling.healthPerStep;
    }

    public static double damageBonus(int steps) {
        return steps * SkillsConfig.get().mobScaling.damagePerStep;
    }

    /** Event hook: buff hostile mobs on load based on their distance from spawn. */
    public static void apply(net.minecraft.world.entity.Entity entity) {
        if (!(entity instanceof Monster monster)
                || !SkillsConfig.get().mobScaling.enabled
                || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        // 26.x world spawn lives in LevelData.RespawnData
        var spawn = level.getRespawnData().pos();
        double distance = Math.sqrt(monster.distanceToSqr(spawn.getX(), spawn.getY(), spawn.getZ()));
        int steps = stepsFor(distance);
        if (steps <= 0) {
            return;
        }
        applyBonus(monster, Attributes.MAX_HEALTH, healthBonus(steps));
        applyBonus(monster, Attributes.ATTACK_DAMAGE, damageBonus(steps));
    }

    private static void applyBonus(LivingEntity entity,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            double amount) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null || amount == 0 || instance.hasModifier(MODIFIER_ID)) {
            return;
        }
        instance.addTransientModifier(
                new AttributeModifier(MODIFIER_ID, amount, Operation.ADD_VALUE));
    }
}
