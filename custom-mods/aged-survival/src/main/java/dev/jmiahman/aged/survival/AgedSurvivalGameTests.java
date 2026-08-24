package dev.jmiahman.aged.survival;

import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
public final class AgedSurvivalGameTests {
    /** Public ctor: fabric-loader instantiates gametest entrypoints reflectively. */
    public AgedSurvivalGameTests() {}

    @GameTest
    public void configLoadsSaneDefaults(GameTestHelper helper) {
        AgedSurvivalConfig cfg = AgedSurvivalConfig.get();
        helper.assertTrue(cfg.thirst.baseDrainPerSecond > 0, "thirst drain must be positive");
        helper.assertTrue(cfg.diet.deficiencyThreshold < cfg.diet.balanceThreshold,
                "deficiency threshold must be below balance threshold");
        helper.assertTrue(cfg.spoilage.chancePerCheck >= 0, "spoil chance must not be negative");
        helper.succeed();
    }

    @GameTest
    public void eatingBreadRefillsGrains(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        AgedSurvivalDiet.setLevel(pig, AgedSurvivalDiet.GRAINS, 0.0);
        ItemStack bread = new ItemStack(Items.BREAD);
        helper.assertTrue(bread.get(DataComponents.FOOD) != null, "bread must be food");
        AgedSurvivalDiet.onEaten(pig, bread);
        double grains = AgedSurvivalDiet.level(pig, AgedSurvivalDiet.GRAINS);
        helper.assertTrue(grains > 0, "eating bread must refill grains");
        helper.assertTrue(AgedSurvivalDiet.level(pig, AgedSurvivalDiet.PROTEINS)
                == AgedSurvivalDiet.MAX_NUTRIENTS,
                "unrelated groups must stay at default");
        helper.succeed();
    }

    @GameTest
    public void nonFoodDoesNotChangeDiet(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        AgedSurvivalDiet.setLevel(pig, AgedSurvivalDiet.VEGETABLES, 10.0);
        AgedSurvivalDiet.onEaten(pig, new ItemStack(Items.STICK));
        helper.assertTrue(
                AgedSurvivalDiet.level(pig, AgedSurvivalDiet.VEGETABLES) == 10.0,
                "sticks are not food; nutrients unchanged");
        helper.succeed();
    }

    @GameTest
    public void decayReducesNutrients(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        AgedSurvivalDiet.setLevel(pig, AgedSurvivalDiet.FRUITS, 50.0);
        var state = AgedSurvivalDiet.applyDecay(pig, AgedSurvivalConfig.get().diet);
        double fruits = AgedSurvivalDiet.level(pig, AgedSurvivalDiet.FRUITS);
        helper.assertTrue(fruits < 50.0 && fruits > 49.0,
                "one decay step should reduce slightly: " + fruits);
        helper.assertTrue(!state.allBalanced(), "50 in one group is not balanced overall");
        helper.assertTrue(state.deficient() == 0, "nothing below deficiency yet");
        helper.succeed();
    }

    @GameTest
    public void decayFlagsDeficiency(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        for (var group : new net.minecraft.tags.TagKey[]{AgedSurvivalDiet.FRUITS}) {
            AgedSurvivalDiet.setLevel(pig, group, 5.0);
        }
        var state = AgedSurvivalDiet.applyDecay(pig, AgedSurvivalConfig.get().diet);
        helper.assertTrue(state.deficient() > 0, "fruit at 5 must count as deficient");
        helper.succeed();
    }

    @GameTest
    public void perishableFoodRots(GameTestHelper helper) {
        SimpleContainer container = new SimpleContainer(new ItemStack(Items.BEEF));
        int rotted = AgedSurvivalSpoilage.spoilContainer(container,
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
        AgedSurvivalSpoilage.spoilContainer(container,
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
        int rotted = AgedSurvivalSpoilage.spoilContainer(container,
                net.minecraft.util.RandomSource.create(), 1.0,
                "minecraft:rotten_flesh", ignored -> { });
        helper.assertTrue(rotted == 0, "honey bottle is tagged non-spoiling");
        helper.assertTrue(!container.getItem(0).isEmpty(), "honey bottle untouched");
        helper.succeed();
    }

    @GameTest
    public void hydrationClampsAtMax(GameTestHelper helper) {
        var pig = helper.spawn(EntityTypes.PIG, 1, 2, 1);
        AgedSurvivalThirst.addHydration(pig, 9999.0);
        helper.assertTrue(AgedSurvivalThirst.hydration(pig)
                == AgedSurvivalThirst.MAX_HYDRATION, "hydration clamped to max");
        helper.succeed();
    }

}
