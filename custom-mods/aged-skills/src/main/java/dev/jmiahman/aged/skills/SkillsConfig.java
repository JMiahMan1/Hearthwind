package dev.jmiahman.aged.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Tunables for <code>config/aged_skills.json</code>; created with defaults
 * on first boot. Same conventions as AgedSurvivalConfig.
 */
public final class SkillsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "aged_skills.json";

    public final Levels levels = new Levels();
    public final Xp xp = new Xp();
    public final Bonuses bonuses = new Bonuses();

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
        /** Bonus max health (HP) per HEALTH level. */
        public double healthHpPerLevel = 0.5;
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
            AgedSkills.LOGGER.warn("Could not read/write {}: using defaults", path, e);
        }
        return cfg;
    }
}
