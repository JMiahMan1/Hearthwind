package dev.jmiahman.hearthwind.skills;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * rpgdifficulty parity: mobs get stronger the farther they spawn from world
 * spawn ("the wilds are dangerous"). Applied once on entity load as a
 * transient modifier keyed <code>hearthwind_skills:mob_scaling</code>, so
 * saved data never double-stacks, and clients still learn the new stats
 * (vanilla syncs any modified attribute, transient ones included).
 *
 * <p>The structure follows rpgdifficulty 1.3.15's {@code MobStrengthener}:
 * one factor for health, damage and protection, each capped on its own, plus
 * a boss path that drops the height term, keeps working in other dimensions
 * and gains a step per nearby player. Two upstream details are deliberately
 * not reproduced, and the docs record both:
 * <ul>
 *   <li>the {@code c:bosses} entity tag rpgdifficulty reads ships in the
 *       wrong folder upstream (and from our adventurez port too), so in
 *       Aged it resolves empty - we honour the tag anyway, and the ender
 *       dragon gets the dedicated treatment upstream gives it;</li>
 *   <li>rpgdifficulty's "players near the boss" box is built at the world
 *       origin, so it never matches; we use the radius the code intends.</li>
 * </ul>
 */
public final class MobScaling {
    public static final Identifier MODIFIER_ID =
            Identifier.fromNamespaceAndPath("hearthwind_skills", "mob_scaling");

