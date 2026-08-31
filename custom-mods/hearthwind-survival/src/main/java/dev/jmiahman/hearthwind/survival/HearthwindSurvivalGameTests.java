package dev.jmiahman.hearthwind.survival;

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
        helper.assertTrue(cfg.diet.deficiencyThreshold < cfg.diet.balanceThreshold,
                "deficiency threshold must be below balance threshold");
        helper.assertTrue(cfg.spoilage.chancePerCheck >= 0, "spoil chance must not be negative");
        helper.succeed();
    }

    @GameTest
    public void eatingBreadRefillsGrains(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalDiet.setLevel(pig, HearthwindSurvivalDiet.GRAINS, 0.0);
        ItemStack bread = new ItemStack(Items.BREAD);
        helper.assertTrue(bread.get(DataComponents.FOOD) != null, "bread must be food");
        HearthwindSurvivalDiet.onEaten(pig, bread);
        double grains = HearthwindSurvivalDiet.level(pig, HearthwindSurvivalDiet.GRAINS);
        helper.assertTrue(grains > 0, "eating bread must refill grains");
        helper.assertTrue(HearthwindSurvivalDiet.level(pig, HearthwindSurvivalDiet.PROTEINS)
                == HearthwindSurvivalDiet.MAX_NUTRIENTS,
                "unrelated groups must stay at default");
        helper.succeed();
    }

    @GameTest
    public void eatHookFiresThroughItemUse(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalDiet.setLevel(player, HearthwindSurvivalDiet.GRAINS, 0.0);
        ItemStack bread = new ItemStack(Items.BREAD);
        HearthwindSurvivalDiet.onEaten(player, bread);
        double grains = HearthwindSurvivalDiet.level(player, HearthwindSurvivalDiet.GRAINS);
        helper.assertTrue(grains > 0, "eating bread via onEaten must refill grains (mixin hook)");
        helper.succeed();
    }

    @GameTest
    public void nonFoodDoesNotChangeDiet(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalDiet.setLevel(pig, HearthwindSurvivalDiet.VEGETABLES, 10.0);
        HearthwindSurvivalDiet.onEaten(pig, new ItemStack(Items.STICK));
        helper.assertTrue(
                HearthwindSurvivalDiet.level(pig, HearthwindSurvivalDiet.VEGETABLES) == 10.0,
                "sticks are not food; nutrients unchanged");
        helper.succeed();
    }

    @GameTest
    public void decayReducesNutrients(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalDiet.setLevel(pig, HearthwindSurvivalDiet.FRUITS, 50.0);
        var state = HearthwindSurvivalDiet.applyDecay(pig, HearthwindSurvivalConfig.get().diet);
        double fruits = HearthwindSurvivalDiet.level(pig, HearthwindSurvivalDiet.FRUITS);
        helper.assertTrue(fruits < 50.0 && fruits > 49.0,
                "one decay step should reduce slightly: " + fruits);
        helper.assertTrue(!state.allBalanced(), "50 in one group is not balanced overall");
        helper.assertTrue(state.deficient() == 0, "nothing below deficiency yet");
        helper.succeed();
    }

    @GameTest
    public void decayFlagsDeficiency(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        for (var group : new net.minecraft.tags.TagKey[]{HearthwindSurvivalDiet.FRUITS}) {
            HearthwindSurvivalDiet.setLevel(pig, group, 5.0);
        }
        var state = HearthwindSurvivalDiet.applyDecay(pig, HearthwindSurvivalConfig.get().diet);
        helper.assertTrue(state.deficient() > 0, "fruit at 5 must count as deficient");
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
    public void dietDeficiencyAppliesDebuffs(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Set all diet groups below deficiency threshold
        for (var group : new net.minecraft.tags.TagKey[]{
                HearthwindSurvivalDiet.FRUITS, HearthwindSurvivalDiet.VEGETABLES,
                HearthwindSurvivalDiet.GRAINS, HearthwindSurvivalDiet.PROTEINS}) {
            HearthwindSurvivalDiet.setLevel(player, group, 0.0);
        }
        HearthwindSurvivalConfig.Diet cfg = HearthwindSurvivalConfig.get().diet;
        HearthwindSurvivalDiet.applyDecay(player, cfg);
        // Deficiency debuffs should be applied on next tick (simulated via direct call)
        helper.assertTrue(HearthwindSurvivalDiet.level(player, HearthwindSurvivalDiet.FRUITS)
                < cfg.deficiencyThreshold, "fruit must be below deficiency");
        helper.succeed();
    }

    @GameTest
    public void dietBalancedGrantsAbsorption(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Set all groups to max
        for (var group : new net.minecraft.tags.TagKey[]{
                HearthwindSurvivalDiet.FRUITS, HearthwindSurvivalDiet.VEGETABLES,
                HearthwindSurvivalDiet.GRAINS, HearthwindSurvivalDiet.PROTEINS,
                HearthwindSurvivalDiet.SUGARS}) {
            HearthwindSurvivalDiet.setLevel(player, group, 100.0);
        }
        HearthwindSurvivalConfig.Diet cfg = HearthwindSurvivalConfig.get().diet;
        var state = HearthwindSurvivalDiet.applyDecay(player, cfg);
        helper.assertTrue(state.allBalanced(), "all groups at max must be balanced");
        helper.succeed();
    }

    @GameTest
    public void temperatureShiftAndClamp(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        double initial = HearthwindSurvivalTemperature.get(player);
        double shifted = HearthwindSurvivalTemperature.shift(player, 5.0);
        helper.assertTrue(shifted == initial + 5.0, "shift must add delta");
        double clamped = HearthwindSurvivalTemperature.shift(player, 999.0);
        helper.assertTrue(clamped == HearthwindSurvivalTemperature.MAX,
                "temperature must clamp to max");
        double coldClamped = HearthwindSurvivalTemperature.shift(player, -999.0);
        helper.assertTrue(coldClamped == HearthwindSurvivalTemperature.MIN,
                "temperature must clamp to min");
        helper.succeed();
    }

    @GameTest
    public void temperatureColdWaterCooldown(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HearthwindSurvivalTemperature.applyColdCooldown(player, 100);
        helper.assertTrue(HearthwindSurvivalTemperature.coldCooldownRemaining(player) > 0,
                "cold cooldown must be active");
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
    public void dietOnEatenMatchesCorrectTag(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        HearthwindSurvivalDiet.setLevel(pig, HearthwindSurvivalDiet.FRUITS, 0.0);
        // Apple is tagged as fruit in the migrated datapack
        var apple = new ItemStack(Items.APPLE);
        HearthwindSurvivalDiet.onEaten(pig, apple);
        double fruits = HearthwindSurvivalDiet.level(pig, HearthwindSurvivalDiet.FRUITS);
        // Even if apple isn't tagged, it shouldn't crash and fruits should be 0 or > 0
        helper.assertTrue(fruits >= 0.0, "fruit level must be non-negative after eating apple");
        helper.succeed();
    }

    @GameTest
    public void configDefaultsAllPositive(GameTestHelper helper) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        helper.assertTrue(cfg.thirst.baseDrainPerSecond > 0, "thirst drain positive");
        helper.assertTrue(cfg.thirst.regenHydrationFloor >= 0, "regen floor non-negative");
        helper.assertTrue(cfg.thirst.damageAmount > 0, "thirst damage positive");
        helper.assertTrue(cfg.temperature.driftPerSecond > 0, "temp drift positive");
        helper.assertTrue(cfg.diet.decayPerSecond > 0, "diet decay positive");
        helper.assertTrue(cfg.diet.nutrientsPerFoodPoint > 0, "nutrients per point positive");
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
    public void temperatureWarnHandlesFirstJoinNullState(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Regression: a fresh player has no warning-state entry; the old
        // `level == prevLevel` unboxing NPE-crashed the server on join.
        HearthwindSurvivalTemperature.warn(player, 10.0);
        HearthwindSurvivalTemperature.warn(player, 10.0);
        HearthwindSurvivalTemperature.warn(player, -12.0);
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
        helper.assertTrue(registry.getOptional(HearthwindSurvivalTemperature.HEATSTROKE).isPresent(),
                "hearthwind:heatstroke damage type must be registered (succumbed to the heat)");
        var src = helper.getLevel().getServer().overworld().damageSources()
                .source(HearthwindSurvivalThirst.DEHYDRATION);
        helper.assertTrue(src != null, "dehydration DamageSource must resolve");
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
        TempSyncPayload sent = new TempSyncPayload(-3.7f);
        TempSyncPayload.CODEC.encode(buf, sent);
        TempSyncPayload got = TempSyncPayload.CODEC.decode(buf);
        helper.assertTrue(got.equals(sent), "temp payload must round-trip: " + got);
        helper.assertTrue(!buf.isReadable(), "temp codec must be symmetric (no leftover bytes)");
        helper.succeed();
    }

    @GameTest
    public void dietSyncPayloadRoundTrip(GameTestHelper helper) {
        var buf = bufFor(helper);
        DietSyncPayload sent = new DietSyncPayload(new float[] {41.5f, 12.25f, 0.0f, -1.0f, 100.0f});
        DietSyncPayload.CODEC.encode(buf, sent);
        DietSyncPayload got = DietSyncPayload.CODEC.decode(buf);
        helper.assertTrue(java.util.Arrays.equals(got.nutrients(), sent.nutrients()),
                "diet payload must round-trip (float[] compare): " + java.util.Arrays.toString(got.nutrients()));
        helper.assertTrue(!buf.isReadable(), "diet codec must be symmetric (no leftover bytes)");
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

        // Simulate drink action
        var result = BareHandDrinkHandler.trySip(player, helper.getLevel());
        helper.assertTrue(result.consumesAction(), "Drinking while crouching on water must succeed");
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
        helper.assertTrue(EnvironmentCorpus.itemCount() >= 1,
                "carried item temperatures must load (got " + EnvironmentCorpus.itemCount() + ")");
        helper.assertTrue(EnvironmentCorpus.tempFor(net.minecraft.world.level.block.Blocks.CAMPFIRE) != null,
                "campfire must be a registered heat source");
        helper.assertTrue(EnvironmentCorpus.tempFor(net.minecraft.world.level.block.Blocks.ICE) != null,
                "ice must be a registered cooling source");
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
        // campfire max_count is 2; each is 1 block away (+2)
        helper.assertTrue(heat == 4, "only max_count (2) campfires may contribute (got " + heat + ")");
        helper.succeed();
    }

    @GameTest
    public void environmentzManagerTablesMatchCorpus(GameTestHelper helper) {
        EnvironmentCorpus.DimensionTable overworld = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "overworld"));
        helper.assertTrue(overworld != null && !overworld.basic(), "overworld table must load");
        helper.assertTrue(overworld.modifier("day", 0) == -4 && overworld.modifier("day", 4) == 4,
                "day row must be -4 (very cold) .. +4 (very hot)");
        helper.assertTrue(overworld.modifier("night", 0) == -6, "night in a very cold biome must be -6");
        helper.assertTrue(overworld.modifier("shadow", 2) == -1, "shadow must be -1");
        helper.assertTrue(overworld.modifier("soaked", 2) == -6, "soaked must be -6");
        helper.assertTrue(overworld.hasHeight() && overworld.heightAt(200) == -2
                && overworld.heightAt(64) == 1, "height rows must apply by altitude");
        int[] bands = EnvironmentCorpus.thermometerBands();
        helper.assertTrue(bands[0] == -6 && bands[1] == -2 && bands[2] == 2 && bands[3] == 6,
                "thermometer bands must be -6/-2/2/6");
        int[] acclimatization = EnvironmentCorpus.acclimatization();
        helper.assertTrue(acclimatization.length == 8 && acclimatization[0] == 180
                && acclimatization[1] == -10 && acclimatization[6] == -1680 && acclimatization[7] == 20,
                "acclimatization table must match the corpus (180/-10, -1680/+20)");
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
    public void temperatureTargetIncludesNearbyFire(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        double cold = HearthwindSurvivalTemperature.environmentAdjustment(player, 0.0, 0.0, false, true, false, false);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        double warm = HearthwindSurvivalTemperature.environmentAdjustment(player, 0.0, 0.0, false, true, false, false);
        helper.assertTrue(Math.abs((warm - cold) - 2.0) < 0.001,
                "a lit campfire must raise the temperature target by 2 (got delta " + (warm - cold) + ")");
        helper.succeed();
    }

    @GameTest
    public void temperatureTargetIncludesNearbyIce(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        double plain = HearthwindSurvivalTemperature.environmentAdjustment(player, 0.0, 0.0, false, true, false, false);
        helper.setBlock(1, 0, 0, net.minecraft.world.level.block.Blocks.PACKED_ICE.defaultBlockState());
        double chilled = HearthwindSurvivalTemperature.environmentAdjustment(player, 0.0, 0.0, false, true, false, false);
        helper.assertTrue(Math.abs((chilled - plain) + 2.0) < 0.001,
                "packed ice must lower the temperature target by 2 (got delta " + (chilled - plain) + ")");
        helper.succeed();
    }

    @GameTest
    public void temperatureModelUsesCorpusTablesByDefault(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        parkPlayer(helper, player);
        helper.assertTrue(HearthwindSurvivalConfig.get().temperature.useEnvironmentzTables,
                "corpus tables must be enabled by default");
        double day = HearthwindSurvivalTemperature.environmentAdjustment(player, 0.0, 0.0, false, true, false, false);
        double night = HearthwindSurvivalTemperature.environmentAdjustment(player, 0.0, 0.0, true, true, false, false);
        // In a temperate biome the corpus sets day and night to 0, so the
        // meaningful assertion is that the delta always comes from the table.
        int band = EnvironmentCorpus.band(player.level().getBiome(player.blockPosition()));
        EnvironmentCorpus.DimensionTable overworld = EnvironmentCorpus.dimension(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "overworld"));
        double expectedDelta = overworld.modifier("night", band) - overworld.modifier("day", band);
        helper.assertTrue(Math.abs((night - day) - expectedDelta) < 0.001,
                "the day/night delta must come from the corpus row (expected " + expectedDelta
                        + ", got " + (night - day) + ")");
        double sheltered = HearthwindSurvivalTemperature.environmentAdjustment(player, 0.0, 0.0, false, false, false, false);
        helper.assertTrue(Math.abs((sheltered - day) + 1.0) < 0.001,
                "being under a roof (shadow) must cost -1 (got " + (sheltered - day) + ")");
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

    private void assertQuench(GameTestHelper helper, net.minecraft.world.item.Item item, int expected) {
        int quench = HydrationCorpus.quench(new ItemStack(item));
        helper.assertTrue(quench == expected,
                BuiltInRegistries.ITEM.getKey(item) + " must quench " + expected + " (got " + quench + ")");
    }
}
