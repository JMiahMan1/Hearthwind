package dev.jmiahman.hearthwind.survival;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * All gameplay tunables live in <code>config/hearthwind_survival.json</code>.
 * Created with defaults on first boot; edit + restart to apply.
 */
public final class HearthwindSurvivalConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "hearthwind_survival.json";

    public final Thirst thirst = new Thirst();
    public final Temperature temperature = new Temperature();
    public final Diet diet = new Diet();
    public final Spoilage spoilage = new Spoilage();

    public static class Thirst {
        /** Hydration points lost per second under normal activity. Scale 0..20. 20/0.025=800s (~13 min) to empty, similar to hunger. */
        public double baseDrainPerSecond = 0.025;
        /** Multiplier applied while sprinting. */
        public double sprintMultiplier = 2.0;
        /** Extra drain per second per amplifier of the dehydration:thirst effect. */
        public double thirstEffectDrainPerSecond = 0.05;
        /** Hydration must exceed this for natural health regeneration. */
        public double regenHydrationFloor = 6.0;
        /** Seconds between starvation-style damage ticks at zero hydration. */
        public double damageIntervalSeconds = 4.0;
        /** Damage per tick at zero hydration (half-hearts). */
        public double damageAmount = 1.0;
    }

    public static class Temperature {
        /** Degrees drifted toward the biome target per second. Scale -10..+10. */
        public double driftPerSecond = 0.05;
        /** Body temperature below which freeze damage starts. */
        public double freezeHurtAt = -8.0;
        /** Body temperature above which heat exhaustion (food drain) starts. */
        public double heatExhaustAt = 7.0;
        /** Body temperature above which heat damage starts. */
        public double heatHurtAt = 9.0;
        /** Cooldown seconds between repeated extreme-temperature damage. */
        public double hurtCooldownSeconds = 4.0;
    }

    public static class Diet {
        /** Nutrient decay per second per group. Scale 0..100. */
        public double decayPerSecond = 0.02;
        /** Nutrients granted per point of vanilla food nutrition per group match. */
        public double nutrientsPerFoodPoint = 4.0;
        /** Below this a group counts as deficient (debuff). */
        public double deficiencyThreshold = 15.0;
        /** All groups at or above this count as a balanced diet (bonus hearts). */
        public double balanceThreshold = 50.0;
        /** Half-heart absorption pool refreshed while balanced (0 disables). */
        public float balancedBonusHearts = 2.0f;
    }

    public static class Spoilage {
        /** Interval between spoilage checks (ticks). */
        public int checkIntervalTicks = 200;
        /** Chance per check that ONE stack slot rots one item further. */
        public double chancePerCheck = 0.002;
        /** Extra chance multiplier while the owner is in a hot biome (>1.5 temp). */
        public double hotBiomeMultiplier = 2.0;
        /** Item id perishables rot into. */
        public String rotsInto = "minecraft:rotten_flesh";
    }

    private static HearthwindSurvivalConfig instance;

    public static HearthwindSurvivalConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static HearthwindSurvivalConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        HearthwindSurvivalConfig cfg = new HearthwindSurvivalConfig();
        try {
            if (Files.exists(path)) {
                cfg = GSON.fromJson(Files.readString(path), HearthwindSurvivalConfig.class);
                if (cfg == null) {
                    cfg = new HearthwindSurvivalConfig();
                }
            }
            Files.writeString(path, GSON.toJson(cfg));
        } catch (IOException e) {
            HearthwindSurvival.LOGGER.warn("Could not read/write {}: using defaults", path, e);
        }
        return cfg;
    }

    /** Public no-arg ctor is required so Gson keeps field-initializer defaults. */
    public HearthwindSurvivalConfig() {}
}
