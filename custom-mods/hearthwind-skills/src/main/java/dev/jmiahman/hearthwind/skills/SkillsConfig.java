package dev.jmiahman.hearthwind.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Tunables for <code>config/hearthwind_skills.json</code>; created with defaults
 * on first boot. Same conventions as HearthwindSurvivalConfig.
 */
public final class SkillsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "hearthwind_skills.json";

    public final Levels levels = new Levels();
    public final Xp xp = new Xp();
    public final Bonuses bonuses = new Bonuses();
    public final MobScaling mobScaling = new MobScaling();
    public final Gates gates = new Gates();

    /**
     * Unspent skill points a brand-new player starts with - Aged's
     * <code>levelz.json5</code> ships <code>"startPoints": 2</code> with
     * <code>"enableStartPoints": true</code>, so a fresh Aged player can spend
     * two points in the Skills screen before earning anything. Granted once,
     * on the player's first join, next to the two vanilla XP bar levels.
     */
    public int startSkillPoints = 2;
    public final Procs procs = new Procs();

    /** Public no-arg ctor required so Gson keeps field-initializer defaults. */
    public SkillsConfig() {}

    public static class Levels {
        /** Maximum reachable level per skill (Aged/LevelZ maxLevel). */
        public int maxLevel = 30;
        /**
         * Aged/LevelZ XP curve: the cost to go from level L to L+1 is
         * <code>(int)(xpBaseCost + xpCostMultiplicator * L^xpExponent)</code>
         * (25 + 1.6*L with the Aged config).
         */
        public int xpBaseCost = 25;
        public double xpCostMultiplicator = 1.6;
        public double xpExponent = 1.0;
    }

    public static class Xp {
        /** Mining-skill XP per mined pickaxe-mineable block. */
        public double miningPerBlock = 2.0;
        /** Farming-skill XP per harvested crop block / bred animal kill? (crops only v1). */
        public double farmingPerCrop = 4.0;
        /**
         * Stamina XP per dug shovel-mineable block. Aged's levelz.json5 ships
         * {@code "staminaBase": 1.1}; we carried 1.0 for the whole port, so
         * every player earned 9.1% less Stamina XP than the reference (0.1.54).
         * The two derived awards in {@code SkillEvents} (0.1x and 0.15x this
         * value) scale with it, so one constant moves all three.
         */
        public double staminaPerDig = 1.1;
        /** Strength XP per hostile mob melee kill. */
        public double strengthPerMeleeKill = 6.0;
        /** Archery XP per ranged-weapon kill. */
        public double archeryPerRangedKill = 6.0;
        /** Archery XP per successful projectile hit (not kill). */
        public double archeryPerHit = 1.5;
        /** Farming XP per passive-animal kill (husbandry cull). */
        public double farmingPerAnimalKill = 2.0;
        /** Defense XP each time the player takes incoming entity damage. */
        public double defensePerHit = 1.0;
        /** Smithing XP awarded upon taking a completed craft from the smithing table. */
        public double smithingPerCraft = 15.0;
        /** Alchemy XP awarded upon brewing completion or potion creation. */
        public double alchemyPerBrew = 8.0;
        /** Agility XP awarded per sprint / travel distance milestone. */
        public double agilityPerDistance = 1.0;
        /** Health XP awarded upon natural health regeneration / eating nutrient food. */
        public double healthPerRegen = 1.0;
        /** Luck XP awarded upon fishing / discovering rare loot. */
        public double luckPerFishing = 5.0;
        /** Trade XP per successful villager trade. */
        public double tradePerTransaction = 2.0;
    }

    public static class Bonuses {
        /** Base starting player health in HP (6.0 = 3 hearts, authentic Aged / LevelZ progression). */
        public double baseStartingHealth = 6.0;
        /** Bonus max health (HP) per HEALTH level (LevelZ healthBonus 1.0). */
        public double healthHpPerLevel = 1.0;
        /** Bonus attack damage per STRENGTH level (LevelZ attackBonus 0.2). */
        public double strengthDamagePerLevel = 0.2;
        /** LevelZ movementBase: player base movement speed at level 0. */
        public double agilityBaseMovement = 0.09;
        /** Fractional movement speed bonus per AGILITY level (LevelZ movementBonus 0.001). */
        public double agilitySpeedFractionPerLevel = 0.001;
        /** Armor points per DEFENSE level (LevelZ defenseBonus 0.2). */
        public double defenseArmorPerLevel = 0.2;
        /** Fractional block-break speed bonus per MINING level. */
        public double miningSpeedFractionPerLevel = 0.01;
        /** Luck points per LUCK level (LevelZ luckBonus 0.05). */
        public double luckPerLevel = 0.05;
    }

    public static class MobScaling {
        /** Master switch for distance-based monster scaling (rpgdifficulty parity). */
        public boolean enabled = true;
        /** Distance from world spawn before any scaling applies (blocks) - rpgdifficulty: startingDistance 300. */
        public double graceDistance = 300.0;
        /** One scaling step per this many blocks beyond the grace distance - rpgdifficulty: increasingDistance 200. */
        public double stepBlocks = 200.0;
        /** Stat multiplier gained per distance step - rpgdifficulty: distanceFactor 0.05. */
        public double distanceFactor = 0.05;
        /** One height step per this many blocks from {@code startingHeight} - rpgdifficulty: heightDistance 25. */
        public double heightDistance = 25.0;
        /** Stat multiplier gained per height step - rpgdifficulty: heightFactor 0.1. */
        public double heightFactor = 0.1;
        /** Height the height scaling starts from - rpgdifficulty: startingHeight 62. */
        public double startingHeight = 62.0;
        /** Health multiplier cap - rpgdifficulty: maxFactorHealth 4.0. */
        public double maxFactorHealth = 4.0;
        /** Attack damage multiplier cap - rpgdifficulty: maxFactorDamage 3.0. */
        public double maxFactorDamage = 3.0;
        /** Armor multiplier cap - rpgdifficulty: maxFactorProtection 2.0. */
        public double maxFactorProtection = 2.0;
        /**
         * Every non-excluded mob's damage factor is multiplied by this, on top
         * of the distance growth - rpgdifficulty: creeperExplosionFactor 1.1.
         * The name is a leftover in upstream: the reference applies it to the
         * general damage factor, not to creepers only, so a port that honours
         * the name would quietly differ from Aged by 10% on all mob damage.
         */
        public double creeperExplosionFactor = 1.1;
        /** Kill XP scales with the mob's health factor - rpgdifficulty: extraXp. */
        public boolean extraXp = true;
        /** Cap on that XP multiplier - rpgdifficulty: maxXPFactor 4.0. */
        public double maxXPFactor = 4.0;
        /** A dangerous mob may drop extra stacks - rpgdifficulty: dropMoreLoot. */
        public boolean dropMoreLoot = true;
        /** Extra-drop chance per point of health factor - rpgdifficulty: moreLootChance 0.02. */
        public double moreLootChance = 0.02;
        /** Cap on the extra-drop chance - rpgdifficulty: maxLootChance 2.0. */
        public double maxLootChance = 2.0;
        /**
         * Chance an individual stack is skipped when the extra drop happens -
         * rpgdifficulty: chanceForEachItem 0.5. This is the "how much of the
         * extra loot" knob, and a 0.5 here means the extra drop is roughly half
         * as big as the reference's arithmetic suggests.
         */
        public double chanceForEachItem = 0.5;
        /** Hard cap on total steps a mob can receive - rpgdifficulty: maxFactorHealth 4.0 (60 steps). */
        public int maxSteps = 60;
        /** No distance scaling outside the overworld - rpgdifficulty: excludeDistanceInOtherDimension. */
        public boolean excludeDistanceInOtherDimension = true;
        /** No height scaling outside the overworld - rpgdifficulty: excludeHeightInOtherDimension. */
        public boolean excludeHeightInOtherDimension = true;
        /**
         * Entity ids that never scale at all - rpgdifficulty: excludedEntity
         * (Aged ships the warden's and the ender dragon's; the third entry
         * upstream lists belongs to a mod this pack does not ship).
         */
        public List<String> excludedEntities = new ArrayList<>(List.of(
                "minecraft:warden", "minecraft:ender_dragon"));
        /** Whether the {@code c:bosses} members and the dragon scale at all - rpgdifficulty: affectBosses. */
        public boolean affectBosses = true;
        /** Boss health cap - rpgdifficulty: bossMaxFactor 3.0. */
        public double bossMaxFactorHealth = 3.0;
        /** Health gained per distance step for bosses - rpgdifficulty: bossDistanceFactor 0.05. */
        public double bossDistanceFactor = 0.05;
        /** Whether a boss grows with every player who is fighting it - rpgdifficulty: dynamicBossModification. */
        public boolean dynamicBossModification = true;
        /** Health added per nearby player - rpgdifficulty: dynamicBossModificator 0.3. */
        public double dynamicBossModificator = 0.3;
        /** How close a player must be to count as fighting the boss (rpgdifficulty: 128-block box). */
        public double dynamicBossRadius = 128.0;

        /*
         * The three rolls below run on EVERY scaled mob, not only the ones past
         * a distance step: rpgdifficulty has no early return between the
         * distance maths and them, so a zombie spawned next to spawn can still
         * come out big. They are gated separately from the caps above because
         * they are per-mob rolls, not multipliers.
         */

        /** Whether each mob's health and damage get a random jitter - rpgdifficulty: allowRandomValues. */
        public boolean allowRandomValues = true;
        /** Percent chance a mob is jittered - rpgdifficulty: randomChance 30. */
        public int randomChance = 30;
        /** Half-width of the jitter, as a percent of the stat - rpgdifficulty: randomFactor 3. */
        public double randomFactor = 3.0;
        /** Whether zombies can roll for the special variants - rpgdifficulty: allowSpecialZombie. */
        public boolean allowSpecialZombie = true;
        /** Percent chance a zombie is a fast, fragile one - rpgdifficulty: speedZombieChance 5. */
        public int speedZombieChance = 5;
        /** Movement speed multiplier for a fast zombie - rpgdifficulty: speedZombieSpeedFactor 1.2. */
        public double speedZombieSpeedFactor = 1.2;
        /** Max health subtracted from a fast zombie - rpgdifficulty: speedZombieMalusLifePoints 10. */
        public double speedZombieMalusLifePoints = 10.0;
        /** Percent chance a zombie is a slow, tanky, oversized one - rpgdifficulty: bigZombieChance 10. */
        public int bigZombieChance = 10;
        /** Movement speed multiplier for a big zombie - rpgdifficulty: bigZombieSlownessFactor 0.7. */
        public double bigZombieSlownessFactor = 0.7;
        /** Max health added to a big zombie - rpgdifficulty: bigZombieBonusLifePoints 10. */
        public double bigZombieBonusLifePoints = 10.0;
        /** Attack damage added to a big zombie - rpgdifficulty: bigZombieBonusDamage 2. */
        public double bigZombieBonusDamage = 2.0;
        /** Hitbox and model scale of a big zombie - rpgdifficulty: bigZombieSize 1.3. */
        public double bigZombieSize = 1.3;
    }

    public static class Gates {
        /** Master switch for break/use skill gates (levelz parity). */
        public boolean enabled = true;
    }

    /**
     * Combat and husbandry proc chances; defaults are the tuning values the
     * pack ships. Except for crits and fall protection - which scale with the
     * skill level - every proc is a capstone that only fires at max level,
     * exactly like the reference progression mod.
     */
    public static class Procs {
        /** Master switch for every proc below. */
        public boolean enabled = true;
        /** Crit chance per LUCK level (level 30 = 30% at the default). */
        public double critChancePerLuckLevel = 0.01;
        /** Extra damage fraction applied on a crit (0.2 = +20%). */
        public double critDamageBonus = 0.2;
        /** Chance a melee hit deals double damage. */
        public double meleeDoubleDamageChance = 0.03;
        /** Chance to take no damage from an attack at all. */
        public double missChance = 0.1;
        /** Chance to reflect the damage back at the attacker. */
        public double reflectChance = 0.05;
        /** Chance to survive a lethal hit at 1 HP. */
        public double surviveChance = 0.5;
        /** Chance that breeding produces a second baby. */
        public double twinBabyChance = 0.2;
        /** Fall damage reduced by this much per AGILITY level. */
        public double fallProtectionPerAgilityLevel = 0.25;
        /**
         * Whether the capstone procs (double damage, miss, reflect, survive,
         * twins) need the skill at maximum level before they can roll.
         */
        public boolean capstonesRequireMaxLevel = true;
    }

    private static SkillsConfig instance;

    public static SkillsConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static SkillsConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        SkillsConfig cfg = new SkillsConfig();
        try {
            if (Files.exists(path)) {
                cfg = GSON.fromJson(Files.readString(path), SkillsConfig.class);
                if (cfg == null) {
                    cfg = new SkillsConfig();
                }
            }
            Files.writeString(path, GSON.toJson(cfg));
        } catch (IOException e) {
            HearthwindSkills.LOGGER.warn("Could not read/write {}: using defaults", path, e);
        }
        return cfg;
    }
}
