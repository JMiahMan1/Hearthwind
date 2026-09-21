package net.satisfy.brewery;

import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.brewery.core.block.entity.BrewstationBlockEntity;
import net.satisfy.brewery.core.recipe.BrewingRecipe;
import net.satisfy.brewery.core.registry.EntityTypeRegistry;
import net.satisfy.brewery.core.registry.MobEffectRegistry;
import net.satisfy.brewery.core.registry.ObjectRegistry;
import net.satisfy.brewery.core.registry.RecipeTypeRegistry;

import java.lang.reflect.Field;

/**
 * Headless smoke tests for the brewery 26.2 port: registry sweep, datapack
 * recipe parsing (custom serializer), station block entities, and the
 * sobriety states (alcohol brewing recipes only exist when alcohol is on;
 * the four NA staples always brew).
 */
public final class BreweryGameTests {
    public BreweryGameTests() {
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
        helper.assertTrue(n > 70, "brewery must register its full item/block set, got " + n);
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Brewery.identifier("root_beer")), "root_beer must exist");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Brewery.identifier("small_beer")), "small_beer must exist");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Brewery.identifier("kvass")), "kvass must exist");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Brewery.identifier("coffee")), "coffee must exist");
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(Brewery.identifier("root_beer")), "root_beer block must exist");
        helper.succeed();
    }

    @GameTest
    public void blockEntitiesEffectsAndRecipeTypesResolve(GameTestHelper helper) {
        sweepRegistrySuppliers(EntityTypeRegistry.class, "EntityTypeRegistry", helper);
        int mobEffects = 0;
        for (Field f : MobEffectRegistry.class.getDeclaredFields()) {
            if (!RegistrySupplier.class.isAssignableFrom(f.getType())) continue;
            if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            try {
                RegistrySupplier<MobEffect> supplier = (RegistrySupplier<MobEffect>) f.get(null);
                helper.assertTrue(supplier.get() != null, "MobEffectRegistry." + f.getName() + " must resolve");
                mobEffects++;
            } catch (IllegalAccessException e) {
                helper.fail("MobEffectRegistry." + f.getName() + " not accessible");
            }
        }
        helper.assertTrue(mobEffects > 0, "MobEffectRegistry must declare entries");
        helper.assertTrue(RecipeTypeRegistry.BREWING_RECIPE_TYPE.get() != null, "brewing type must resolve");
        helper.assertTrue(RecipeTypeRegistry.BREWING_RECIPE_SERIALIZER.get() != null, "brewing serializer must resolve");
        helper.succeed();
    }

    @GameTest
    public void naBrewingRecipesParseFromDatapack(GameTestHelper helper) {
        for (String name : new String[]{"root_beer", "small_beer", "kvass", "coffee"}) {
            Identifier id = Brewery.identifier("brewing/" + name);
            boolean found = false;
            for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
                if (holder.id().identifier().equals(id) && holder.value() instanceof BrewingRecipe) {
                    found = true;
                    break;
                }
            }
            helper.assertTrue(found, name + " brewing recipe must parse (custom serializer round-trip)");
        }
        helper.succeed();
    }

    @GameTest
    public void soberAlcoholRecipesAbsent(GameTestHelper helper) {
        Identifier id = Brewery.identifier("brewing/wheat_beer");
        boolean found = false;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.id().identifier().equals(id) && holder.value() instanceof BrewingRecipe) {
                found = true;
                break;
            }
        }
        if (dev.jmiahman.hearthwind.survival.Sobriety.alcoholRemoved()) {
            helper.assertTrue(!found, "sober pack must not contain the wheat_beer brewing recipe");
        } else {
            helper.assertTrue(found, "alcohol-enabled pack must contain the wheat_beer brewing recipe");
        }
        helper.succeed();
    }

    @GameTest
    public void brewKettlePlacesWithBlockEntity(GameTestHelper helper) {
        helper.setBlock(1, 2, 1, ObjectRegistry.WOODEN_BREWINGSTATION.get());
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof BrewstationBlockEntity,
                "brew kettle must create its block entity on placement");
        helper.succeed();
    }

    @GameTest
    public void beverageMugPlaces(GameTestHelper helper) {
        helper.setBlock(1, 2, 1, ObjectRegistry.ROOT_BEER.get());
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(ObjectRegistry.ROOT_BEER.get()),
                "root beer mug must place as a block");
        helper.succeed();
    }
}
