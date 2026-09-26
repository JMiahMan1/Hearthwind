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
    public final Hydration hydration = new Hydration();

    /**
     * Bare-hand cupping (sneak + empty hand + hold right-click on water),
     * upstream Dehydration {@code water_souce_quench} / {@code water_sip_*}
     * semantics. Aged override: chance 0.5, duration 300.
     */
    public static class BareHand {
        /** Thirst level points granted per completed sip (upstream 1). */
        public int waterSourceQuench = 1;
        /** Chance the sip gives the thirst effect (halved in river biomes). */
        public double waterSipThirstChance = 0.5;
        /** Duration of the thirst effect in ticks. */
        public int waterSipThirstDuration = 300;
        /**
         * Consume the still water source after a sip (upstream removes the
         * block whenever {@code allow_non_flowing_water_sip} is false).
         */
        public boolean consumeStillSource = true;
        /** Allow cupping from non-still (flowing) water. */
        public boolean allowNonFlowingWaterSip = false;
    }

    /**
     * Leather flask tunables, upstream Dehydration {@code flask_*} /
     * {@code potion_bad_thirst_*} / {@code milk_*} config semantics.
     * Aged override: dirty chance 0.3, dirty duration 200,
     * potion bad-thirst chance 0.15.
     */
    public static class Flask {
        /** Thirst level points per flask sip (upstream flask_thirst_quench). */
        public int quench = 4;
        /** Chance of the thirst effect per dirty-water sip (amplifier 1). */
        public double dirtyThirstChance = 0.3;
        /** Chance of the thirst effect per impure-water sip (amplifier 0). */
        public double impureThirstChance = 0.15;
        /** Duration of the thirst effect in ticks. */
        public int thirstDuration = 200;
        /** Chance the thirst effect follows a bad potion drink. */
        public double potionBadThirstChance = 0.15;
        /** Duration of the thirst effect from a bad potion in ticks. */
        public int potionBadThirstDuration = 300;
        /** Thirst level points per milk bucket (upstream milk_thirst_quench). */
        public int milkQuench = 8;
        /** Chance the thirst effect follows a milk drink (0.4 upstream). */
        public double milkThirstChance = 0.4;
        /** Thirst level points per honey bottle (upstream honey_quench). */
        public int honeyQuench = 1;
        /** Thirst level points per water bowl (upstream water_bowl_quench). */
        public int waterBowlQuench = 3;
        /** Chance a dirty water bowl gives the thirst effect (0.4 upstream). */
        public double waterBowlThirstChance = 0.4;
    }

    /**
     * Upstream Dehydration {@code ThirstManager}/config semantics: the
     * manager keeps a 0..20 level plus a 0..40 dehydration buffer that
     * only fills from {@code Player.causeFoodExhaustion(exhaustion)}
     * divided by {@link #hydratingFactor}. There is no passive drain.
     */
    public static class Thirst {
        /** Upstream {@code hydrating_factor}; Aged override 2.0. */
        public double hydratingFactor = 2.0;
        /** Upstream {@code thirst_damage}; 1.0 = half-heart per 90 ticks at level 0. */
        public double thirstDamage = 1.0;
        /** Upstream {@code thirst_effect_factor}; Aged override 0.03 per amplifier per tick. */
        public double thirstEffectFactor = 0.03;
        /** Upstream {@code sleep_thirst_consumption}. */
        public int sleepThirstConsumption = 4;
        /** Upstream {@code sleep_hunger_consumption}. */
        public int sleepHungerConsumption = 2;
        /** Upstream {@code harder_nether} (off in Aged). */
        public boolean harderNether = false;
        /** Upstream {@code nether_factor} (only when harderNether). */
        public double netherFactor = 2.0;
        /**
         * Use the migrated hydration corpus (data/dehydration/hydration_items)
         * so foods and drinks restore hydration by tier.
         */
        public boolean useHydrationCorpus = true;
        /** Multiplier applied to catalogued hydration tiers (1 = catalogue value). */
        public double hydrationCorpusScale = 1.0;
        /**
         * Reference {@code potion_thirst_quench} fallback: hydration granted
         * by a potion that is not in the hydration corpus.
         */
        public double potionThirstQuench = 2.0;
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

    /**
     * Dehydration hydration blocks (campfire + copper cauldrons), upstream
     * {@code water_boiling_time} semantics. Aged 1.3.6 ships 0.1/0.15 rain
     * and snow fill chances for the copper cauldron and 0.2 for the campfire
     * cauldron.
     */
    public static class Hydration {
        /** Ticks of boiling on a lit campfire before water turns purified (Aged: 100). */
        public int waterBoilingTime = 100;
        /** Rain fill chance per precipitation tick for the copper cauldron (Aged: 0.1). */
        public double copperRainFillChance = 0.1;
        /** Snow fill chance per precipitation tick for the copper cauldron (Aged: 0.15). */
        public double copperSnowFillChance = 0.15;
        /** Rain fill chance per precipitation tick for the campfire cauldron (Aged: 0.2). */
        public double campfireRainFillChance = 0.2;
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
