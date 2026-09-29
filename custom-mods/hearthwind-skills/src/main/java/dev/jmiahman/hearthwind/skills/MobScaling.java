package dev.jmiahman.hearthwind.skills;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import net.minecraft.world.entity.monster.zombie.Zombie;
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

    /**
     * Marks a zombie that rolled {@code bigZombieChance}, so its hitbox and
     * model render oversized. rpgdifficulty keeps the same flag in a data
     * tracker and scales {@code getDimensions}; a synced attachment is the
     * 26.x equivalent, because the client has to agree on the size for the
     * hitbox to match what is drawn.
     */
    public static final AttachmentType<Boolean> BIG_ZOMBIE =
            AttachmentRegistry.<Boolean>builder()
                    .initializer(() -> Boolean.FALSE)
                    .persistent(Codec.BOOL)
                    .syncWith(StreamCodec.of(ByteBufCodecs.BOOL, ByteBufCodecs.BOOL),
                            (holder, value) -> true)
                    .buildAndRegister(Identifier.fromNamespaceAndPath("hearthwind_skills", "big_zombie"));

    /** Which special variant, if any, a zombie rolled. */
    public enum Special {
        NONE, SPEED, BIG
    }

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

        // rpgdifficulty rolls for jitter and for a special zombie AFTER the
        // distance maths with no early return in between, so a mob spawned on
        // top of world spawn still rolls. Keeping that order is what makes
        // "I fought a big zombie right outside my base" a real Aged memory.
        if (cfg.allowRandomValues && mob.getRandom().nextFloat() <= cfg.randomChance / 100.0F) {
            double factor = cfg.randomFactor / 100.0;
            jitter(mob, Attributes.MAX_HEALTH, factor, mob.getRandom().nextDouble());
            jitter(mob, Attributes.ATTACK_DAMAGE, factor, mob.getRandom().nextDouble());
        }
        if (cfg.allowSpecialZombie && !(mob instanceof AgeableMob) && mob instanceof Zombie) {
            // Speed is rolled first and short-circuits the big roll, so a
            // zombie can never be both.
            Special special = rollSpecialZombie(
                    mob.getRandom().nextFloat(), mob.getRandom().nextFloat(), cfg);
            if (special == Special.SPEED) {
                addFlat(mob, Attributes.MAX_HEALTH, -cfg.speedZombieMalusLifePoints);
                multiply(mob, Attributes.MOVEMENT_SPEED, cfg.speedZombieSpeedFactor);
            } else if (special == Special.BIG) {
                multiply(mob, Attributes.MOVEMENT_SPEED, cfg.bigZombieSlownessFactor);
                addFlat(mob, Attributes.MAX_HEALTH, cfg.bigZombieBonusLifePoints);
                addFlat(mob, Attributes.ATTACK_DAMAGE, cfg.bigZombieBonusDamage);
                mob.setAttached(BIG_ZOMBIE, Boolean.TRUE);
            }
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
     * rpgdifficulty's jitter: a stat is scaled into
     * {@code 1 - factor + roll * factor * 2} for one roll in [0,1), so the
     * result is a value between {@code 1 - factor} and {@code 1 + factor}
     * around the stat it started from. Pure math; gametested.
     */
    public static double jitter(double value, double factor, double roll) {
        return value * (1 - factor + roll * factor * 2);
    }

    /**
     * Which special zombie a pair of rolls picks, and in which order. Pure
     * math; gametested. The speed roll is checked first and the big roll is
     * only consulted when it failed, so the two variants stay exclusive.
     */
    public static Special rollSpecialZombie(float speedRoll, float bigRoll, SkillsConfig.MobScaling cfg) {
        if (speedRoll < cfg.speedZombieChance / 100.0F) {
            return Special.SPEED;
        }
        if (bigRoll < cfg.bigZombieChance / 100.0F) {
            return Special.BIG;
        }
        return Special.NONE;
    }

    /** Applies one jitter roll to an attribute, rounding the way upstream does. */
    private static void jitter(Mob mob, Holder<Attribute> attribute, double factor, double roll) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null || instance.hasModifier(MODIFIER_ID)) {
            return;
        }
        double base = instance.getBaseValue();
        instance.setBaseValue(round(jitter(base, factor, roll), 2));
        mob.setHealth(Math.min(mob.getHealth(), mob.getMaxHealth()));
    }

    /** Adds a flat amount to an attribute (the speed and big zombie bonuses). */
    private static void addFlat(Mob mob, Holder<Attribute> attribute, double amount) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(Math.max(1.0, round(instance.getBaseValue() + amount, 2)));
        }
    }

    /**
     * Multiplies an attribute. Movement speed has no cap in rpgdifficulty, so
     * unlike {@link #applyFactor} this does not clamp.
     */
    private static void multiply(Mob mob, Holder<Attribute> attribute, double factor) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(round(instance.getBaseValue() * factor, 3));
        }
    }

    /** rpgdifficulty rounds health and damage to 2 decimals, speed to 3. */
    private static double round(double value, int decimals) {
        double scale = decimals == 2 ? 100.0 : 1000.0;
        return Math.round(value * scale) / scale;
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
