package dev.jmiahman.hearthwind.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    public final Procs procs = new Procs();

    /** Public no-arg ctor required so Gson keeps field-initializer defaults. */
    public SkillsConfig() {}

    public static class Levels {
        /** Maximum reachable level per skill (levelz parity). */
        public int maxLevel = 30;
        /** XP needed to go from level N-1 to N equals baseXpPerLevel * N. */
        public int baseXpPerLevel = 30;
    }

    public static class Xp {
        /** Mining-skill XP per mined pickaxe-mineable block. */
        public double miningPerBlock = 2.0;
        /** Farming-skill XP per harvested crop block / bred animal kill? (crops only v1). */
        public double farmingPerCrop = 4.0;
        /** Stamina XP per dug shovel-mineable block. */
        public double staminaPerDig = 1.0;
        /** Strength XP per hostile mob melee kill. */
        public double strengthPerMeleeKill = 6.0;
        /** Archery XP per ranged-weapon kill. */
        public double archeryPerRangedKill = 6.0;
        /** Farming XP per passive-animal kill (husbandry cull). */
        public double farmingPerAnimalKill = 2.0;
    }

    public static class Bonuses {
        /** Base starting player health in HP (6.0 = 3 hearts, authentic Aged / LevelZ progression). */
        public double baseStartingHealth = 6.0;
        /** Bonus max health (HP) per HEALTH level (+1 HP per level = +0.5 heart). */
        public double healthHpPerLevel = 1.0;
        /** Bonus attack damage per STRENGTH level. */
        public double strengthDamagePerLevel = 0.25;
        /** Fractional movement speed bonus per AGILITY level (0.005 = 0.5%). */
        public double agilitySpeedFractionPerLevel = 0.005;
        /** Armor points per DEFENSE level. */
        public double defenseArmorPerLevel = 0.3;
        /** Fractional block-break speed bonus per MINING level. */
        public double miningSpeedFractionPerLevel = 0.01;
        /** Luck points per LUCK level. */
        public double luckPerLevel = 0.1;
    }

    public static class MobScaling {
        /** Master switch for distance-based monster scaling (rpgdifficulty parity). */
        public boolean enabled = true;
        /** Distance from world spawn before any scaling applies (blocks) - rpgdifficulty: startingDistance 300. */
        public double graceDistance = 300.0;
        /** One scaling step per this many blocks beyond the grace distance - rpgdifficulty: increasingDistance 200. */
        public double stepBlocks = 200.0;
        /** Extra max health (HP) per step (5% of 20 base = 1.0 HP). */
        public double healthPerStep = 1.0;
        /** Extra attack damage per step (5% of 6 base = 0.3 dmg). */
        public double damagePerStep = 0.3;
        /** Hard cap on total steps a mob can receive - rpgdifficulty: maxFactorHealth 4.0 (60 steps). */
        public int maxSteps = 60;
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
