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
    public final BareHand bareHand = new BareHand();
    public final Flask flask = new Flask();
    public final Temperature temperature = new Temperature();
    public final Diet diet = new Diet();
    public final Spoilage spoilage = new Spoilage();

    /** Bare-hand cupping (sneak + empty hand + hold right-click on water). */
    public static class BareHand {
        /** Hydration points granted per completed sip (two sips = half droplet). */
        public double sipQuench = 0.5;
        /** Chance the sip gives the thirst effect (halved in river biomes). */
        public double sipThirstChance = 0.5;
        /** Duration of the thirst effect in ticks. */
        public int sipThirstDuration = 300;
        /** Consuming the still water source after a sip. */
        public boolean consumeStillSource = true;
        /** Allow cupping from non-still (flowing) water. */
        public boolean allowNonFlowingWaterSip = false;
    }

    /** Leather flask drink tunables. */
    public static class Flask {
        /** Hydration points per flask sip (scale 0..20). */
        public double quench = 4.0;
        /** Chance of the thirst effect per dirty-water sip (amplifier 1). */
        public double dirtyThirstChance = 0.3;
        /** Chance of the thirst effect per impure-water sip (amplifier 0). */
        public double impureThirstChance = 0.15;
        /** Duration of the thirst effect in ticks. */
        public int thirstDuration = 200;
    }

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
        /** Drain multiplier in icy biomes. */
        public double icyDrainMod = 1.5;
        /** Drain multiplier in cold biomes. */
        public double coldDrainMod = 1.2;
        /** Drain multiplier in neutral biomes. */
        public double neutralDrainMod = 1.0;
        /** Drain multiplier in warm biomes. */
        public double warmDrainMod = 1.1;
        /** Drain multiplier in hot biomes. */
        public double hotDrainMod = 1.3;
        /** Chance of dirty water sickness when drinking from open water. */
        public double dirtyWaterSicknessChance = 0.3;
        /** Duration of dirty water sickness in ticks. */
        public int dirtyWaterSicknessDuration = 600;
        /** Duration of throat irritation in ticks. */
        public int throatIrritationDuration = 300;
        /**
         * Use the migrated hydration corpus (data/dehydration/hydration_items)
         * so foods and drinks restore hydration by tier instead of relying on
         * the flask alone.
         */
        public boolean useHydrationCorpus = true;
        /** Multiplier applied to catalogued hydration tiers (1 = catalogue value). */
        public double hydrationCorpusScale = 1.0;
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
        /** Temperature offset in icy biomes. */
        public double icyOffset = -3.0;
        /** Temperature offset in cold biomes. */
        public double coldOffset = -1.5;
        /** Temperature offset in hot biomes. */
        public double hotOffset = 2.0;
        /** Activity temperature offset while sprinting. */
        public double activityOffset = 0.5;
        /** Temperature change rate per tick. */
        public double changeRatePerTick = 0.001;
        /** Duration of cold debuff in ticks. */
        public int coldDebuffDuration = 600;
        /** Duration of heat stroke in ticks. */
        public int heatStrokeDuration = 400;
        /**
         * Use the migrated environmentz corpus (data/environmentz/manager) for
         * day/night, armor, wetness, shadow and height modifiers instead of the
         * hand-tuned constants below. Falls back to the constants when no
         * corpus is installed.
         */
        public boolean useEnvironmentzTables = true;
        /** Radius scanned for heating/cooling blocks (0 disables block heat). */
        public int heatBlockRadius = 3;
        /** Bonus multiplier applied to fire heat in an enclosed room. */
        public double roomHeatFactor = 0.5;
        /** Radius used to decide whether a player is sheltered. */
        public int enclosedRadius = 3;
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
        /** Require farming skill to eat proteins (mirrors Aged early-game gate). */
        public boolean proteinsRequireFarming = true;
        /** Require farming skill to eat grains (mirrors Aged early-game gate). */
        public boolean grainsRequireFarming = true;
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