    /**
     * rpgdifficulty's own boss tag, in the {@code c} namespace. Empty in
     * Aged (upstream ships the file as {@code entity_types}, a folder the
     * game ignores), but a datapack that writes the tag correctly still
     * gets the boss treatment.
     */
    public static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("c", "bosses"));

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
     * Distance steps, honouring rpgdifficulty's
     * {@code excludeDistanceInOtherDimension}: a mob 4000 blocks from spawn
     * is vanilla-strength in the Nether, and scaled in the overworld.
     */
    public static int stepsFor(double distanceFromSpawn, boolean overworld) {
        SkillsConfig.MobScaling cfg = SkillsConfig.get().mobScaling;
        if (!overworld && cfg.excludeDistanceInOtherDimension) {
            return 0;
        }
        return stepsFor(distanceFromSpawn);
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

    /**
     * Height steps, honouring rpgdifficulty's
     * {@code excludeHeightInOtherDimension}.
     */
    public static int heightStepsFor(double y, boolean overworld) {
        SkillsConfig.MobScaling cfg = SkillsConfig.get().mobScaling;
        if (!overworld && cfg.excludeHeightInOtherDimension) {
            return 0;
        }
        return heightStepsFor(y);
    }

    /** rpgdifficulty factor for a mob's distance/height, uncapped. */
    public static double factorFor(int distanceSteps, int heightSteps) {
        SkillsConfig.MobScaling cfg = SkillsConfig.get().mobScaling;
        return 1.0 + distanceSteps * cfg.distanceFactor + heightSteps * cfg.heightFactor;
    }

    /** rpgdifficulty caps: health 4.0x, damage 3.0x, protection 2.0x by default. */
    public static double cappedFactor(double factor, double maxFactor) {
        return Math.min(factor, maxFactor);
    }

    /**
     * Boss health factor: the distance term with the boss factor instead of
     * the normal one, multiplied by one step per nearby player
     * ({@code dynamicBossModificator}), capped at {@code bossMaxFactor}.
     */
    public static double bossFactorFor(int distanceSteps, int nearbyPlayers) {
        SkillsConfig.MobScaling cfg = SkillsConfig.get().mobScaling;
        double factor = 1.0 + distanceSteps * cfg.bossDistanceFactor;
        if (cfg.dynamicBossModification) {
            factor *= 1.0 + cfg.dynamicBossModificator * Math.max(0, nearbyPlayers);
        }
        return cappedFactor(factor, cfg.bossMaxFactorHealth);
    }

    /** Entity types rpgdifficulty never touches ({@code excludedEntity}). */
    public static boolean isExcluded(EntityType<?> type) {
        return SkillsConfig.get().mobScaling.excludedEntities.contains(
                BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
    }

    /** True for {@code c:bosses} members, the tag rpgdifficulty reads. */
    public static boolean isBoss(EntityType<?> type) {
        return type.builtInRegistryHolder().is(BOSSES);
    }

    /**
     * Event hook: buff mobs on load based on their distance from spawn.
     * Aged scales every mob except the excluded ids and baby animals -
     * livestock included - so this does too.
     */
    public static void apply(Entity entity) {
        if (!(entity instanceof Mob mob)
                || !SkillsConfig.get().mobScaling.enabled
                || !(entity.level() instanceof ServerLevel level)
                || isExcluded(mob.getType())) {
            return;
        }
        if (mob instanceof AgeableMob ageable && ageable.isBaby()) {
            return;
        }
        SkillsConfig.MobScaling cfg = SkillsConfig.get().mobScaling;

        // The ender dragon sits in Aged's excluded list, but rpgdifficulty's
        // dragon path never reads that list, so the dragon still scales.
        if (mob.getType() == EntityTypes.ENDER_DRAGON || isBoss(mob.getType())) {
            if (cfg.affectBosses) {
                applyBoss(mob, level, cfg);
            }
            return;
        }

        // 26.x world spawn lives in LevelData.RespawnData
        var spawn = level.getRespawnData().pos();
        double distance = Math.sqrt(mob.distanceToSqr(spawn.getX(), spawn.getY(), spawn.getZ()));
        boolean overworld = overworld(level);
        int distanceSteps = stepsFor(distance, overworld);
        int heightSteps = heightStepsFor(mob.getY(), overworld);
        if (distanceSteps <= 0 && heightSteps <= 0) {
            return;
        }
        double factor = factorFor(distanceSteps, heightSteps);
        applyFactor(mob, Attributes.MAX_HEALTH, factor, cfg.maxFactorHealth);
        applyFactor(mob, Attributes.ATTACK_DAMAGE, factor, cfg.maxFactorDamage);
        applyFactor(mob, Attributes.ARMOR, factor, cfg.maxFactorProtection);
    }

    /**
     * The boss path: no height term, no dimension exclusion, health capped
     * at {@code bossMaxFactor}, one extra step per nearby player.
     */
    private static void applyBoss(Mob mob, ServerLevel level, SkillsConfig.MobScaling cfg) {
        var spawn = level.getRespawnData().pos();
        double distance = Math.sqrt(mob.distanceToSqr(spawn.getX(), spawn.getY(), spawn.getZ()));
        double factor = bossFactorFor(stepsFor(distance), playersNear(mob, level, cfg.dynamicBossRadius));
        applyFactor(mob, Attributes.MAX_HEALTH, factor, cfg.bossMaxFactorHealth);
        if (mob.getType() != EntityTypes.ENDER_DRAGON) {
            applyFactor(mob, Attributes.ATTACK_DAMAGE, factor, cfg.maxFactorDamage);
            applyFactor(mob, Attributes.ARMOR, factor, cfg.maxFactorProtection);
        }
    }

    private static boolean overworld(ServerLevel level) {
        return level.dimension() == Level.OVERWORLD;
    }

    /** Living, non-spectating players within {@code radius} blocks of {@code mob}. */
    private static int playersNear(Mob mob, ServerLevel level, double radius) {
        int count = 0;
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && player.isAlive()
                    && player.distanceToSqr(mob) <= radius * radius) {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds {@code base * (min(factor, maxFactor) - 1)} as an additive
     * modifier, so the stat lands on rpgdifficulty's capped multiplier.
     */
    private static void applyFactor(LivingEntity entity, Holder<Attribute> attribute,
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
