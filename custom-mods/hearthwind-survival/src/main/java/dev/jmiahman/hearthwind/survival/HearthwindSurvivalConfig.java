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
    public final Sobriety sobriety = new Sobriety();

    /** Bare-hand cupping (sneak + empty hand + hold right-click on water). */
    public static class BareHand {
        /** Hydration points granted per completed sip (two sips = half droplet). */
        public double sipQuench = 1.0;
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

    /**
     * EnvironmentZ 2.0.8 tunables (Aged 3.1.2 overrides applied): the body
     * temperature model itself is data-driven from
     * {@code data/environmentz/manager}, exactly like the original mod.
     */
    public static class Temperature {
        /**
         * Recalculate once per this many ticks + 1 (reference
         * TemperatureManager semantics). Aged override: 10.
         */
        public int temperatureCalculationTime = 10;
        /** Try to keep the vanilla spawn point friendly (reference option). */
        public boolean easyWorldSpawn = true;
        /** Body icon X offset: drawn at width/2 - iconX. */
        public int iconX = 7;
        /** Body icon Y offset: drawn at height - iconY. */
        public int iconY = 52;
        /** Thermometer X offset: Aged override -95 (width/2 - (-95)). */
        public int thermometerIconX = -95;
        /** Thermometer Y offset: drawn at height - thermometerIconY. */
        public int thermometerIconY = 32;
        public boolean showThermometer = true;
        /** Reference startup comfort effect duration (ticks). */
        public int startUpComfortEffectDuration = 9600;
        /** Radius scanned for heating/cooling blocks, fluids and items. */
        public int heatBlockRadius = 3;
        public boolean printInConsole = false;
        /** Exhaustion added per overheating calculation. Aged override: 0.07. */
        public float overheatingExhaustion = 0.07F;
        /**
         * Reference Dehydration-compat option. Our thirst is in-house, so the
         * exhaustion path (food > 6) is the Aged-equivalent default.
         */
        public boolean exhaustionInsteadDehydration = true;
        public boolean coldOverlay = true;
        public boolean shakingScreenEffect = true;
        public boolean blurScreenEffect = true;
    }

    /**
     * NutritionZ 1.0.11 parity (the exact version Aged 3.1.2 ships).
     * Five integer nutrients (carbohydrates, protein, fat, vitamins,
     * minerals) on a 0..maxNutrition scale. Eating/drinking adds the
     * positive values from the loaded {@code data/<ns>/nutrition/*.json}
     * item maps; the {@code nutrition_manager} datapack decides the
     * threshold effects. There is no deficiency-debuff list in code -
     * effects are data-driven.
     */
    public static class Diet {
        /** Upper bound of each nutrient (original maxNutrition). */
        public int maxNutrition = 300;
        /** At or below this, the negative effect list applies. */
        public int negativeNutrition = 30;
        /** At or above this, the positive effect list applies. */
        public int positiveNutrition = 270;
        /** Icon shown on the first Nutrients row. */
        public String carbohydrateItemId = "minecraft:sugar";
        /** Icon shown on the second Nutrients row. */
        public String proteinItemId = "minecraft:chicken";
        /** Icon shown on the third Nutrients row. */
        public String fatItemId = "minecraft:porkchop";
        /** Icon shown on the fourth Nutrients row (Aged override). */
        public String vitaminItemId = "farm_and_charm:lettuce";
        /** Icon shown on the fifth Nutrients row (Aged override). */
        public String mineralItemId = "meadow:alpine_salt";
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

    /** Family-friendly brewing: alcohol becomes juice / NA medieval drinks. */
    public static class Sobriety {
        /** When true, letsdo mods unregister alcohol and ship juice/NA drinks instead. */
        public boolean removeAlcohol = true;
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
