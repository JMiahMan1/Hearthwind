package dev.jmiahman.hearthwind.world;

import net.minecraft.gametest.framework.GameTestHelper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Headless gametests for the world module; run via
 * custom-mods/tools/run_gametests.sh.
 */
public final class HearthwindWorldGameTests {
    public HearthwindWorldGameTests() {}

    @GameTest
    public void seasonFromDayCyclesCorrectly(GameTestHelper helper) {
        int days = 18;
        helper.assertTrue(Season.fromDay(0, days) == Season.SPRING, "day 0 -> spring");
        helper.assertTrue(Season.fromDay(days - 1, days) == Season.SPRING, "day N-1 -> spring");
        helper.assertTrue(Season.fromDay(days, days) == Season.SUMMER, "day N -> summer");
        helper.assertTrue(Season.fromDay(2 * days - 1, days) == Season.SUMMER, "day 2N-1 -> summer");
        helper.assertTrue(Season.fromDay(3 * days - 1, days) == Season.AUTUMN, "day 3N-1 -> autumn");
        helper.assertTrue(Season.fromDay(4 * days - 1, days) == Season.WINTER, "day 4N-1 -> winter");
        helper.assertTrue(Season.fromDay(4 * days, days) == Season.SPRING, "day 4N wraps to spring");
        helper.succeed();
    }

    @GameTest
    public void seasonFromWorldTimeUsesGameTime(GameTestHelper helper) {
        int days = 18;
        helper.assertTrue(Season.fromWorldTime(0, days) == Season.SPRING, "gt 0 -> spring");
        long summerStart = days * 24000L;
        helper.assertTrue(Season.fromWorldTime(summerStart, days) == Season.SUMMER,
                "gt = 18 days -> summer");
        helper.succeed();
    }

    @GameTest
    public void cropMultiplierReturnsCorrectSeason(GameTestHelper helper) {
        HearthwindWorldConfig cfg = HearthwindWorldConfig.get();
        helper.assertTrue(cfg.springCropMultiplier == 1.0, "spring multiplier default 1.0");
        helper.assertTrue(cfg.summerCropMultiplier == 1.2, "summer multiplier default 1.2");
        helper.assertTrue(cfg.autumnCropMultiplier == 0.9, "autumn multiplier default 0.9");
        helper.assertTrue(cfg.winterCropMultiplier == 0.4, "winter multiplier default 0.4");

        helper.assertTrue(Season.SPRING.cropMultiplier(cfg) == 1.0, "spring -> 1.0");
        helper.assertTrue(Season.SUMMER.cropMultiplier(cfg) == 1.2, "summer -> 1.2");
        helper.assertTrue(Season.AUTUMN.cropMultiplier(cfg) == 0.9, "autumn -> 0.9");
        helper.assertTrue(Season.WINTER.cropMultiplier(cfg) == 0.4, "winter -> 0.4");
        helper.succeed();
    }

    @GameTest
    public void tempOffsetReturnsCorrectSeason(GameTestHelper helper) {
        HearthwindWorldConfig cfg = HearthwindWorldConfig.get();
        helper.assertTrue(cfg.springTempOffset == 0.5, "spring temp offset 0.5");
        helper.assertTrue(cfg.summerTempOffset == 2.0, "summer temp offset 2.0");
        helper.assertTrue(cfg.autumnTempOffset == 0.0, "autumn temp offset 0.0");
        helper.assertTrue(cfg.winterTempOffset == -3.0, "winter temp offset -3.0");

        helper.assertTrue(Season.SPRING.tempOffset(cfg) == 0.5, "spring -> 0.5");
        helper.assertTrue(Season.SUMMER.tempOffset(cfg) == 2.0, "summer -> 2.0");
        helper.assertTrue(Season.AUTUMN.tempOffset(cfg) == 0.0, "autumn -> 0.0");
        helper.assertTrue(Season.WINTER.tempOffset(cfg) == -3.0, "winter -> -3.0");
        helper.succeed();
    }

