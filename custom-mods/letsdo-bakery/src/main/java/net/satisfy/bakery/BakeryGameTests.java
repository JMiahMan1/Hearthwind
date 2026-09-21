package net.satisfy.bakery;

import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.bakery.core.block.entity.SmallCookingPotBlockEntity;
import net.satisfy.bakery.core.effect.VitalityEffect;
import net.satisfy.bakery.core.recipe.BlankCakeInteractionInput;
import net.satisfy.bakery.core.recipe.BlankCakeInteractionRecipe;
import net.satisfy.bakery.core.recipe.BlankCakeStage;
import net.satisfy.bakery.core.registry.EntityTypeRegistry;
import net.satisfy.bakery.core.registry.MobEffectRegistry;
import net.satisfy.bakery.core.registry.ObjectRegistry;
import net.satisfy.bakery.core.registry.RecipeTypeRegistry;
import net.satisfy.bakery.platform.PlatformHelper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;


/**
 * Headless smoke tests for the bakery 26.2 port. No client needed: registry
 * sweep (catches missing setId / broken DeferredRegister wiring), datapack
 * recipe parsing (catches JSON format drift), block-entity placement, recipe
 * matching, and the VitalityEffect exhaustion bank cap.
 */
public final class BakeryGameTests {
    public BakeryGameTests() {
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
        helper.assertTrue(PlatformHelper.getVitalityEffectInterval() > 0, "vitality interval must be positive");
        helper.assertTrue(PlatformHelper.getVitalityEffectExhaustionReduction() >= 0, "vitality reduction must be sane");
        helper.succeed();
    }

