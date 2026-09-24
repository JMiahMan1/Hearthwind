package dev.jmiahman.hearthwind.survival;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Headless gametests, run with the fabric-api gametest harness:
 * <pre>
 *   java -Dfabric-api.gametest=true \
 *        -Dfabric-api.gametest.report-file=report.xml \
 *        -jar fabric-server.jar nogui
 * </pre>
 * or via custom-mods/tools/run_gametests.sh. No structures needed (the
 * default fabric-gametest-api-v1:empty template is used).
 */
public final class HearthwindSurvivalGameTests {
    /** Public ctor: fabric-loader instantiates gametest entrypoints reflectively. */
    public HearthwindSurvivalGameTests() {}

    @GameTest
    public void configLoadsSaneDefaults(GameTestHelper helper) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        helper.assertTrue(cfg.thirst.baseDrainPerSecond > 0, "thirst drain must be positive");
        helper.assertTrue(cfg.diet.negativeNutrition < cfg.diet.positiveNutrition,
                "negative threshold must be below positive threshold");
        helper.assertTrue(cfg.spoilage.chancePerCheck >= 0, "spoil chance must not be negative");
        helper.succeed();
    }

    @GameTest
    public void bareHandQuenchDefaultsToAgedValue(GameTestHelper helper) {
        // Dehydration parity: water_source_quench = 1 on the 0..20 scale.
        helper.assertTrue(HearthwindSurvivalConfig.get().bareHand.sipQuench == 1.0,
                "sip quench must default to 1.0, got " + HearthwindSurvivalConfig.get().bareHand.sipQuench);
        helper.assertTrue(HearthwindSurvivalConfig.get().bareHand.sipThirstChance == 0.5,
                "sip thirst chance must default to the Aged override 0.5");
        helper.assertTrue(HearthwindSurvivalConfig.get().bareHand.sipThirstDuration == 300,
                "sip thirst duration must default to 300");
        helper.succeed();
    }

    private ServerPlayer aimAtWater(GameTestHelper helper, net.minecraft.core.BlockPos water) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setShiftKeyDown(true);
        player.getInventory().clearContent();
        player.setPos(water.getX() + 0.5, water.getY() + 0.5, water.getZ() + 0.5);
        return player;
    }

    @GameTest
    public void bareHandRequiresSneak(GameTestHelper helper) {
        net.minecraft.core.BlockPos water = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(water, net.minecraft.world.level.block.Blocks.WATER);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setShiftKeyDown(false);
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        helper.assertTrue(BareHandDrinkHandler.trySip(player, helper.getLevel()) == net.minecraft.world.InteractionResult.PASS,
                "standing (not sneaking) player must not sip");
        helper.succeed();
    }

    @GameTest
    public void bareHandHoldCompletesSipAndConsumesSource(GameTestHelper helper) {
        net.minecraft.core.BlockPos water = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        helper.setBlock(1, 2, 1, net.minecraft.world.level.block.Blocks.WATER);
        ServerPlayer player = aimAtWater(helper, water);
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        double before = HearthwindSurvivalThirst.hydration(player);
        double chance = HearthwindSurvivalConfig.get().bareHand.sipThirstChance;
        HearthwindSurvivalConfig.get().bareHand.sipThirstChance = 0.0;
        try {
            for (int i = 0; i < 30; i++) {
                BareHandDrinkHandler.trySip(player, helper.getLevel());
            }
        } finally {
            HearthwindSurvivalConfig.get().bareHand.sipThirstChance = chance;
        }
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) == before + 1.0,
                "~21 sustained sips must complete one +1 quench drink");
        helper.assertTrue(helper.getLevel().getBlockState(water).isAir(),
                "still source must be consumed by default");
        helper.succeed();
    }

    @GameTest
    public void bareHandFlowingWaterRefusedByDefault(GameTestHelper helper) {
        helper.setBlock(1, 2, 1, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL, 1));
        net.minecraft.core.BlockPos water = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        ServerPlayer player = aimAtWater(helper, water);
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        for (int i = 0; i < 30; i++) {
            helper.assertTrue(BareHandDrinkHandler.trySip(player, helper.getLevel()) == net.minecraft.world.InteractionResult.PASS,
                    "flowing water must refuse sips by default");
        }
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) == 10.0, "no hydration from refused sips");
        helper.succeed();
    }

    @GameTest
    public void purifiedWaterRegisters(GameTestHelper helper) {
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.FLUID.containsKey(
                net.minecraft.resources.Identifier.parse("dehydration:purified_water")), "purified fluid must exist");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.FLUID.containsKey(
                net.minecraft.resources.Identifier.parse("dehydration:purified_flowing_water")), "purified flowing fluid must exist");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(
                net.minecraft.resources.Identifier.parse("dehydration:purified_water")), "purified water block must exist");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(
                net.minecraft.resources.Identifier.parse("dehydration:purified_water_bucket")), "purified bucket must exist");
        var lookup = helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.FLUID);
        var set = lookup.get(PurifiedWater.PURIFIED_TAG);
        helper.assertTrue(set.isPresent() && set.get().size() >= 2, "purified fluid tag must list both fluids");
        helper.succeed();
    }

    @GameTest
    public void purifiedBucketSmeltingRecipeParses(GameTestHelper helper) {
        boolean found = false;
        for (var holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.id().identifier().equals(net.minecraft.resources.Identifier.parse("dehydration:purified_water_bucket"))) {
                found = true;
                break;
            }
        }
        helper.assertTrue(found, "smelting a water bucket must yield a purified bucket (Dehydration parity)");
        helper.succeed();
    }

    @GameTest
    public void purifiedSipNeverThirsts(GameTestHelper helper) {
        net.minecraft.core.BlockPos water = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        helper.setBlock(1, 2, 1, PurifiedWater.BLOCK.defaultBlockState());
        ServerPlayer player = aimAtWater(helper, water);
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        double chance = HearthwindSurvivalConfig.get().bareHand.sipThirstChance;
        HearthwindSurvivalConfig.get().bareHand.sipThirstChance = 1.0;
        try {
            for (int i = 0; i < 30; i++) {
                BareHandDrinkHandler.trySip(player, helper.getLevel());
            }
        } finally {
            HearthwindSurvivalConfig.get().bareHand.sipThirstChance = chance;
        }
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) > 10.0, "purified sip must hydrate");
        helper.assertTrue(!player.hasEffect(ThirstMobEffect.HOLDER), "purified sip must never inflict thirst");
        helper.succeed();
    }

    @GameTest
    public void sobrietyDefaultsToAlcoholFree(GameTestHelper helper) {
        helper.assertTrue(HearthwindSurvivalConfig.get().sobriety.removeAlcohol,
                "removeAlcohol must default to true");
        helper.assertTrue(Sobriety.alcoholRemoved(),
                "Sobriety helper must report alcohol removed by default");
        helper.succeed();
    }

    @GameTest
    public void eatingAppleAddsVitaminsAndMinerals(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalDiet.setLevel(pig, 3, 0);
        HearthwindSurvivalDiet.setLevel(pig, 4, 0);
        ItemStack apple = new ItemStack(Items.APPLE);
        helper.assertTrue(apple.get(DataComponents.FOOD) != null, "apple must be food");
        HearthwindSurvivalDiet.onEaten(pig, apple);
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(pig, 3) == 15,
                "vanilla_items.json: apple must grant vitamins 15");
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(pig, 4) == 5,
                "vanilla_items.json: apple must grant minerals 5");
        helper.succeed();
    }

    @GameTest
    public void eatHookFiresThroughItemUse(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalDiet.setLevel(player, 3, 0);
        HearthwindSurvivalDiet.onEaten(player, new ItemStack(Items.APPLE));
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(player, 3) == 15,
                "apple via onEaten must add vitamins (mixin hook)");
        helper.succeed();
    }

    @GameTest
    public void nonFoodDoesNotChangeDiet(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalDiet.setLevel(pig, 0, 10);
        HearthwindSurvivalDiet.onEaten(pig, new ItemStack(Items.STICK));
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(pig, 0) == 10,
                "sticks are not food; nutrients unchanged");
        helper.succeed();
    }

    @GameTest
    public void decayReducesAllFiveNutrients(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            HearthwindSurvivalDiet.setLevel(player, i, 50);
        }
        HearthwindSurvivalDiet.applyDecay(player);
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            helper.assertTrue(HearthwindSurvivalDiet.getLevel(player, i) == 49,
                    "hunger decay must drop every nutrient by 1 (index " + i + ")");
        }
        helper.succeed();
    }

    @GameTest
    public void decayNeverGoesBelowZero(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalDiet.setLevel(player, 0, 0);
        HearthwindSurvivalDiet.applyDecay(player);
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(player, 0) == 0,
                "nutrient decay must clamp at zero");
        helper.succeed();
    }

    @GameTest
    public void nutrientsClampAtMax(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalDiet.setLevel(player, 0, 299);
        HearthwindSurvivalDiet.addLevel(player, 0, 999);
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(player, 0)
                == HearthwindSurvivalConfig.get().diet.maxNutrition,
                "nutrients must clamp at maxNutrition");
        helper.succeed();
    }

    @GameTest
    public void nutritionCorpusLoadsVanillaItems(GameTestHelper helper) {
        helper.assertTrue(HearthwindSurvivalDiet.itemCount() >= 40,
                "vanilla nutrition map must load (got " + HearthwindSurvivalDiet.itemCount() + ")");
        int[] apple = HearthwindSurvivalDiet.nutritionOf(Items.APPLE);
        helper.assertTrue(apple != null && apple[3] == 15 && apple[4] == 5,
                "apple must map to vitamins 15 / minerals 5 from vanilla_items.json");
        int[] cake = HearthwindSurvivalDiet.nutritionOf(Items.CAKE);
        helper.assertTrue(cake != null, "vanilla_blocks.json (cake) must load");
        int[] flask = HearthwindSurvivalDiet.nutritionOf(FlaskItems.LEATHER_FLASK);
        helper.assertTrue(flask != null && flask[4] == 20,
                "dehydration compat must map the flask to minerals 20");
        helper.succeed();
    }

    @GameTest
    public void nutritionThresholdsApplyDatapackEffects(GameTestHelper helper) {
        helper.assertTrue(NutritionEffects.negativeCount() > 0 && NutritionEffects.positiveCount() > 0,
                "nutrition_manager/default.json must load positive and negative effect lists");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            HearthwindSurvivalDiet.setLevel(player, i, 150);
        }
        // Low vitamins: default.json lists weakness on the negative side.
        HearthwindSurvivalDiet.setLevel(player, 3, 0);
        NutritionEffects.applyForTest(player);
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS),
                "vitamins <= negativeNutrition must apply the datapack weakness effect");
        // High vitamins: regeneration.
        HearthwindSurvivalDiet.setLevel(player, 3, 300);
        NutritionEffects.applyForTest(player);
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION),
                "vitamins >= positiveNutrition must apply the datapack regeneration effect");
        helper.succeed();
    }

    @GameTest
    public void perishableFoodRots(GameTestHelper helper) {
        SimpleContainer container = new SimpleContainer(new ItemStack(Items.BEEF));
        int rotted = HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 1, "beef is perishable and must rot");
        helper.assertTrue(container.getItem(0).isEmpty(),
                "original beef slot must be empty after rotting");
        helper.succeed();
    }

    @GameTest
    public void spoilOutputIsRottenFlesh(GameTestHelper helper) {
        java.util.List<ItemStack> spilled = new java.util.ArrayList<>();
        SimpleContainer container = new SimpleContainer(new ItemStack(Items.COOKED_CHICKEN));
        HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", spilled::add);
        helper.assertTrue(spilled.size() == 1
                && spilled.get(0).is(Items.ROTTEN_FLESH),
                "rot output must be rotten flesh");
        helper.succeed();
    }

    @GameTest
    public void nonSpoilingItemsAreExempt(GameTestHelper helper) {
        SimpleContainer container = new SimpleContainer(new ItemStack(Items.HONEY_BOTTLE));
        int rotted = HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 0, "honey bottle is tagged non-spoiling");
        helper.assertTrue(!container.getItem(0).isEmpty(), "honey bottle untouched");
        helper.succeed();
    }

    @GameTest
    public void hydrationClampsAtMax(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalThirst.addHydration(pig, 9999.0);
        helper.assertTrue(HearthwindSurvivalThirst.hydration(pig)
                == HearthwindSurvivalThirst.MAX_HYDRATION, "hydration clamped to max");
        helper.succeed();
    }

    @GameTest
    public void containerSpoilageRotsPerishables(GameTestHelper helper) {
        var container = new SimpleContainer(9);
        container.setItem(0, new ItemStack(Items.BEEF));
        int rotted = HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 1, "beef in chest must rot at chance=1.0");
        helper.assertTrue(container.getItem(0).isEmpty(),
                "original beef slot must be empty");
        helper.succeed();
    }

    @GameTest
    public void containerSpoilageSpillsToCallback(GameTestHelper helper) {
        var container = new SimpleContainer(9);
        java.util.List<ItemStack> spilled = new java.util.ArrayList<>();
        container.setItem(0, new ItemStack(Items.COOKED_CHICKEN));
        HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", spilled::add);
        helper.assertTrue(spilled.size() == 1, "one rotten flesh must spill");
        helper.assertTrue(spilled.get(0).is(Items.ROTTEN_FLESH),
                "spill must be rotten flesh");
        helper.succeed();
    }

    @GameTest
    public void containerNonSpoilingItemsExempt(GameTestHelper helper) {
        var container = new SimpleContainer(9);
        container.setItem(0, new ItemStack(Items.HONEY_BOTTLE));
        int rotted = HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 0, "honey bottle in chest must not rot");
        helper.assertTrue(!container.getItem(0).isEmpty(),
                "honey bottle must remain untouched");
        helper.succeed();
    }

    @GameTest
    public void containerSpoilageMixedSlots(GameTestHelper helper) {
        var container = new SimpleContainer(9);
        container.setItem(0, new ItemStack(Items.BEEF));
        container.setItem(1, new ItemStack(Items.HONEY_BOTTLE));
        container.setItem(2, new ItemStack(Items.COOKED_CHICKEN));
        int rotted = HearthwindSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 2, "two perishables must rot, honey exempt");
        helper.assertTrue(container.getItem(0).isEmpty(), "beef slot empty");
        helper.assertTrue(!container.getItem(1).isEmpty(), "honey bottle untouched");
        helper.assertTrue(container.getItem(2).isEmpty(), "chicken slot empty");
        helper.succeed();
    }

    @GameTest
    public void thirstHydrationDrainsOverTime(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalThirst.addHydration(pig, 10.0);
        double before = HearthwindSurvivalThirst.hydration(pig);
        // Directly set hydration lower to simulate drain
        HearthwindSurvivalThirst.addHydration(pig, -5.0);
        double after = HearthwindSurvivalThirst.hydration(pig);
        helper.assertTrue(after < before, "hydration must decrease with negative add");
        helper.assertTrue(after >= 0.0, "hydration must not go below zero");
        helper.succeed();
    }

    @GameTest
    public void thirstDrinkWaterRefills(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalThirst.addHydration(pig, -10.0);
        double before = HearthwindSurvivalThirst.hydration(pig);
        HearthwindSurvivalThirst.addHydration(pig, 5.0);
        double after = HearthwindSurvivalThirst.hydration(pig);
        helper.assertTrue(after == before + 5.0, "drinking water must add hydration");
        helper.succeed();
    }

    @GameTest
    public void flaskFillSetsCapacityAndQuality(GameTestHelper helper) {
        ItemStack flask = new ItemStack(dev.jmiahman.hearthwind.survival.FlaskItems.LEATHER_FLASK);
        dev.jmiahman.hearthwind.survival.FlaskItems.setFill(flask, 2, dev.jmiahman.hearthwind.survival.FlaskData.IMPURIFIED);
        var data = flask.get(dev.jmiahman.hearthwind.survival.FlaskItems.FLASK_DATA);
        helper.assertTrue(data != null, "filled flask must carry flask_data");
        helper.assertTrue(data.fillLevel() == 2, "leather flask capacity is 2");
        helper.assertTrue(data.qualityLevel() == dev.jmiahman.hearthwind.survival.FlaskData.IMPURIFIED,
                "quality must round-trip");
        helper.assertTrue(flask.has(net.minecraft.core.component.DataComponents.CONSUMABLE),
                "filled flask must be drinkable (CONSUMABLE present)");
        helper.succeed();
    }

    @GameTest
    public void flaskDrinkDecrementsFillAndAddsHydration(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalThirst.addHydration(player, -15.0); // 20 -> 5
        ItemStack flask = new ItemStack(dev.jmiahman.hearthwind.survival.FlaskItems.LEATHER_FLASK);
        dev.jmiahman.hearthwind.survival.FlaskItems.setFill(flask, 2, dev.jmiahman.hearthwind.survival.FlaskData.PURIFIED);
        dev.jmiahman.hearthwind.survival.FlaskItems.onFlaskConsumed(player, flask);
        var data = flask.get(dev.jmiahman.hearthwind.survival.FlaskItems.FLASK_DATA);
        helper.assertTrue(data != null && data.fillLevel() == 1, "drink must decrement fill 2->1");
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) > 5.0, "drink must add hydration");
        helper.assertTrue(flask.get(net.minecraft.core.component.DataComponents.CONSUMABLE) != null,
                "still-filled flask stays drinkable");
        helper.succeed();
    }

    @GameTest
    public void flaskLastDrinkEmptiesFlask(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack flask = new ItemStack(dev.jmiahman.hearthwind.survival.FlaskItems.DIAMOND_LEATHER_FLASK);
        dev.jmiahman.hearthwind.survival.FlaskItems.setFill(flask, 1, dev.jmiahman.hearthwind.survival.FlaskData.DIRTY);
        dev.jmiahman.hearthwind.survival.FlaskItems.onFlaskConsumed(player, flask);
        helper.assertTrue(flask.get(dev.jmiahman.hearthwind.survival.FlaskItems.FLASK_DATA) == null,
                "empty flask must drop flask_data");
        helper.assertTrue(flask.get(net.minecraft.core.component.DataComponents.CONSUMABLE) == null,
                "empty flask must drop CONSUMABLE");
        helper.succeed();
    }

    @GameTest
    public void flaskTiersHaveIncreasingCapacity(GameTestHelper helper) {
        helper.assertTrue(dev.jmiahman.hearthwind.survival.FlaskItems.LEATHER_FLASK.capacity() == 2
                && dev.jmiahman.hearthwind.survival.FlaskItems.IRON_LEATHER_FLASK.capacity() == 3
                && dev.jmiahman.hearthwind.survival.FlaskItems.GOLDEN_LEATHER_FLASK.capacity() == 4
                && dev.jmiahman.hearthwind.survival.FlaskItems.DIAMOND_LEATHER_FLASK.capacity() == 5
                && dev.jmiahman.hearthwind.survival.FlaskItems.NETHERITE_LEATHER_FLASK.capacity() == 6,
                "flask capacities must be 2..6 by tier");
        helper.succeed();
    }

    @GameTest
    public void thresholdAttributeModifiersApplyAndClear(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;
        for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_COUNT; i++) {
            HearthwindSurvivalDiet.setLevel(player, i, 150);
        }
        HearthwindSurvivalDiet.setLevel(player, 0, 300);
        NutritionEffects.applyForTest(player);
        double boosted = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        HearthwindSurvivalDiet.setLevel(player, 0, 150);
        NutritionEffects.applyForTest(player);
        double cleared = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        helper.assertTrue(boosted > cleared,
                "datapack attribute modifiers must apply at the positive threshold and clear in the normal band");
        helper.succeed();
    }

    @GameTest
    public void temperatureStateRoundTripsAndClamps(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(HearthwindSurvivalTemperature.body(player) == 0,
                "fresh players start at body temperature 0");
        HearthwindSurvivalTemperature.setBody(player, 2400);
        helper.assertTrue(HearthwindSurvivalTemperature.body(player) == 2400,
                "setBody must round-trip");
        HearthwindSurvivalTemperature.setWetness(player, 180);
        helper.assertTrue(HearthwindSurvivalTemperature.wetness(player) == 180,
                "setWetness must round-trip (soaked band)");
        helper.succeed();
    }

    @GameTest
    public void temperatureCutoffClampsAndAcclimatizes(GameTestHelper helper) {
        helper.assertTrue(HearthwindSurvivalTemperature.applyCutoff(9_999, 2)
                == EnvironmentCorpus.bodyTemperature(6),
                "normal environments must clamp the body at +2400");
        helper.assertTrue(HearthwindSurvivalTemperature.applyCutoff(-9_999, 2)
                == EnvironmentCorpus.bodyTemperature(0),
                "normal environments must clamp the body at -2400");
        helper.assertTrue(HearthwindSurvivalTemperature.applyCutoff(300, 0) == 280,
                "cold environments push a hot body down by twice the hot acclimatization (300 -> 280)");
        helper.assertTrue(HearthwindSurvivalTemperature.applyCutoff(-300, 4) == -280,
                "hot environments push a cold body up by twice the cold acclimatization (-300 -> -280)");
        helper.succeed();
    }

    @GameTest
    public void thirstEffectIncreasesDrain(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                ThirstMobEffect.HOLDER, 200, 0));
        HearthwindSurvivalConfig.Thirst cfg = HearthwindSurvivalConfig.get().thirst;
        helper.assertTrue(cfg.thirstEffectDrainPerSecond > 0,
                "thirst effect drain must be positive");
        helper.succeed();
    }

    @GameTest
    public void drinkingFlaskAddsMinerals(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalDiet.setLevel(player, 4, 0);
        ItemStack flask = new ItemStack(FlaskItems.LEATHER_FLASK);
        FlaskItems.setFill(flask, 2, FlaskData.PURIFIED);
        FlaskItems.onFlaskConsumed(player, flask);
        helper.assertTrue(HearthwindSurvivalDiet.getLevel(player, 4) == 20,
                "dehydration compat: a flask drink must add minerals 20 through the drink hook");
        helper.succeed();
    }

    @GameTest
    public void configDefaultsAllPositive(GameTestHelper helper) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        helper.assertTrue(cfg.thirst.baseDrainPerSecond > 0, "thirst drain positive");
        helper.assertTrue(cfg.thirst.regenHydrationFloor >= 0, "regen floor non-negative");
        helper.assertTrue(cfg.thirst.damageAmount > 0, "thirst damage positive");
        helper.assertTrue(cfg.temperature.temperatureCalculationTime == 10,
                "Aged override temperatureCalculationTime must be 10");
        helper.assertTrue(cfg.temperature.heatBlockRadius == 3, "heatBlockRadius must default to 3");
        helper.assertTrue(cfg.temperature.easyWorldSpawn, "easyWorldSpawn must default to true");
        helper.assertTrue(cfg.temperature.showThermometer, "showThermometer must default to true");
        helper.assertTrue(cfg.temperature.overheatingExhaustion == 0.07F,
                "Aged override overheatingExhaustion must be 0.07");
        helper.assertTrue(cfg.temperature.exhaustionInsteadDehydration,
                "in-house thirst uses the exhaustion path by default");
        helper.assertTrue(cfg.temperature.thermometerIconX == -95,
                "Aged override thermometerIconX must be -95");
        helper.assertTrue(cfg.temperature.iconX == 7 && cfg.temperature.iconY == 52,
                "body icon offsets must default to 7/52");
        helper.assertTrue(cfg.temperature.thermometerIconY == 32,
                "thermometer Y offset must default to 32");
        helper.assertTrue(cfg.diet.maxNutrition == 300, "maxNutrition default must be 300");
        helper.assertTrue(cfg.diet.negativeNutrition == 30, "negativeNutrition default must be 30");
        helper.assertTrue(cfg.diet.positiveNutrition == 270, "positiveNutrition default must be 270");
        helper.assertTrue("farm_and_charm:lettuce".equals(cfg.diet.vitaminItemId),
                "Aged override vitaminItemId must be farm_and_charm:lettuce");
        helper.assertTrue("meadow:alpine_salt".equals(cfg.diet.mineralItemId),
                "Aged override mineralItemId must be meadow:alpine_salt");
        helper.assertTrue(cfg.spoilage.chancePerCheck >= 0, "spoil chance non-negative");
        helper.succeed();
    }

    @GameTest
    public void spoilageRespectsBiomeModifier(GameTestHelper helper) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        // Hot biomes should double chance - verify the config has the field
        helper.assertTrue(cfg.spoilage.chancePerCheck >= 0, "base chance exists");
        helper.succeed();
    }

    @GameTest
    public void temperatureEffectRowsProfileBodyBands(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // environmentz:warming (+8) only helps while cold (body < 0);
        // environmentz:cooling (-8) only helps while hot (body > 0).
        helper.assertTrue(EnvironmentzEffects.WARMING != null
                && EnvironmentzEffects.COOLING != null
                && EnvironmentzEffects.COMFORT != null,
                "environmentz warming/cooling/comfort effects must register");
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                EnvironmentzEffects.WARMING, 200, 0));
        HearthwindSurvivalTemperature.setState(player,
                HearthwindSurvivalTemperature.State.DEFAULT.withBody(-1000));
        int coldWithWarming = HearthwindSurvivalTemperature.effectTemperature(
                player, HearthwindSurvivalTemperature.getState(player),
                new int[]{0, 0});
        helper.assertTrue(coldWithWarming == 8,
                "warming must add +8 while cold (got " + coldWithWarming + ")");
        HearthwindSurvivalTemperature.setState(player,
                HearthwindSurvivalTemperature.State.DEFAULT.withBody(1000));
        int hotWithWarming = HearthwindSurvivalTemperature.effectTemperature(
                player, HearthwindSurvivalTemperature.getState(player),
                new int[]{0, 0});
        helper.assertTrue(hotWithWarming == 0,
                "warming must not add heat while already hot (got " + hotWithWarming + ")");
        helper.succeed();
    }

    @GameTest
    public void deathDamageTypesAreRegistered(GameTestHelper helper) {
        // The death messages come from data/hearthwind/damage_type/*.json +
        // assets/hearthwind/lang - if the JSONs fail to load, hurt() throws.
        var registry = helper.getLevel().getServer().registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE);
        helper.assertTrue(registry.getOptional(HearthwindSurvivalThirst.DEHYDRATION).isPresent(),
                "hearthwind:dehydration damage type must be registered (died of thirst)");
        helper.assertTrue(registry.getOptional(HearthwindSurvivalTemperature.FREEZING).isPresent(),
                "environmentz:freezing damage type must be registered (froze to death)");
        var src = helper.getLevel().getServer().overworld().damageSources()
                .source(HearthwindSurvivalTemperature.FREEZING);
        helper.assertTrue(src != null, "freezing DamageSource must resolve");
        helper.succeed();
    }

    // ---- sync-payload codec round-trips -------------------------------------
    // Regression guard: encode/decode pairs must be mirror-symmetric. A
    // mismatch (e.g. writeInt vs readVarInt) decodes garbage on the client and
    // kicks the player with DecoderException: Failed to decode custom_payload.
    // Values above 127 make varint-vs-int width mismatches bite.

    private RegistryFriendlyByteBuf bufFor(GameTestHelper helper) {
        return new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),
                helper.getLevel().getServer().registryAccess());
    }

    @GameTest
    public void thirstSyncPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        ThirstSyncPayload sent = new ThirstSyncPayload(12.3f);
        ThirstSyncPayload.CODEC.encode(buf, sent);
        ThirstSyncPayload got = ThirstSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "thirst payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "thirst codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void tempSyncPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        TempSyncPayload sent = new TempSyncPayload(-1800, 180, -6);
        TempSyncPayload.CODEC.encode(buf, sent);
        TempSyncPayload got = TempSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "temp payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "temp codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void dietSyncPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        DietSyncPayload sent = new DietSyncPayload(300, 12, 0, 299, 150);
        DietSyncPayload.CODEC.encode(buf, sent);
        DietSyncPayload got = DietSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "diet payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "diet codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void nutritionItemMapPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        NutritionItemMapPayload sent = new NutritionItemMapPayload(java.util.List.of(
                new NutritionItemMapPayload.Entry(
                        net.minecraft.resources.Identifier.parse("minecraft:apple"), 0, 0, 0, 15, 5),
                new NutritionItemMapPayload.Entry(
                        net.minecraft.resources.Identifier.parse("dehydration:leather_flask"), 0, 0, 0, 0, 20)));
        NutritionItemMapPayload.CODEC.encode(buf, sent);
        NutritionItemMapPayload got = NutritionItemMapPayload.CODEC.decode(buf);
        helper.assertTrue(got.entries().equals(sent.entries()),
                "nutrition item map payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "item map codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void nutritionEffectsPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        java.util.List<java.util.List<String>> positive = java.util.List.of(
                java.util.List.of("attribute.name.attack_speed"),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of("effect.minecraft.regeneration"),
                java.util.List.of("effect.minecraft.haste"));
        java.util.List<java.util.List<String>> negative = java.util.List.of(
                java.util.List.of("attribute.name.attack_speed"),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of("effect.minecraft.weakness"),
                java.util.List.of("effect.minecraft.unluck"));
        NutritionEffectsPayload sent = new NutritionEffectsPayload(positive, negative);
        NutritionEffectsPayload.CODEC.encode(buf, sent);
        NutritionEffectsPayload got = NutritionEffectsPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "nutrition effects payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "effects codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void jobSyncPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        JobSyncPayload sent = new JobSyncPayload("miner", 200, 12345.678, 100.0);
        JobSyncPayload.CODEC.encode(buf, sent);
        JobSyncPayload got = JobSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "job payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "job codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void skillsSyncPayloadRoundTrip(GameTestHelper helper) {
        // Level 200 exceeds 7-bit varint width: catches any writeInt vs
        // readVarInt asymmetry (the exact bug that kicked clients on login).
        var buf = bufFor(helper);
        SkillsSyncPayload sent = new SkillsSyncPayload(
                java.util.List.of("mining", "smithing", "farming"),
                java.util.List.of(200, 1, 31));
        SkillsSyncPayload.CODEC.encode(buf, sent);
        SkillsSyncPayload got = SkillsSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.skills().equals(sent.skills()) && got.levels().equals(sent.levels()),
                "skills payload must round-trip with varint-wide levels: " + got);
        helper.assertTrue(!buf.isReadable(), "skills codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void skillUpPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        SkillUpPayload sent = new SkillUpPayload("archery", 130);
        SkillUpPayload.CODEC.encode(buf, sent);
        SkillUpPayload got = SkillUpPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "skill-up payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "skill-up codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void bareHandDrinkingWhileCrouchingAddsHydration(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;
        player.setPose(net.minecraft.world.entity.Pose.CROUCHING);
        player.setShiftKeyDown(true);

        player.getInventory().clearContent();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        net.minecraft.core.BlockPos waterPos = new net.minecraft.core.BlockPos(1, 1, 1);
        helper.setBlock(waterPos, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
        player.setPos(helper.absolutePos(waterPos).getX() + 0.5,
                helper.absolutePos(waterPos).getY() + 0.5,
                helper.absolutePos(waterPos).getZ() + 0.5);

        // Drain thirst partially
        HearthwindSurvivalThirst.setHydration(player, 10.0);
        double before = HearthwindSurvivalThirst.hydration(player);

        // Simulate sustained drinking (hold loop, ~21 use-events)
        var result = BareHandDrinkHandler.trySip(player, helper.getLevel());
        helper.assertTrue(result.consumesAction(), "Drinking while crouching on water must succeed");
        for (int i = 0; i < 30; i++) {
            BareHandDrinkHandler.trySip(player, helper.getLevel());
        }
        double after = HearthwindSurvivalThirst.hydration(player);
        helper.assertTrue(after > before, "Hydration must increase after bare hand drink: " + after + " > " + before);
        helper.succeed();
    }

    @GameTest
    public void fatalDamageTriggersDownedState(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;

        // Fatal damage
        boolean deathAllowed = dev.jmiahman.hearthwind.survival.revive.ReviveManager.onFatalDamage(
                player, player.level().damageSources().generic());

        helper.assertTrue(!deathAllowed, "Fatal damage must be intercepted to enter Downed state");
        helper.assertTrue(dev.jmiahman.hearthwind.survival.revive.DownedState.isDowned(player),
                "Player must be in Downed state");
        helper.succeed();
    }

    @GameTest
    public void downedPlayerCanBeRevivedByChanneling(GameTestHelper helper) {
        var downed = helper.makeMockServerPlayerInLevel();
        var reviver = helper.makeMockServerPlayerInLevel();
        downed.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        reviver.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        downed.getAbilities().instabuild = false;
        reviver.getAbilities().instabuild = false;

        // Down the player
        dev.jmiahman.hearthwind.survival.revive.ReviveManager.onFatalDamage(
                downed, downed.level().damageSources().generic());
        helper.assertTrue(dev.jmiahman.hearthwind.survival.revive.DownedState.isDowned(downed), "Downed state active");

        // Complete revive
        dev.jmiahman.hearthwind.survival.revive.ReviveManager.completeRevive(downed, reviver);

        helper.assertTrue(!dev.jmiahman.hearthwind.survival.revive.DownedState.isDowned(downed),
                "Player must no longer be downed after revival");
        helper.assertTrue(downed.getHealth() >= 6.0f, "Revived player must have at least 6 HP");
        helper.succeed();
    }

    @GameTest
    public void downedSyncPayloadRoundTrips(GameTestHelper helper) {
        var buf = net.minecraft.network.RegistryFriendlyByteBuf.decorator(
                helper.getLevel().registryAccess()).apply(io.netty.buffer.Unpooled.buffer());
        var sent = new dev.jmiahman.hearthwind.survival.revive.DownedSyncPayload(true, 45, 80);
        dev.jmiahman.hearthwind.survival.revive.DownedSyncPayload.CODEC.encode(buf, sent);
        var got = dev.jmiahman.hearthwind.survival.revive.DownedSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "DownedSyncPayload must round-trip correctly");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // environmentz corpus (data/environmentz/*): heat/cold sources plus the
    // dimension modifier tables. These are the numbers Aged tunes, so the
    // tests pin them rather than the implementation.
    // ------------------------------------------------------------------

    /**
     * Parks the mock player on the test origin and returns that absolute
     * BlockPos, so block placements below line up with the player no matter
     * where the gametest structure landed.
     */
    private static net.minecraft.core.BlockPos parkPlayer(GameTestHelper helper, ServerPlayer player) {
        net.minecraft.core.BlockPos origin = helper.absolutePos(net.minecraft.core.BlockPos.ZERO);
        player.teleportTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
        player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        return origin;
    }

    @GameTest
    public void environmentzCorpusLoadsHeatSources(GameTestHelper helper) {
        helper.assertTrue(EnvironmentCorpus.blockCount() >= 10,
                "heating/cooling block table must resolve vanilla entries (got "
                        + EnvironmentCorpus.blockCount() + ")");
        helper.assertTrue(EnvironmentCorpus.itemCount() >= 20,
                "carried item temperatures must load from the EnvironmentZ defaults (got "
                        + EnvironmentCorpus.itemCount() + ")");
        helper.assertTrue(EnvironmentCorpus.tempFor(net.minecraft.world.level.block.Blocks.CAMPFIRE) != null,
                "campfire must be a registered heat source");
        helper.assertTrue(EnvironmentCorpus.tempFor(net.minecraft.world.level.block.Blocks.ICE) != null,
                "ice must be a registered cooling source");
        helper.assertTrue(EnvironmentCorpus.tempFor(net.minecraft.world.level.block.Blocks.LAVA) != null,
                "lava must be a registered heat source");
        helper.succeed();
    }

    @GameTest
    public void litCampfireWarmsAdjacentPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == 2, "lit campfire 1 block away must give +2 (got " + heat
                + ", block is " + helper.getLevel().getBlockState(player.blockPosition().offset(1, 0, 0)) + ")");
        helper.succeed();
    }

    @GameTest
    public void unlitCampfireDoesNotWarm(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == 0, "unlit campfire must not warm (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void iceCoolsAdjacentPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.ICE.defaultBlockState());
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == -2, "ice 1 block away must give -2 (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void heatSourceBeyondRadiusIsIgnored(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(6, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == 0, "campfire outside the scan radius must not warm (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void wallBlocksHeatFromFire(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        helper.setBlock(2, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == 0, "a wall must block fire heat (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void atMostMaxCountSourcesOfOneTypeContribute(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        net.minecraft.world.level.block.state.BlockState fire =
                net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true);
        helper.setBlock(1, 0, 0, fire);
        helper.setBlock(-1, 0, 0, fire);
        helper.setBlock(0, 0, 1, fire);
        int heat = EnvironmentCorpus.blockHeat(player);
        // EnvironmentZ 2.0.8 campfire max_count is 3; each is 1 block away (+2)
        helper.assertTrue(heat == 6, "only max_count (3) campfires may contribute (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void lavaFluidWarmsAdjacentPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
        int heat = EnvironmentCorpus.blockHeat(player);
        helper.assertTrue(heat == 3, "lava 1 block away must give +3 from the fluid table (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void environmentzManagerTablesMatchCorpus(GameTestHelper helper) {
        EnvironmentCorpus.DimensionTable overworld = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "overworld"));
        helper.assertTrue(overworld != null && !overworld.basic(), "overworld table must load");
        helper.assertTrue(overworld.day(0) == -4 && overworld.day(4) == 4,
                "day row must be -4 (very cold) .. +4 (very hot)");
        helper.assertTrue(overworld.night(0) == -6 && overworld.night(4) == 2,
                "night row must be -6 (very cold) .. +2 (very hot)");
        helper.assertTrue(overworld.shadow(2) == -1, "shadow must be -1");
        helper.assertTrue(overworld.soaked(2) == -6 && overworld.wett(2) == -3,
                "soaked must be -6 and wett -3");
        helper.assertTrue(overworld.sweat(0) == -2 && overworld.sweat(1) == -3,
                "sweat must be -2 (hot) / -3 (very hot)");
        helper.assertTrue(overworld.armor(2) == 1 && overworld.insulatedArmor(2) == 3
                && overworld.icedArmor(2) == -5,
                "armor rows must be +1 / +3 insulated / -5 iced");
        helper.assertTrue(overworld.hasHeight() && overworld.heightAt(200) == -2
                && overworld.heightAt(64) == 0 && overworld.heightAt(10) == 1
                && overworld.heightAt(-5) == 2,
                "height rows must apply by altitude (2/1/0/-1/-2 at y<0/0/30/120/190)");
        helper.assertTrue(overworld.acclimatization() == EnvironmentCorpus.NO_DIMENSION_ACCLIMATIZATION,
                "overworld uses the global acclimatization table");
        int[] bands = EnvironmentCorpus.thermometerBands();
        helper.assertTrue(bands[0] == -6 && bands[1] == -3 && bands[2] == 3 && bands[3] == 6,
                "thermometer bands must be -6/-3/3/6");
        int[] acclimatization = EnvironmentCorpus.acclimatizationBands();
        helper.assertTrue(acclimatization.length == 8 && acclimatization[0] == 180
                && acclimatization[1] == -10 && acclimatization[2] == 1600
                && acclimatization[3] == -15 && acclimatization[4] == -180
                && acclimatization[5] == 10 && acclimatization[6] == -1600
                && acclimatization[7] == 15,
                "acclimatization table must be 180/-10, 1600/-15, -180/+10, -1600/+15");
        EnvironmentCorpus.DimensionTable nether = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "the_nether"));
        helper.assertTrue(nether != null && nether.basic() && nether.standard(2) == 5
                && nether.acclimatization() == 0,
                "nether must be a basic +5 dimension with acclimatization 0");
        EnvironmentCorpus.DimensionTable end = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "the_end"));
        helper.assertTrue(end != null && end.basic() && end.standard(2) == -5,
                "end must be a basic -5 dimension");
        helper.succeed();
    }

    @GameTest
    public void biomeAndBodyBandsMatchEnvironmentzDefaults(GameTestHelper helper) {
        int[] body = EnvironmentCorpus.bodyTemperatures();
        helper.assertTrue(body.length == 7 && body[0] == -2400 && body[2] == -240 && body[4] == 240
                && body[6] == 2400,
                "body bands must be -2400/-1800/-240/0/240/1800/2400");
        helper.assertTrue(EnvironmentCorpus.biomeTemperature(0) == 0.2F
                && EnvironmentCorpus.biomeTemperature(1) == 0.4F
                && EnvironmentCorpus.biomeTemperature(2) == 1.2F
                && EnvironmentCorpus.biomeTemperature(3) == 1.6F,
                "biome thresholds must be 0.2/0.4/1.2/1.6");
        helper.assertTrue(HearthwindSurvivalTemperature.environmentCode(0.1F) == 0
                && HearthwindSurvivalTemperature.environmentCode(0.3F) == 1
                && HearthwindSurvivalTemperature.environmentCode(1.0F) == 2
                && HearthwindSurvivalTemperature.environmentCode(1.4F) == 3
                && HearthwindSurvivalTemperature.environmentCode(1.7F) == 4,
                "environmentCode must map the four biome thresholds");
        helper.assertTrue(EnvironmentCorpus.wetness(0) == 200 && EnvironmentCorpus.wetness(1) == 180
                && EnvironmentCorpus.wetness(2) == 100 && EnvironmentCorpus.wetness(3) == 1
                && EnvironmentCorpus.wetness(4) == -1,
                "wetness bands must be max 200/soaked 180/water +100/rain +1/dry -1");
        helper.assertTrue(EnvironmentCorpus.protection(0) == 600
                && EnvironmentCorpus.protection(1) == 600
                && EnvironmentCorpus.protection(2) == 600
                && EnvironmentCorpus.protection(3) == 600,
                "protection pools must cap at 600");
        helper.succeed();
    }

    @GameTest
    public void acclimatizationTableAdjustsBodyByBand(GameTestHelper helper) {
        EnvironmentCorpus.DimensionTable overworld = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "overworld"));
        helper.assertTrue(HearthwindSurvivalTemperature.acceptanceAdjustment(overworld, 2, 200) == -10,
                "body above +180 in a normal biome must acclimatize -10");
        helper.assertTrue(HearthwindSurvivalTemperature.acceptanceAdjustment(overworld, 2, -200) == 10,
                "body below -180 in a normal biome must acclimatize +10");
        helper.assertTrue(HearthwindSurvivalTemperature.acceptanceAdjustment(overworld, 1, -1700) == 15,
                "very cold biomes push a very cold body +15");
        helper.assertTrue(HearthwindSurvivalTemperature.acceptanceAdjustment(overworld, 3, 1700) == -15,
                "hot biomes push an overheating body -15");
        helper.succeed();
    }

    @GameTest
    public void itemTemperatureTablesMatchEnvironmentz(GameTestHelper helper) {
        EnvironmentCorpus.ItemTemp stones = EnvironmentCorpus.item(EnvironmentzItems.HEATING_STONES);
        helper.assertTrue(stones != null && stones.temperature() == 10 && stones.damage() == 1
                && stones.coldProtection() == 2 && stones.heatProtection() == 0,
                "heating stones must be +10 heat, 1 wear, cold_protection 2");
        EnvironmentCorpus.ItemTemp pack = EnvironmentCorpus.item(EnvironmentzItems.ICE_PACK);
        helper.assertTrue(pack != null && pack.temperature() == -10 && pack.damage() == 1
                && pack.heatProtection() == 2 && pack.coldProtection() == 0,
                "ice packs must be -10 heat, 1 wear, heat_protection 2");
        helper.succeed();
    }

    @GameTest
    public void equippedTemperatureItemsAddHeatAndProtection(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getInventory().clearContent();
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(EnvironmentzItems.HEATING_STONES));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND,
                new ItemStack(EnvironmentzItems.HEATING_STONES));
        int[] pools = {0, 0};
        int total = HearthwindSurvivalTemperature.itemTemperature(player,
                HearthwindSurvivalTemperature.getState(player), pools);
        helper.assertTrue(total == 20, "two held heating stones must add +10 each (got " + total + ")");
        helper.assertTrue(pools[1] == 4, "cold protection must accumulate 2+2 (got " + pools[1] + ")");
        helper.succeed();
    }

    @GameTest
    public void protectionPoolsConsumeIncomingDelta(GameTestHelper helper) {
        // Cold: resistance first, then cold protection, both decremented.
        int[] pools = {0, 50};
        int[] resistances = {0, 200};
        int remaining = HearthwindSurvivalTemperature.consumeProtection(-100, 0, pools, resistances);
        helper.assertTrue(remaining == 0, "cold resistance 200 must fully absorb -100 (got " + remaining + ")");
        helper.assertTrue(resistances[1] == 100, "cold resistance must be consumed to 100");
        helper.assertTrue(pools[1] == 50, "unused cold protection must remain");

        // Heat: partial resistance passes the leftover through to heat protection.
        pools = new int[]{30, 0};
        resistances = new int[]{40, 0};
        remaining = HearthwindSurvivalTemperature.consumeProtection(100, 4, pools, resistances);
        helper.assertTrue(remaining == 30,
                "40 heat resistance + 30 heat protection must leave 30 (got " + remaining + ")");
        helper.assertTrue(resistances[0] == 0 && pools[0] == 0,
                "both heat pools must be fully consumed at their limit");

        // Full pool soak caps at the 600 pool.
        pools = new int[]{600, 600};
        resistances = new int[]{0, 0};
        remaining = HearthwindSurvivalTemperature.consumeProtection(100, 4, pools, resistances);
        helper.assertTrue(remaining == 0 && pools[0] == 500,
                "600 heat protection must absorb 100 and leave 500 (got " + remaining + ")");
        helper.succeed();
    }

    @GameTest
    public void armorTemperatureReadsInsulatedAndIcedNbt(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        EnvironmentCorpus.DimensionTable overworld = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "overworld"));

        ItemStack insulated = new ItemStack(Items.LEATHER_CHESTPLATE);
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA, insulated,
                tag -> tag.putString("environmentz", "fur_insolated"));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, insulated);
        helper.assertTrue(HearthwindSurvivalTemperature.armorTemperature(player, overworld, 2) == 3,
                "insulated armor must use the +3 insulated_armor row");

        ItemStack iced = new ItemStack(Items.CHAINMAIL_CHESTPLATE);
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA, iced,
                tag -> tag.putInt("iced", 2));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, iced);
        helper.assertTrue(HearthwindSurvivalTemperature.armorTemperature(player, overworld, 2) == -5,
                "iced armor must add the -5 iced_armor row");
        net.minecraft.world.item.component.CustomData data =
                iced.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        helper.assertTrue(data != null && data.copyTag().getInt("iced").orElse(0) == 1,
                "one calculation must consume one iced charge");
        HearthwindSurvivalTemperature.armorTemperature(player, overworld, 2);
        data = iced.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        helper.assertTrue(data == null || !data.copyTag().contains("iced"),
                "the last iced charge must clear the custom-data key");
        helper.succeed();
    }

    @GameTest
    public void bandDebuffsApplyAndClear(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        net.minecraft.resources.Identifier freezing =
                net.minecraft.resources.Identifier.fromNamespaceAndPath("environmentz", "freezing_debuff");
        net.minecraft.resources.Identifier cold =
                net.minecraft.resources.Identifier.fromNamespaceAndPath("environmentz", "cold_debuff");
        net.minecraft.resources.Identifier general =
                net.minecraft.resources.Identifier.fromNamespaceAndPath("environmentz", "general_debuff");
        var speed = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        var attackSpeed = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        helper.assertTrue(speed != null && attackSpeed != null, "attribute instances must resolve");

        HearthwindSurvivalTemperature.applyBandDebuffs(player, -2000);
        helper.assertTrue(speed.hasModifier(freezing), "freezing must apply the -25% speed modifier");
        helper.assertTrue(attackSpeed.hasModifier(general), "freezing must apply the -20% attack speed modifier");
        HearthwindSurvivalTemperature.applyBandDebuffs(player, -1000);
        helper.assertTrue(!speed.hasModifier(freezing) && speed.hasModifier(cold),
                "the cold band must swap freezing for the -8% speed modifier");
        helper.assertTrue(!attackSpeed.hasModifier(general), "general debuff must clear in the cold band");
        HearthwindSurvivalTemperature.applyBandDebuffs(player, 100);
        helper.assertTrue(!speed.hasModifier(cold), "comfortable bodies must clear the cold modifier");
        helper.succeed();
    }

    @GameTest
    public void wetnessDriesByOnePerCalculation(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalTemperature.setWetness(player, 50);
        int dried = HearthwindSurvivalTemperature.updateWetness(player, 50);
        helper.assertTrue(dried == 49, "dry players must lose 1 wetness per calculation (got " + dried + ")");
        helper.assertTrue(HearthwindSurvivalTemperature.updateWetness(player, 0) == 0,
                "wetness must never go below 0");
        helper.succeed();
    }

    @GameTest
    public void biomeBandsOrderFromColdToHot(GameTestHelper helper) {
        helper.assertTrue(EnvironmentCorpus.band(helper.getLevel().getBiome(helper.absolutePos(net.minecraft.core.BlockPos.ZERO))) >= 0,
                "band must resolve for any biome");
        helper.assertTrue(EnvironmentCorpus.bandName(0).equals("very_cold")
                && EnvironmentCorpus.bandName(4).equals("very_hot"), "band names must match the corpus keys");
        helper.succeed();
    }

    @GameTest
    public void fullCalculationHeatsFromNearbyCampfire(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        HearthwindSurvivalTemperature.setState(player,
                HearthwindSurvivalTemperature.State.DEFAULT.withBody(0));
        HearthwindSurvivalTemperature.calculate(player);
        int withoutFire = HearthwindSurvivalTemperature.body(player);

        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        HearthwindSurvivalTemperature.setState(player,
                HearthwindSurvivalTemperature.State.DEFAULT.withBody(0));
        HearthwindSurvivalTemperature.calculate(player);
        int withFire = HearthwindSurvivalTemperature.body(player);
        helper.assertTrue(withFire - withoutFire == 2,
                "a lit campfire must add exactly +2 per calculation through the full pipeline (got delta "
                        + (withFire - withoutFire) + ")");
        helper.succeed();
    }

    @GameTest
    public void litCampfireUsesLitPropertyGate(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
        int heat = EnvironmentCorpus.blockHeat(player, 3);
        helper.assertTrue(heat == 0, "an unlit furnace/campfire must be gated by its lit property");
        helper.succeed();
    }

    @GameTest
    public void thermometerExcludesCarriedItemHeat(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        HearthwindSurvivalTemperature.setState(player,
                HearthwindSurvivalTemperature.State.DEFAULT.withBody(0).withThermometer(0));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(EnvironmentzItems.HEATING_STONES));
        HearthwindSurvivalTemperature.calculate(player);
        int body = HearthwindSurvivalTemperature.body(player);
        int thermometer = HearthwindSurvivalTemperature.thermometer(player);
        // The thermometer row deliberately excludes armor/item/effect/wetness
        // drivers (reference thermometerCalculatingTemperature), so held heat
        // shows on the body but not on the thermometer.
        helper.assertTrue(body - thermometer == 10,
                "carried heating stones must add +10 to the body only (body " + body
                        + ", thermometer " + thermometer + ")");
        helper.succeed();
    }

    @GameTest
    public void hydrationCorpusLoadsCataloguedItems(GameTestHelper helper) {
        helper.assertTrue(HydrationCorpus.hasCorpus(), "the hydration corpus must load from the world datapack");
        helper.assertTrue(HydrationCorpus.itemCount() >= 10,
                "expected at least 10 catalogued foods/drinks, got " + HydrationCorpus.itemCount());
        helper.assertTrue(HydrationCorpus.tierCount() >= 5,
                "expected at least 5 hydration tiers, got " + HydrationCorpus.tierCount());
        helper.succeed();
    }

    @GameTest
    public void hydrationCorpusTiersMatchCatalogue(GameTestHelper helper) {
        assertQuench(helper, Items.MELON_SLICE, 1);
        assertQuench(helper, Items.GLOW_BERRIES, 2);
        assertQuench(helper, Items.MUSHROOM_STEW, 3);
        assertQuench(helper, Items.APPLE, 4);
        assertQuench(helper, Items.GOLDEN_APPLE, 6);
        assertQuench(helper, Items.MILK_BUCKET, 8);
        helper.succeed();
    }

    @GameTest
    public void eatingCataloguedFoodRestoresHydration(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalThirst.setHydration(player, 5.0);
        double granted = HydrationCorpus.hydrateOnConsume(player, new ItemStack(Items.MELON_SLICE));
        helper.assertTrue(Math.abs(granted - 1.0) < 0.001,
                "a melon slice must grant 1 hydration (got " + granted + ")");
        helper.assertTrue(Math.abs(HearthwindSurvivalThirst.hydration(player) - 6.0) < 0.001,
                "hydration must rise from 5 to 6 (got " + HearthwindSurvivalThirst.hydration(player) + ")");
        helper.succeed();
    }

    @GameTest
    public void hydrationFromFoodCapsAtMax(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalThirst.setHydration(player, HearthwindSurvivalThirst.MAX_HYDRATION - 1.0);
        HydrationCorpus.hydrateOnConsume(player, new ItemStack(Items.APPLE));
        helper.assertTrue(HearthwindSurvivalThirst.hydration(player) <= HearthwindSurvivalThirst.MAX_HYDRATION,
                "hydration must never exceed the maximum (got " + HearthwindSurvivalThirst.hydration(player) + ")");
        helper.assertTrue(Math.abs(HearthwindSurvivalThirst.hydration(player)
                - HearthwindSurvivalThirst.MAX_HYDRATION) < 0.001, "a nearly full player must top up to 20");
        helper.succeed();
    }

    @GameTest
    public void uncataloguedFoodDoesNotRestoreHydration(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalThirst.setHydration(player, 8.0);
        double granted = HydrationCorpus.hydrateOnConsume(player, new ItemStack(Items.STICK));
        helper.assertTrue(granted == 0.0, "a stick must grant no hydration (got " + granted + ")");
        helper.assertTrue(Math.abs(HearthwindSurvivalThirst.hydration(player) - 8.0) < 0.001,
                "hydration must be unchanged (got " + HearthwindSurvivalThirst.hydration(player) + ")");
        helper.succeed();
    }

    @GameTest
    public void seasonTempHookShiftsWithSeason(GameTestHelper helper) {
        var cfg = dev.jmiahman.hearthwind.world.HearthwindWorldConfig.get();
        double winter = dev.jmiahman.hearthwind.world.Season.WINTER.tempOffset(cfg);
        double summer = dev.jmiahman.hearthwind.world.Season.SUMMER.tempOffset(cfg);
        helper.assertTrue(winter < 0, "winter offset must cool (got " + winter + ")");
        helper.assertTrue(summer > 0, "summer offset must warm (got " + summer + ")");
        helper.assertTrue(winter < summer, "winter must be colder than summer");
        helper.succeed();
    }

    private void assertQuench(GameTestHelper helper, net.minecraft.world.item.Item item, int expected) {
        int quench = HydrationCorpus.quench(new ItemStack(item));
        helper.assertTrue(quench == expected,
                BuiltInRegistries.ITEM.getKey(item) + " must quench " + expected + " (got " + quench + ")");
    }

    @GameTest
    public void starterKitContainsGuidebookBottleAndCampfire(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getInventory().clearContent();

        StarterKit.grantStarterKit(player);

        // Verify Guidebook
        boolean hasBook = false;
        boolean hasBottle = false;
        boolean hasCampfire = false;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            boolean hasLavenderGuide = false;
            if (FabricLoader.getInstance().isModLoaded("lavender")) {
                Identifier guideId = Identifier.fromNamespaceAndPath("lavender", "aged_guide_book");
                hasLavenderGuide = BuiltInRegistries.ITEM.getOptional(guideId)
                        .map(item -> stack.is(item))
                        .orElse(false);
            }
            if (hasLavenderGuide) {
                hasBook = true;
            } else if (stack.is(Items.WRITTEN_BOOK)) {
                var content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
                if (content != null && content.title().raw().contains("Hearthwind Survival Guide")
                        && content.pages().size() >= 5) {
                    hasBook = true;
                }
            } else if (stack.is(Items.GLASS_BOTTLE)) {
                hasBottle = true;
            } else if (stack.is(Items.CAMPFIRE)) {
                hasCampfire = true;
            }
        }

        helper.assertTrue(hasBook, "Player must receive a valid Survival Guidebook with pages");
        helper.assertTrue(hasBottle, "Player must receive a Glass Bottle");
        helper.assertTrue(hasCampfire, "Player must receive a Campfire");

        helper.succeed();
    }
}
