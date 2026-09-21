package net.satisfy.farm_and_charm;

import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.satisfy.farm_and_charm.core.registry.MobEffectRegistry;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.farm_and_charm.core.block.entity.CookingPotBlockEntity;
import net.satisfy.farm_and_charm.core.block.entity.StoveBlockEntity;
import net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe;
import net.satisfy.farm_and_charm.core.registry.EntityTypeRegistry;
import net.satisfy.farm_and_charm.core.registry.MobEffectRegistry;
import net.satisfy.farm_and_charm.core.registry.ObjectRegistry;
import net.satisfy.farm_and_charm.core.registry.RecipeTypeRegistry;

import java.lang.reflect.Field;

/**
 * Headless smoke tests for the farm-and-charm 26.2 port: registry sweep,
 * datapack recipe parsing, block-entity placement, and cross-mod compat
 * (bakery recipes cooked in the FAC pot must load when bakery is present).
 */
public final class FarmAndCharmGameTests {
    public FarmAndCharmGameTests() {
    }

    private static int sweepRegistrySuppliers(Class<?> registryClass, String what, GameTestHelper helper) {
        int count = 0;
        for (Field f : registryClass.getDeclaredFields()) {
            if (!RegistrySupplier.class.isAssignableFrom(f.getType())) continue;
            if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            try {
                RegistrySupplier<?> supplier = (RegistrySupplier<?>) f.get(null);
                helper.assertTrue(supplier.get() != null, what + "." + f.getName() + " must resolve");
                count++;
            } catch (IllegalAccessException e) {
                helper.fail(what + "." + f.getName() + " not accessible: " + e.getMessage());
            }
        }
        helper.assertTrue(count > 0, what + " must declare entries");
        return count;
    }

    @GameTest
    public void allItemsAndBlocksResolve(GameTestHelper helper) {
        int n = sweepRegistrySuppliers(ObjectRegistry.class, "ObjectRegistry", helper);
        helper.assertTrue(n > 100, "farm_and_charm must register its full item/block set, got " + n);
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(FarmAndCharm.identifier("stove")), "stove block must exist");
        helper.succeed();
    }

    @GameTest
    public void blockEntitiesEffectsAndRecipeTypesResolve(GameTestHelper helper) {
        sweepRegistrySuppliers(EntityTypeRegistry.class, "EntityTypeRegistry", helper);
        int mobEffects = 0;
        for (Field f : MobEffectRegistry.class.getDeclaredFields()) {
            if (f.getType() == net.minecraft.resources.Identifier.class && java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                try {
                    Identifier id = (Identifier) f.get(null);
                    helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.keySet().contains(id),
                            "mob effect " + id + " must resolve");
                    mobEffects++;
                } catch (IllegalAccessException e) {
                    helper.fail("MobEffectRegistry." + f.getName() + " not accessible");
                }
            }
        }
        helper.assertTrue(mobEffects > 0, "MobEffectRegistry must declare entries");
        helper.assertTrue(RecipeTypeRegistry.COOKING_POT_RECIPE_TYPE.get() != null, "cooking pot type must resolve");
        helper.assertTrue(RecipeTypeRegistry.COOKING_POT_RECIPE_SERIALIZER.get() != null, "cooking pot serializer must resolve");
        helper.succeed();
    }

    @GameTest
    public void potCookingRecipesParseFromDatapack(GameTestHelper helper) {
        Identifier id = FarmAndCharm.identifier("pot_cooking/yeast");
        boolean found = false;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.id().identifier().equals(id) && holder.value() instanceof CookingPotRecipe) {
                found = true;
                break;
            }
        }
        helper.assertTrue(found, "yeast pot recipe must parse (custom serializer round-trip)");
        helper.succeed();
    }

    @GameTest
    public void bakeryCompatRecipesLoadAlongside(GameTestHelper helper) {
        Identifier id = FarmAndCharm.identifier("pot_cooking/apple_jam");
        boolean found = false;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.id().identifier().equals(id) && holder.value() instanceof CookingPotRecipe) {
                found = true;
                break;
            }
        }
        helper.assertTrue(found, "bakery apple_jam pot recipe must load when bakery is present");
        helper.succeed();
    }

    @GameTest
    public void bakerySmallPotInFacTags(GameTestHelper helper) {
        // Soft dep farm_and_charm -> bakery: FAC's optional tag entries must
        // resolve the bakery pot when bakery is loaded (both directions).
        var lookup = helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK);
        var pot = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(BakeryCompat.BAKERY_POT_ID);
        for (String path : new String[]{"farm_and_charm:cooking_pots", "farm_and_charm:suppress_campfire_smoke_particles"}) {
            var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.parse(path));
            var set = lookup.get(tag);
            helper.assertTrue(set.isPresent(), "#" + path + " must exist");
            boolean hasPot = false;
            var it = set.get().iterator();
            while (it.hasNext()) {
                if (it.next().value() == pot) {
                    hasPot = true;
                    break;
                }
            }
            helper.assertTrue(hasPot, "#" + path + " must contain bakery:small_cooking_pot when bakery is loaded");
        }
        helper.succeed();
    }

    private static final class BakeryCompat {
        // Hard reference avoided: bakery may be absent, so resolve by id.
        static final Identifier BAKERY_POT_ID = Identifier.parse("bakery:small_cooking_pot");
    }

    @GameTest
    public void stovePlacesWithBlockEntity(GameTestHelper helper) {
        helper.setBlock(1, 2, 1, ObjectRegistry.STOVE.get());
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof StoveBlockEntity,
                "stove must create its block entity on placement");
        helper.succeed();
    }

    @GameTest
    public void cookingPotPlacesWithBlockEntity(GameTestHelper helper) {
        helper.setBlock(1, 2, 1, ObjectRegistry.COOKING_POT.get());
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof CookingPotBlockEntity,
                "cooking pot must create its block entity on placement");
        helper.succeed();
    }

    @GameTest
    public void plantingCropAboveFarmlandDoesNotCrash(GameTestHelper helper) {
        // Regression: neighborChanged's Orientation is null for directionless
        // updates (plain setBlock notifications) - the farmland planting
        // particle mixin must null-guard instead of NPEing the tick loop.
        helper.setBlock(1, 1, 1, net.minecraft.world.level.block.Blocks.FARMLAND);
        helper.setBlock(1, 2, 1, net.minecraft.world.level.block.Blocks.WHEAT);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.WHEAT),
                "crop must be placed above farmland");
        helper.succeed();
    }
}