    @GameTest
    public void allItemsAndBlocksResolve(GameTestHelper helper) {
        int n = sweepRegistrySuppliers(ObjectRegistry.class, "ObjectRegistry", helper);
        helper.assertTrue(n > 50, "bakery must register its full item/block set, got " + n);
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(Bakery.identifier("rolling_pin")), "rolling_pin item must exist");
        helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(Bakery.identifier("baker_station")), "baker_station block must exist");
        helper.succeed();
    }

    @GameTest
    public void blockEntitiesEffectsAndRecipeTypesResolve(GameTestHelper helper) {
        sweepRegistrySuppliers(EntityTypeRegistry.class, "EntityTypeRegistry", helper);
        sweepRegistrySuppliers(MobEffectRegistry.class, "MobEffectRegistry", helper);
        helper.assertTrue(RecipeTypeRegistry.BLANK_CAKE_INTERACTION_TYPE.get() != null, "blank cake type must resolve");
        helper.assertTrue(RecipeTypeRegistry.BAKING_STATION_RECIPE_TYPE.get() != null, "baking station type must resolve");
        helper.assertTrue(RecipeTypeRegistry.BLANK_CAKE_INTERACTION_SERIALIZER.get() != null, "blank cake serializer must resolve");
        helper.assertTrue(RecipeTypeRegistry.BAKING_STATION_RECIPE_SERIALIZER.get() != null, "baking station serializer must resolve");
        helper.succeed();
    }

    @GameTest
    public void blankCakeRecipesParseFromDatapack(GameTestHelper helper) {
        Identifier id = Bakery.identifier("blank_cake_interaction/apple_cupcake");
        boolean found = false;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.id().identifier().equals(id) && holder.value() instanceof BlankCakeInteractionRecipe) {
                found = true;
                break;
            }
        }
        helper.assertTrue(found, "apple_cupcake interaction recipe must parse (custom serializer round-trip)");
        helper.succeed();
    }

    @GameTest
    public void rollingPinRecipeMatches(GameTestHelper helper) {
        ItemStack rollingPin = new ItemStack(ObjectRegistry.ROLLING_PIN.get());
        BlankCakeInteractionInput input = new BlankCakeInteractionInput(rollingPin);
        boolean matched = false;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            if (holder.value() instanceof BlankCakeInteractionRecipe recipe
                    && recipe.matchesStage(BlankCakeStage.CUPCAKE)
                    && recipe.matches(input, helper.getLevel())) {
                matched = true;
                break;
            }
        }
        helper.assertTrue(matched, "rolling pin must match a cookie-stage interaction recipe");
        helper.succeed();
    }

    @GameTest
    public void sandwichRecipesParsingWithFacTags(GameTestHelper helper) {
        // Soft dep bakery -> farm_and_charm: sandwich recipes reference FAC
        // tags; presence proves they parsed with those refs intact.
        boolean sandwich = false;
        boolean vegetable = false;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            Identifier id = holder.id().identifier();
            if (id.equals(Bakery.identifier("sandwich"))) sandwich = true;
            if (id.equals(Bakery.identifier("vegetable_sandwich"))) vegetable = true;
        }
        helper.assertTrue(sandwich, "bakery:sandwich must parse (references #farm_and_charm:cabbage)");
        helper.assertTrue(vegetable, "bakery:vegetable_sandwich must parse (references FAC tags)");
        helper.succeed();
    }

    @GameTest
    public void facIngredientTagsPopulated(GameTestHelper helper) {
        // Soft dep bakery -> farm_and_charm: the tags above must be non-empty.
        var lookup = helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ITEM);
        for (String path : new String[]{"farm_and_charm:cabbage", "farm_and_charm:tomato"}) {
            var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(path));
            var set = lookup.get(tag);
            helper.assertTrue(set.isPresent() && set.get().size() > 0, "#" + path + " must be populated for bakery recipes");
        }
        helper.succeed();
    }

    @GameTest
    public void facCompatRecipesParse(GameTestHelper helper) {
        // Soft dep bakery -> farm_and_charm: compat recipes shipped in the
        // bakery jar under the farm_and_charm namespace, one per FAC station.
        record Expect(String path, Class<?> type) {
        }
        Expect[] expects = {
                new Expect("farm_and_charm:stove/baguette", net.satisfy.farm_and_charm.core.recipe.StoveRecipe.class),
                new Expect("farm_and_charm:stove/bread", net.satisfy.farm_and_charm.core.recipe.StoveRecipe.class),
                new Expect("farm_and_charm:crafting_bowl/cake_dough", net.satisfy.farm_and_charm.core.recipe.CraftingBowlRecipe.class),
                new Expect("farm_and_charm:crafting_bowl/sweet_dough", net.satisfy.farm_and_charm.core.recipe.CraftingBowlRecipe.class),
                new Expect("farm_and_charm:pot_cooking/chocolate_jam", net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe.class),
                new Expect("farm_and_charm:pot_cooking/pudding", net.satisfy.farm_and_charm.core.recipe.CookingPotRecipe.class),
        };
        for (Expect expect : expects) {
            boolean found = false;
            for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
                if (holder.id().identifier().equals(Identifier.parse(expect.path()))
                        && expect.type().isInstance(holder.value())) {
                    found = true;
                    break;
                }
            }
            helper.assertTrue(found, expect.path() + " must parse as " + expect.type().getSimpleName());
        }
        helper.succeed();
    }

    @GameTest
    public void smallCookingPotPlacesWithBlockEntity(GameTestHelper helper) {
        helper.setBlock(1, 2, 1, ObjectRegistry.SMALL_COOKING_POT.get());
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof SmallCookingPotBlockEntity,
                "small cooking pot must create its block entity on placement");
        helper.succeed();
    }

    @GameTest
    public void vitalityBankCapBinds(GameTestHelper helper) throws Exception {
        // NOTE: fabric's mock player is not alive headless, so build a real
        // ServerPlayer directly (never added to the level; the effect only
        // reads uuid/food/alive state).
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new com.mojang.authlib.GameProfile(UUID.randomUUID(), "VitalityProbe"),
                net.minecraft.server.level.ClientInformation.createDefault());
        helper.assertTrue(player.isAlive(), "probe player must be alive for effect ticks");
        helper.assertTrue(PlatformHelper.getVitalityEffectExhaustionReduction() > 0, "vitality reduction must be active");
        player.getFoodData().eat(2, 2.0F);
        helper.assertTrue(player.getFoodData().needsFood() || player.getFoodData().getSaturationLevel() > 0,
                "player must have saturation or hunger for the effect gate, sat=" + player.getFoodData().getSaturationLevel());
        VitalityEffect effect = new VitalityEffect();
        for (int i = 0; i < 100; i++) {
            helper.assertTrue(effect.applyEffectTick(helper.getLevel(), player, 0), "vitality tick must not fail");
        }
        Field statesField = VitalityEffect.class.getDeclaredField("STATES");
        statesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, Object> states = (Map<UUID, Object>) statesField.get(null);
        Object state = states.get(player.getUUID());
        helper.assertTrue(state != null, "idle vitality ticks must bank exhaustion");
        java.lang.reflect.Method bankedMethod = state.getClass().getMethod("banked");
        bankedMethod.setAccessible(true);
        float banked = (float) bankedMethod.invoke(state);
        helper.assertTrue(banked <= 4.0001F, "banked exhaustion must be capped at one cycle, got " + banked);
        helper.assertTrue(banked >= 3.9F, "bank should fill to the cap while idle, got " + banked);
        effect.onMobRemoved(helper.getLevel(), player, 0, Entity.RemovalReason.KILLED);
        helper.assertTrue(!states.containsKey(player.getUUID()), "effect removal must clear the ledger");
        helper.succeed();
    }
}
