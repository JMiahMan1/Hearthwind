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

    /**
     * Height steps over the world's spawn height (rpgdifficulty applies its
     * factor both above and below startingHeight).
     */
    public static int heightStepsFor(double y) {
        SkillsConfig.MobScaling cfg = SkillsConfig.get().mobScaling;
        if (!cfg.enabled || cfg.heightDistance <= 0) {
            return 0;
        }
        return (int) (Math.abs(y - cfg.startingHeight) / cfg.heightDistance);
    }

    /** rpgdifficulty factor for a mob's distance/height, uncapped. */
    public static double factorFor(int distanceSteps, int heightSteps) {
        SkillsConfig.MobScaling cfg = SkillsConfig.get().mobScaling;
        return 1.0 + distanceSteps * cfg.distanceFactor + heightSteps * cfg.heightFactor;
    }

    /** rpgdifficulty caps: health 4.0x, damage 3.0x by default. */
    public static double cappedFactor(double factor, double maxFactor) {
        return Math.min(factor, maxFactor);
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
        int distanceSteps = stepsFor(distance);
        int heightSteps = heightStepsFor(monster.getY());
        if (distanceSteps <= 0 && heightSteps <= 0) {
            return;
        }
        double factor = factorFor(distanceSteps, heightSteps);
        SkillsConfig.MobScaling cfg = SkillsConfig.get().mobScaling;
        applyFactor(monster, Attributes.MAX_HEALTH, factor, cfg.maxFactorHealth);
        applyFactor(monster, Attributes.ATTACK_DAMAGE, factor, cfg.maxFactorDamage);
    }

    /**
     * Adds {@code base * (min(factor, maxFactor) - 1)} as an additive
     * modifier, so the stat lands on rpgdifficulty's capped multiplier.
     */
    private static void applyFactor(LivingEntity entity,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            double factor, double maxFactor) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null || instance.hasModifier(MODIFIER_ID)) {
            return;
        }
        double capped = cappedFactor(factor, maxFactor);
        double amount = instance.getBaseValue() * (capped - 1.0);
        if (amount == 0.0) {
            return;
        }
        instance.addTransientModifier(
                new AttributeModifier(MODIFIER_ID, amount, Operation.ADD_VALUE));
    }
}
