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
}