    @GameTest
    public void seasonFromWorldTimeWithLongTicks(GameTestHelper helper) {
        int days = 18;
        long gtSpring = 0;
        long gtSummer = days * 24000L;
        long gtAutumn = 2L * days * 24000L;
        long gtWinter = 3L * days * 24000L;
        helper.assertTrue(Season.fromWorldTime(gtSpring, days) == Season.SPRING, "gt 0 is spring");
        helper.assertTrue(Season.fromWorldTime(gtSummer, days) == Season.SUMMER, "gt 18 days is summer");
        helper.assertTrue(Season.fromWorldTime(gtAutumn, days) == Season.AUTUMN, "gt 36 days is autumn");
        helper.assertTrue(Season.fromWorldTime(gtWinter, days) == Season.WINTER, "gt 54 days is winter");
        helper.succeed();
    }

    @GameTest
    public void naturalistAnimalsAndDropsRegistered(GameTestHelper helper) {
        // Entity types
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("naturalist", "snail")), "naturalist:snail exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("naturalist", "deer")), "naturalist:deer exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("naturalist", "bear")), "naturalist:bear exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("naturalist", "duck")), "naturalist:duck exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("naturalist", "lion")), "naturalist:lion exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("naturalist", "snake")), "naturalist:snake exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("naturalist", "alligator")), "naturalist:alligator exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("naturalist", "boar")), "naturalist:boar exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath("naturalist", "hippo")), "naturalist:hippo exists");

        // Drops & Meats
        String[] meats = {
            "venison", "cooked_venison", "duck", "cooked_duck",
            "boar_chop", "cooked_boar_chop", "alligator_tail", "cooked_alligator_tail",
            "hippo_meat", "cooked_hippo_meat", "lizard_tail", "cooked_lizard_tail",
            "snail", "cooked_snail", "bass", "cooked_bass", "catfish", "cooked_catfish"
        };
        for (String meat : meats) {
            var item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("naturalist", meat));
            helper.assertTrue(item != null, "naturalist:" + meat + " exists");
            var food = new net.minecraft.world.item.ItemStack(item).get(net.minecraft.core.component.DataComponents.FOOD);
            helper.assertTrue(food != null, "meat " + meat + " must have food component");
            helper.assertTrue(food.nutrition() > 0, "meat " + meat + " must give nutrition");
        }

        // Trophies, Horns & Materials
        String[] materials = {
            "bear_fur", "deer_antler", "duck_feather", "lion_mane",
            "elephant_tusk", "rhino_horn", "rattle", "vulture_feather",
            "snail_shell", "snail_mucus", "snail_bucket", "glow_goop", "caterpillar"
        };
        for (String mat : materials) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("naturalist", mat)),
                    "naturalist material: " + mat + " exists");
        }

        helper.succeed();
    }

    @GameTest
    public void explorationItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(
                Identifier.fromNamespaceAndPath("antiqueatlas", "antique_atlas")), "antique_atlas exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(
                Identifier.fromNamespaceAndPath("exposure", "camera")), "exposure:camera exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(
                Identifier.fromNamespaceAndPath("exposure", "photograph")), "exposure:photograph exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(
                Identifier.fromNamespaceAndPath("inmis", "baby_backpack")), "inmis:baby_backpack exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(
                Identifier.fromNamespaceAndPath("inmis", "plated_backpack")), "inmis:plated_backpack exists");
        helper.succeed();
    }

    @GameTest
    public void winterSnowAccumulationPlacesLayer(GameTestHelper helper) {
        var groundPos = new net.minecraft.core.BlockPos(2, 2, 2);
        var airPos = groundPos.above();
        helper.setBlock(groundPos, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
        helper.setBlock(airPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());

        boolean placed = dev.jmiahman.hearthwind.world.snow.WinterSnowAccumulation.tryAccumulateSnow(helper.getLevel(), helper.absolutePos(airPos));
        helper.assertTrue(placed, "Snow must accumulate on solid ground in winter");
        helper.succeed();
    }

    @GameTest
    public void seasonCropCorpusLoads(GameTestHelper helper) {
        SeasonCrops.load(helper.getLevel().getServer().getResourceManager());
        helper.assertTrue(SeasonCrops.count() >= 10,
                "per-crop season corpus must load (10+ crops), loaded " + SeasonCrops.count());
        helper.succeed();
    }

    @GameTest
    public void winterBlocksBreeding(GameTestHelper helper) {
        helper.assertTrue(!HearthwindWorldConfig.get().animalsBreedInWinter,
                "animals must not breed in winter by default");
        helper.succeed();
    }
}
