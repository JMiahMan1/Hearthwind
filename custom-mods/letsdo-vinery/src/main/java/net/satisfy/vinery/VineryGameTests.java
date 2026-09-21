package net.satisfy.vinery;

import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.entity.ApplePressBlockEntity;
import net.satisfy.vinery.core.block.entity.FermentationBarrelBlockEntity;
import net.satisfy.vinery.core.recipe.ApplePressFermentingRecipe;
import net.satisfy.vinery.core.recipe.ApplePressMashingRecipe;
import net.satisfy.vinery.core.recipe.FermentationBarrelRecipe;
import net.satisfy.vinery.core.registry.EntityTypeRegistry;
import net.satisfy.vinery.core.registry.MobEffectRegistry;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.registry.RecipeTypesRegistry;
import net.satisfy.vinery.platform.PlatformHelper;

import java.lang.reflect.Field;

/**
 * Headless smoke tests for the vinery 26.2 port: registry sweep, datapack
 * recipe parsing (custom serializers), station block entities, and the
 * sobriety states (wine recipes output juice while alcohol is removed).
 */
public final class VineryGameTests {
    public VineryGameTests() {
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
    public void configLoadsSaneValues(GameTestHelper helper) {
        helper.assertTrue(PlatformHelper.getTotalFermentationTime() > 0, "fermentation time must be positive");
        helper.assertTrue(PlatformHelper.getMaxFluidLevel() > 0, "fluid level must be positive");
        helper.succeed();
    }

    @GameTest
    public void allItemsAndBlocksResolve(GameTestHelper helper) {
        int n = sweepRegistrySuppliers(ObjectRegistry.class, "ObjectRegistry", helper);
        helper.assertTrue(n > 150, "vinery must register its full item/block set, got " + n);
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(Vinery.identifier("red_grapejuice")), "red_grapejuice must exist");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(Vinery.identifier("honey_cordial")), "honey_cordial must exist");
        helper.succeed();
    }

    @GameTest
    public void blockEntitiesEffectsAndRecipeTypesResolve(GameTestHelper helper) {
        sweepRegistrySuppliers(EntityTypeRegistry.class, "EntityTypeRegistry", helper);
        int mobEffects = 0;
        for (Field f : MobEffectRegistry.class.getDeclaredFields()) {
            if (f.getType() == Identifier.class && java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
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
        helper.assertTrue(RecipeTypesRegistry.FERMENTATION_BARREL_RECIPE_TYPE.get() != null, "fermentation type must resolve");
        helper.assertTrue(RecipeTypesRegistry.APPLE_PRESS_MASHING_RECIPE_TYPE.get() != null, "mashing type must resolve");
        helper.assertTrue(RecipeTypesRegistry.APPLE_PRESS_FERMENTING_RECIPE_TYPE.get() != null, "fermenting type must resolve");
        helper.assertTrue(RecipeTypesRegistry.FERMENTATION_BARREL_RECIPE_SERIALIZER.get() != null, "fermentation serializer must resolve");
        helper.succeed();
    }

    @GameTest
    public void fermentationRecipesParseFromDatapack(GameTestHelper helper) {
        Identifier id = Vinery.identifier("wine_fermentation/red_wine");
        boolean found = false;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.id().identifier().equals(id) && holder.value() instanceof FermentationBarrelRecipe) {
                found = true;
                break;
            }
        }
        helper.assertTrue(found, "red_wine fermentation recipe must parse (custom serializer round-trip)");
        helper.succeed();
    }

    @GameTest
    public void soberFermentationOutputsJuice(GameTestHelper helper) {
        if (!dev.jmiahman.hearthwind.survival.Sobriety.alcoholRemoved()) {
            helper.succeed();
            return;
        }
        FermentationBarrelRecipe recipe = null;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.id().identifier().equals(Vinery.identifier("wine_fermentation/red_wine"))
                    && holder.value() instanceof FermentationBarrelRecipe r) {
                recipe = r;
                break;
            }
        }
        helper.assertTrue(recipe != null, "red_wine fermentation recipe must exist");
        helper.assertTrue(recipe.getResultItem().is(ObjectRegistry.RED_GRAPEJUICE.get()),
                "sober red_wine fermentation must output red grapejuice");
        helper.succeed();
    }

    @GameTest
    public void soberMeadOutputsHoneyCordial(GameTestHelper helper) {
        if (!dev.jmiahman.hearthwind.survival.Sobriety.alcoholRemoved()) {
            helper.succeed();
            return;
        }
        boolean found = false;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.id().identifier().equals(Vinery.identifier("wine_fermentation/mead"))
                    && holder.value() instanceof FermentationBarrelRecipe r
                    && r.getResultItem().is(ObjectRegistry.HONEY_CORDIAL.get())) {
                found = true;
                break;
            }
        }
        helper.assertTrue(found, "sober mead fermentation must output honey cordial");
        helper.succeed();
    }

    @GameTest
    public void applePressRecipesParse(GameTestHelper helper) {
        boolean mashing = false;
        boolean fermenting = false;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.value() instanceof ApplePressMashingRecipe) mashing = true;
            if (holder.value() instanceof ApplePressFermentingRecipe) fermenting = true;
        }
        helper.assertTrue(mashing, "apple press mashing recipes must parse");
        helper.assertTrue(fermenting, "apple press fermenting recipes must parse");
        helper.succeed();
    }

    @GameTest
    public void fermentationBarrelPlacesWithBlockEntity(GameTestHelper helper) {
        helper.setBlock(1, 2, 1, ObjectRegistry.FERMENTATION_BARREL.get());
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof FermentationBarrelBlockEntity,
                "fermentation barrel must create its block entity on placement");
        helper.succeed();
    }

    @GameTest
    public void applePressPlacesWithBlockEntity(GameTestHelper helper) {
        helper.setBlock(1, 2, 1, ObjectRegistry.APPLE_PRESS.get());
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof ApplePressBlockEntity,
                "apple press must create its block entity on placement");
        helper.succeed();
    }
}
