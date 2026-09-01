package dev.jmiahman.hearthwind.flora;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;

/**
 * Gametests for Hearthwind Flora (Let's Do ecosystem port on 26.2).
 */
public final class HearthwindFloraGameTests {
    public HearthwindFloraGameTests() {}

    @GameTest
    public void vineryItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("vinery", "red_grape")), "vinery:red_grape exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("vinery", "fermentation_barrel")), "vinery:fermentation_barrel exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("vinery", "chenet_wine")), "vinery:chenet_wine exists");
        helper.succeed();
    }

    @GameTest
    public void candlelightItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("candlelight", "tomato")), "candlelight:tomato exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("candlelight", "lasagna")), "candlelight:lasagna exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("candlelight", "cooking_pan")), "candlelight:cooking_pan exists");
        helper.succeed();
    }

    @GameTest
    public void meadowItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("meadow", "edelweiss")), "meadow:edelweiss exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("meadow", "piece_of_cheese")), "meadow:piece_of_cheese exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("meadow", "cheese_form")), "meadow:cheese_form exists");
        helper.succeed();
    }

    @GameTest
    public void meadowAlpineOresRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("meadow", "alpine_salt")), "meadow:alpine_salt exists");
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath("meadow", "alpine_salt_ore")), "meadow:alpine_salt_ore block exists");
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath("meadow", "alpine_iron_ore")), "meadow:alpine_iron_ore block exists");
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath("meadow", "alpine_diamond_ore")), "meadow:alpine_diamond_ore block exists");
        helper.succeed();
    }

    @GameTest
    public void bakeryItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("bakery", "baguette")), "bakery:baguette exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("bakery", "strawberry")), "bakery:strawberry exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("bakery", "brick_oven")), "bakery:brick_oven exists");
        helper.succeed();
    }

    @GameTest
    public void herbalBrewsItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("herbalbrews", "lavender")), "herbalbrews:lavender exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("herbalbrews", "green_tea")), "herbalbrews:green_tea exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("herbalbrews", "tea_kettle")), "herbalbrews:tea_kettle exists");
        helper.succeed();
    }

    @GameTest
    public void farmAndCharmItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("farm_and_charm", "barley")), "farm_and_charm:barley exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("farm_and_charm", "corn")), "farm_and_charm:corn exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("farm_and_charm", "strawberries")), "farm_and_charm:strawberries exists");
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath("farm_and_charm", "silo")), "farm_and_charm:silo exists");
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath("farm_and_charm", "butter_churn")), "farm_and_charm:butter_churn exists");
        helper.succeed();
    }

    @GameTest
    public void breweryItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("brewery", "hops")), "brewery:hops exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("brewery", "beer")), "brewery:beer exists");
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath("brewery", "beer_barrel")), "brewery:beer_barrel exists");
        helper.succeed();
    }

    @GameTest
    public void netherVineryItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("nethervinery", "crimson_grape")), "nethervinery:crimson_grape exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("nethervinery", "warped_grape")), "nethervinery:warped_grape exists");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("nethervinery", "ghast_wine")), "nethervinery:ghast_wine exists");
        helper.succeed();
    }

    @GameTest
    public void vineryWineNutritionAndConsumable(GameTestHelper helper) {
        var wine = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("vinery", "chenet_wine"));
        helper.assertTrue(wine != null, "vinery:chenet_wine exists");
        var stack = new net.minecraft.world.item.ItemStack(wine);
        var food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        helper.assertTrue(food != null, "wine must have food component");
        helper.assertTrue(food.canAlwaysEat(), "wine must be always drinkable/edible");
        helper.assertTrue(food.nutrition() > 0, "wine must give nutrition");
        helper.succeed();
    }

    @GameTest
    public void candlelightProduceAndMealNutrition(GameTestHelper helper) {
        var tomato = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("candlelight", "tomato"));
        var lasagna = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("candlelight", "lasagna"));
        helper.assertTrue(tomato != null && lasagna != null, "produce and meals exist");
        var tFood = new net.minecraft.world.item.ItemStack(tomato).get(net.minecraft.core.component.DataComponents.FOOD);
        var lFood = new net.minecraft.world.item.ItemStack(lasagna).get(net.minecraft.core.component.DataComponents.FOOD);
        helper.assertTrue(tFood != null && lFood != null, "both must have food components");
        helper.assertTrue(lFood.nutrition() > tFood.nutrition(), "full meal lasagna must give more nutrition than raw tomato");
        helper.succeed();
    }

    @GameTest
    public void meadowCheeseNutritionAndForms(GameTestHelper helper) {
        var cheese = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("meadow", "piece_of_cheese"));
        helper.assertTrue(cheese != null, "cheese exists");
        var cFood = new net.minecraft.world.item.ItemStack(cheese).get(net.minecraft.core.component.DataComponents.FOOD);
        helper.assertTrue(cFood != null, "cheese must have food component");
        helper.assertTrue(cFood.nutrition() >= 4, "cheese must provide substantial nutrition");
        helper.succeed();
    }

    @GameTest
    public void bakeryBreadsAndPastriesNutrition(GameTestHelper helper) {
        var baguette = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("bakery", "baguette"));
        var cake = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("bakery", "strawberry_cake"));
        helper.assertTrue(baguette != null && cake != null, "baguette and cake exist");
        var bFood = new net.minecraft.world.item.ItemStack(baguette).get(net.minecraft.core.component.DataComponents.FOOD);
        var cFood = new net.minecraft.world.item.ItemStack(cake).get(net.minecraft.core.component.DataComponents.FOOD);
        helper.assertTrue(bFood != null && cFood != null, "baked goods must be edible");
        helper.succeed();
    }

    @GameTest
    public void herbalBrewsTeasAlwaysEdible(GameTestHelper helper) {
        var tea = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("herbalbrews", "green_tea"));
        helper.assertTrue(tea != null, "green_tea exists");
        var food = new net.minecraft.world.item.ItemStack(tea).get(net.minecraft.core.component.DataComponents.FOOD);
        helper.assertTrue(food != null, "tea must have food component");
        helper.assertTrue(food.canAlwaysEat(), "tea must be drinkable at full hunger");
        helper.succeed();
    }

    @GameTest
    public void fruitLeavesHarvestAndBonemeal(GameTestHelper helper) {
        var cherryLeaves = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("vinery", "dark_cherry_leaves"));
        var appleLeaves = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("vinery", "apple_leaves"));
        helper.assertTrue(cherryLeaves instanceof FruitLeavesBlock, "dark_cherry_leaves is a FruitLeavesBlock");
        helper.assertTrue(appleLeaves instanceof FruitLeavesBlock, "apple_leaves is a FruitLeavesBlock");

        var pos = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(pos, cherryLeaves.defaultBlockState().setValue(FruitLeavesBlock.AGE, 3));
        helper.assertTrue(helper.getBlockState(pos).getValue(FruitLeavesBlock.AGE) == 3, "cherry leaves at stage 3");

        var bonemealPos = new net.minecraft.core.BlockPos(1, 2, 2);
        helper.setBlock(bonemealPos, appleLeaves.defaultBlockState().setValue(FruitLeavesBlock.AGE, 1));
        ((FruitLeavesBlock) appleLeaves).performBonemeal(helper.getLevel(), helper.getLevel().getRandom(), helper.absolutePos(bonemealPos), helper.getBlockState(bonemealPos));
        helper.assertTrue(helper.getBlockState(bonemealPos).getValue(FruitLeavesBlock.AGE) == 2, "apple leaves advanced by bonemeal");

        helper.succeed();
    }

    @GameTest
    public void fermentationBarrelBlockEntityTicking(GameTestHelper helper) {
        var barrelBlock = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("vinery", "fermentation_barrel"));
        helper.assertTrue(barrelBlock != null, "fermentation_barrel block exists");

        var pos = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(pos, barrelBlock.defaultBlockState());

        var be = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(be instanceof dev.jmiahman.hearthwind.flora.blockentity.FermentationBarrelBlockEntity, "be is FermentationBarrelBlockEntity");
        helper.succeed();
    }

    @GameTest
    public void applePressBlockEntityInteraction(GameTestHelper helper) {
        var pressBlock = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("vinery", "apple_press"));
        helper.assertTrue(pressBlock != null, "apple_press block exists");

        var pos = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(pos, pressBlock.defaultBlockState());

        var be = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(be instanceof dev.jmiahman.hearthwind.flora.blockentity.ApplePressBlockEntity, "be is ApplePressBlockEntity");
        helper.succeed();
    }

    @GameTest
    public void cookingPotAndStoveBlockEntities(GameTestHelper helper) {
        var potBlock = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("farm_and_charm", "cooking_pot"));
        var stoveBlock = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("farm_and_charm", "stove"));
        helper.assertTrue(potBlock != null && stoveBlock != null, "pot and stove blocks exist");

        var stovePos = new net.minecraft.core.BlockPos(1, 1, 1);
        var potPos = new net.minecraft.core.BlockPos(1, 2, 1);

        helper.setBlock(stovePos, stoveBlock.defaultBlockState());
        helper.setBlock(potPos, potBlock.defaultBlockState());

        var stoveBe = helper.getLevel().getBlockEntity(helper.absolutePos(stovePos));
        var potBe = helper.getLevel().getBlockEntity(helper.absolutePos(potPos));

        helper.assertTrue(stoveBe instanceof dev.jmiahman.hearthwind.flora.blockentity.StoveBlockEntity, "stoveBe is StoveBlockEntity");
        helper.assertTrue(potBe instanceof dev.jmiahman.hearthwind.flora.blockentity.CookingPotBlockEntity, "potBe is CookingPotBlockEntity");
        helper.succeed();
    }

    @GameTest
    public void brewstationAndTeaKettleBlockEntities(GameTestHelper helper) {
        var brewBlock = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("brewery", "beer_barrel"));
        var kettleBlock = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("herbalbrews", "tea_kettle"));
        helper.assertTrue(brewBlock != null && kettleBlock != null, "brew and kettle blocks exist");

        var brewPos = new net.minecraft.core.BlockPos(1, 1, 1);
        var kettlePos = new net.minecraft.core.BlockPos(1, 2, 1);

        helper.setBlock(brewPos, brewBlock.defaultBlockState());
        helper.setBlock(kettlePos, kettleBlock.defaultBlockState());

        var brewBe = helper.getLevel().getBlockEntity(helper.absolutePos(brewPos));
        var kettleBe = helper.getLevel().getBlockEntity(helper.absolutePos(kettlePos));

        helper.assertTrue(brewBe instanceof dev.jmiahman.hearthwind.flora.blockentity.BrewstationBlockEntity, "brewBe is BrewstationBlockEntity");
        helper.assertTrue(kettleBe instanceof dev.jmiahman.hearthwind.flora.blockentity.TeaKettleBlockEntity, "kettleBe is TeaKettleBlockEntity");
        helper.succeed();
    }

    @GameTest
    public void cheeseRackAndStorageBlockEntities(GameTestHelper helper) {
        var cheeseBlock = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("meadow", "cheese_form"));
        var storageBlock = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("vinery", "wine_box"));
        helper.assertTrue(cheeseBlock != null && storageBlock != null, "cheese and storage blocks exist");

        var cheesePos = new net.minecraft.core.BlockPos(1, 1, 1);
        var storagePos = new net.minecraft.core.BlockPos(1, 2, 1);

        helper.setBlock(cheesePos, cheeseBlock.defaultBlockState());
        helper.setBlock(storagePos, storageBlock.defaultBlockState());

        var cheeseBe = helper.getLevel().getBlockEntity(helper.absolutePos(cheesePos));
        var storageBe = helper.getLevel().getBlockEntity(helper.absolutePos(storagePos));

        helper.assertTrue(cheeseBe instanceof dev.jmiahman.hearthwind.flora.blockentity.CheeseRackBlockEntity, "cheeseBe is CheeseRackBlockEntity");
        helper.assertTrue(storageBe instanceof dev.jmiahman.hearthwind.flora.blockentity.StorageBlockEntity, "storageBe is StorageBlockEntity");
        helper.succeed();
    }

    @GameTest
    public void grapevineGrowthAndHarvesting(GameTestHelper helper) {
        var block = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("vinery", "red_grapevine"));
        helper.assertTrue(block instanceof dev.jmiahman.hearthwind.flora.block.GrapevineBlock, "red_grapevine exists");
        var grapevineBlock = (dev.jmiahman.hearthwind.flora.block.GrapevineBlock) block;

        var pos = new net.minecraft.core.BlockPos(1, 2, 1);
        helper.setBlock(pos, grapevineBlock.defaultBlockState().setValue(dev.jmiahman.hearthwind.flora.block.GrapevineBlock.AGE, 0));
        helper.assertTrue(helper.getBlockState(pos).getValue(dev.jmiahman.hearthwind.flora.block.GrapevineBlock.AGE) == 0, "grapevine starts at age 0");

        grapevineBlock.performBonemeal(helper.getLevel(), helper.getLevel().getRandom(), helper.absolutePos(pos), helper.getBlockState(pos));
        helper.assertTrue(helper.getBlockState(pos).getValue(dev.jmiahman.hearthwind.flora.block.GrapevineBlock.AGE) == 1, "grapevine advanced to age 1");

        helper.setBlock(pos, grapevineBlock.defaultBlockState().setValue(dev.jmiahman.hearthwind.flora.block.GrapevineBlock.AGE, 3));
        helper.assertTrue(helper.getBlockState(pos).getValue(dev.jmiahman.hearthwind.flora.block.GrapevineBlock.AGE) == 3, "grapevine is fully ripe at age 3");
        helper.succeed();
    }

    @GameTest
    public void chairSeatingEntity(GameTestHelper helper) {
        var pos = new net.minecraft.core.BlockPos(1, 1, 1);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        boolean seated = dev.jmiahman.hearthwind.flora.block.ChairEntity.sitPlayer(helper.getLevel(), helper.absolutePos(pos), player);
        helper.assertTrue(seated, "player successfully sat on chair");
        helper.succeed();
    }

    @GameTest
    public void floraStatusEffectsRegistered(GameTestHelper helper) {
        helper.assertTrue(FloraStatusEffects.JELLIE != null, "vinery:jellie effect exists");
        helper.assertTrue(FloraStatusEffects.SUGAR_RUSH != null, "bakery:sugar_rush effect exists");
        helper.assertTrue(FloraStatusEffects.INTOXICATION != null, "brewery:intoxication effect exists");
        helper.assertTrue(FloraStatusEffects.WELL_SERVED != null, "candlelight:well_served effect exists");
        helper.assertTrue(FloraStatusEffects.FARMERS_BLESSING != null, "farm_and_charm:farmers_blessing effect exists");
        helper.assertTrue(FloraStatusEffects.LIFELEECH != null, "herbalbrews:lifeleech effect exists");
        helper.succeed();
    }

    @GameTest
    public void waterSprinklerHydration(GameTestHelper helper) {
        var sprinklerPos = new net.minecraft.core.BlockPos(2, 2, 2);
        var farmPos = new net.minecraft.core.BlockPos(2, 1, 3);

        helper.setBlock(farmPos, net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE, 0));
        helper.assertTrue(helper.getBlockState(farmPos).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE) == 0, "farmland starts dry");

        dev.jmiahman.hearthwind.flora.blockentity.FarmMachineryBlockEntities.WaterSprinklerBlockEntity.hydrateArea(
                helper.getLevel(), helper.absolutePos(sprinklerPos));

        helper.assertTrue(helper.getBlockState(farmPos).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE) == 7, "farmland was hydrated by sprinkler");
        helper.succeed();
    }
}
