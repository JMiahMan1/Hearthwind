package net.satisfy.herbalbrews;

import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.herbalbrews.core.blocks.entity.CauldronBlockEntity;
import net.satisfy.herbalbrews.core.blocks.entity.TeaKettleBlockEntity;
import net.satisfy.herbalbrews.core.recipe.CauldronRecipe;
import net.satisfy.herbalbrews.core.recipe.TeaKettleRecipe;
import net.satisfy.herbalbrews.core.registry.EntityTypeRegistry;
import net.satisfy.herbalbrews.core.registry.EffectRegistry;
import net.satisfy.herbalbrews.core.registry.ObjectRegistry;
import net.satisfy.herbalbrews.core.registry.RecipeTypeRegistry;
import net.satisfy.herbalbrews.core.util.HerbalBrewsIdentifier;

import java.lang.reflect.Field;

/**
 * Headless smoke tests for the herbalbrews 26.2 port: registry sweep,
 * datapack recipe parsing (custom serializers), station block entities.
 * All drinks are teas/coffees/infusions - sobriety N/A (no alcohol content).
 */
public final class HerbalBrewsGameTests {
    public HerbalBrewsGameTests() {
    }

    private static Identifier id(String path) {
        return HerbalBrewsIdentifier.identifier(path);
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
        helper.assertTrue(n > 50, "herbalbrews must register its full item/block set, got " + n);
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(id("green_tea")), "green_tea must exist");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(id("coffee")), "coffee must exist");
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(id("tea_kettle")), "tea_kettle block must exist");
        helper.succeed();
    }

    @GameTest
    public void blockEntitiesEffectsAndRecipeTypesResolve(GameTestHelper helper) {
        sweepRegistrySuppliers(EntityTypeRegistry.class, "EntityTypeRegistry", helper);
        int mobEffects = 0;
        for (Field f : EffectRegistry.class.getDeclaredFields()) {
            if (!RegistrySupplier.class.isAssignableFrom(f.getType())) continue;
            if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            try {
                RegistrySupplier<?> supplier = (RegistrySupplier<?>) f.get(null);
                helper.assertTrue(supplier.get() != null, "EffectRegistry." + f.getName() + " must resolve");
                mobEffects++;
            } catch (IllegalAccessException e) {
                helper.fail("EffectRegistry." + f.getName() + " not accessible");
            }
        }
        helper.assertTrue(mobEffects > 0, "MobEffectRegistry must declare entries");
        helper.assertTrue(RecipeTypeRegistry.TEA_KETTLE_RECIPE_TYPE.get() != null, "kettle type must resolve");
        helper.assertTrue(RecipeTypeRegistry.CAULDRON_RECIPE_TYPE.get() != null, "cauldron type must resolve");
        helper.assertTrue(RecipeTypeRegistry.TEA_KETTLE_RECIPE_SERIALIZER.get() != null, "kettle serializer must resolve");
        helper.succeed();
    }

    @GameTest
    public void kettleBrewingRecipesParseFromDatapack(GameTestHelper helper) {
        for (String name : new String[]{"green_tea", "black_tea", "coffee", "hibiscus_tea"}) {
            Identifier rid = id("kettle_brewing/" + name);
            boolean found = false;
            for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
                if (holder.id().identifier().equals(rid) && holder.value() instanceof TeaKettleRecipe) {
                    found = true;
                    break;
                }
            }
            helper.assertTrue(found, name + " kettle recipe must parse (custom serializer round-trip)");
        }
        helper.succeed();
    }

    @GameTest
    public void cauldronRecipesParseFromDatapack(GameTestHelper helper) {
        boolean found = false;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.value() instanceof CauldronRecipe) {
                found = true;
                break;
            }
        }
        helper.assertTrue(found, "cauldron recipes must parse");
        helper.succeed();
    }

    @GameTest
    public void teaKettlePlacesWithBlockEntity(GameTestHelper helper) {
        helper.setBlock(1, 2, 1, ObjectRegistry.TEA_KETTLE.get());
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof TeaKettleBlockEntity,
                "tea kettle must create its block entity on placement");
        helper.succeed();
    }

    @GameTest
    public void cauldronPlacesWithBlockEntity(GameTestHelper helper) {
        helper.setBlock(1, 2, 1, ObjectRegistry.CAULDRON.get());
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof CauldronBlockEntity,
                "cauldron must create its block entity on placement");
        helper.succeed();
    }
}
