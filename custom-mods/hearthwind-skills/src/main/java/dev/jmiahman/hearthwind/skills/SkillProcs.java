package dev.jmiahman.hearthwind.skills;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;

/**
 * Combat and husbandry procs granted by skill levels.
 *
 * The roll functions are pure (they take a {@link RandomSource}) so they can
 * be gametested deterministically; the wiring that uses them lives in
 * {@link #register()} and in the two mixins.
 *
 * With the tuning values the pack ships, only crits and fall protection scale
 * with the level - the rest are capstones that need the skill at maximum
 * level, matching the reference progression mod.
 */
public final class SkillProcs {
    /** Outcome of one melee swing; multiplier 1 means a plain hit. */
    public record MeleeRoll(boolean crit, boolean doubled, float multiplier) {
        public static final MeleeRoll NONE = new MeleeRoll(false, false, 1.0f);
    }

    private SkillProcs() {}

    /** True when {@code chance} fires against the given source of randomness. */
    public static boolean roll(RandomSource rng, double chance) {
        return chance > 0.0 && rng.nextFloat() < chance;
    }

    private static boolean isMax(Entity entity, Skill skill) {
        return SkillXp.level(entity, skill) >= SkillsConfig.get().levels.maxLevel;
    }

    private static boolean capstone(Entity entity, Skill skill) {
        SkillsConfig.Procs p = SkillsConfig.get().procs;
        return p.enabled
                && (!p.capstonesRequireMaxLevel || isMax(entity, skill));
    }

    /** Crit chance for a LUCK level: {@code level * critChancePerLuckLevel}. */
    public static double critChance(int luckLevel) {
        return luckLevel * SkillsConfig.get().procs.critChancePerLuckLevel;
    }

    /**
     * Rolls the attacker's melee swing: a max-level STRENGTH capstone can
     * double the hit, otherwise LUCK can turn it into a crit.
     */
    public static MeleeRoll rollMelee(Player attacker, RandomSource rng) {
        SkillsConfig.Procs p = SkillsConfig.get().procs;
        if (!p.enabled) {
            return MeleeRoll.NONE;
        }
        if (capstone(attacker, Skill.STRENGTH)
                && roll(rng, p.meleeDoubleDamageChance)) {
            return new MeleeRoll(false, true, 2.0f);
        }
        int luck = SkillXp.level(attacker, Skill.LUCK);
        if (luck > 0 && roll(rng, critChance(luck))) {
            return new MeleeRoll(true, false, (float) (1.0 + p.critDamageBonus));
        }
        return MeleeRoll.NONE;
    }

    /** Applies a {@link MeleeRoll} to a damage amount. */
    public static float applyMelee(float amount, MeleeRoll roll) {
        return amount * roll.multiplier();
    }

    /** Whether an incoming attack is dodged outright (AGILITY capstone). */
    public static boolean dodges(Player victim, RandomSource rng) {
        SkillsConfig.Procs p = SkillsConfig.get().procs;
        return capstone(victim, Skill.AGILITY) && roll(rng, p.missChance);
    }

    /** Whether the victim reflects the damage back (DEFENSE capstone). */
    public static boolean reflects(Player victim, RandomSource rng) {
        SkillsConfig.Procs p = SkillsConfig.get().procs;
        return capstone(victim, Skill.DEFENSE) && roll(rng, p.reflectChance);
    }

    /** Whether a lethal hit is survived at 1 HP (LUCK capstone). */
    public static boolean survivesDeath(Player victim, RandomSource rng) {
        SkillsConfig.Procs p = SkillsConfig.get().procs;
        return capstone(victim, Skill.LUCK) && roll(rng, p.surviveChance);
    }

    /** Whether breeding yields a second baby (FARMING capstone). */
    public static boolean twinBaby(Player breeder, RandomSource rng) {
        SkillsConfig.Procs p = SkillsConfig.get().procs;
        return capstone(breeder, Skill.FARMING) && roll(rng, p.twinBabyChance);
    }

    /** Fall damage after the AGILITY reduction, never below zero. */
    public static float applyFallProtection(Player victim, float amount) {
        SkillsConfig.Procs p = SkillsConfig.get().procs;
        if (!p.enabled) {
            return amount;
        }
        double reduction = SkillXp.level(victim, Skill.AGILITY)
                * p.fallProtectionPerAgilityLevel;
        return (float) Math.max(0.0, amount - reduction);
    }

    /** Registers the fabric hooks for the passive/capstone procs. */
    public static void register() {
        // AGILITY capstone: the attack simply misses.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer victim)) {
                return true;
            }
            return !dodges(victim, rngFor(victim));
        });

        // LUCK capstone: cheat death once, come back at 1 HP.
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer victim)) {
                return true;
            }
            if (!survivesDeath(victim, rngFor(victim))) {
                return true;
            }
            victim.setHealth(1.0f);
            victim.clearFire();
            victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0));
            return false;
        });

        // DEFENSE capstone: hand the damage straight back.
        ServerLivingEntityEvents.AFTER_DAMAGE
                .register((entity, source, originalDamage, amount, blocked) -> {
                    if (blocked || !(entity instanceof ServerPlayer victim)) {
                        return;
                    }
                    // never bounce our own reflection back and forth
                    if (source.is(DamageTypes.THORNS)) {
                        return;
                    }
                    Entity attacker = source.getEntity();
                    if (attacker == null || attacker == victim
                            || !(attacker instanceof LivingEntity living)) {
                        return;
                    }
                    if (!reflects(victim, rngFor(victim))) {
                        return;
                    }
                    if (victim.level() instanceof ServerLevel serverLevel) {
                        living.hurtServer(serverLevel,
                                victim.damageSources().thorns(victim),
                                originalDamage);
                    }
                });
    }

    private static RandomSource rngFor(Entity entity) {
        return entity.level().getRandom();
    }
}
