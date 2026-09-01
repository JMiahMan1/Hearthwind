package com.talhanation.smallships;

import com.talhanation.smallships.world.entity.ModEntityTypes;
import com.talhanation.smallships.world.entity.cannon.GroundCannonEntity;
import com.talhanation.smallships.world.entity.projectile.CannonBallEntity;
import com.talhanation.smallships.world.entity.ship.BriggEntity;
import com.talhanation.smallships.world.entity.ship.CogEntity;
import com.talhanation.smallships.world.entity.ship.DrakkarEntity;
import com.talhanation.smallships.world.entity.ship.GalleyEntity;
import com.talhanation.smallships.world.item.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Headless gametests for the smallships 26.2 port; run via
 * custom-mods/tools/run_gametests.sh.
 */
public final class SmallshipsGameTests {
    public SmallshipsGameTests() {}

    private static void assertItemRegistered(GameTestHelper helper, Item item, String what) {
        helper.assertTrue(item != null && item != Items.AIR, what + " resolves to an item");
        helper.assertTrue(item != null && BuiltInRegistries.ITEM.getKey(item) != null,
                what + " has a registry key");
    }

    @GameTest
    public void shipEntityTypesRegistered(GameTestHelper helper) {
        String ns = SmallShipsMod.MOD_ID;
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath(ns, CogEntity.ID)), "smallships:cog exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath(ns, BriggEntity.ID)), "smallships:brigg exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath(ns, GalleyEntity.ID)), "smallships:galley exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath(ns, DrakkarEntity.ID)), "smallships:drakkar exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath(ns, GroundCannonEntity.ID)), "smallships:ground_cannon exists");
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath(ns, CannonBallEntity.ID)), "smallships:cannon_ball exists");
        helper.assertTrue(ModEntityTypes.COG != null, "ModEntityTypes.COG wired");
        helper.assertTrue(ModEntityTypes.BRIGG != null, "ModEntityTypes.BRIGG wired");
        helper.assertTrue(ModEntityTypes.GALLEY != null, "ModEntityTypes.GALLEY wired");
        helper.assertTrue(ModEntityTypes.DRAKKAR != null, "ModEntityTypes.DRAKKAR wired");
        helper.succeed();
    }

    @GameTest
    public void shipItemsRegistered(GameTestHelper helper) {
        assertItemRegistered(helper, ModItems.SAIL, "smallships:sail");
        assertItemRegistered(helper, ModItems.CANNON, "smallships:cannon");
        assertItemRegistered(helper, ModItems.CANNON_BALL, "smallships:cannon_ball");
        helper.assertFalse(ModItems.COG_ITEMS.isEmpty(), "cog items populated");
        helper.assertFalse(ModItems.BRIGG_ITEMS.isEmpty(), "brigg items populated");
        helper.assertFalse(ModItems.GALLEY_ITEMS.isEmpty(), "galley items populated");
        helper.assertFalse(ModItems.DRAKKAR_ITEMS.isEmpty(), "drakkar items populated");
        for (Item item : ModItems.COG_ITEMS.values()) {
            assertItemRegistered(helper, item, "a cog ship item");
        }
        for (Item item : ModItems.BRIGG_ITEMS.values()) {
            assertItemRegistered(helper, item, "a brigg ship item");
        }
        for (Item item : ModItems.GALLEY_ITEMS.values()) {
            assertItemRegistered(helper, item, "a galley ship item");
        }
        for (Item item : ModItems.DRAKKAR_ITEMS.values()) {
            assertItemRegistered(helper, item, "a drakkar ship item");
        }
        helper.succeed();
    }

    @GameTest
    public void cogShipSpawnsAndStaysAlive(GameTestHelper helper) {
        helper.spawn(ModEntityTypes.COG, 1, 1, 1);
        helper.succeedWhenEntityPresent(ModEntityTypes.COG, 1, 1, 1);
    }

    @GameTest
    public void drakkarShipSpawnsAndStaysAlive(GameTestHelper helper) {
        helper.spawn(ModEntityTypes.DRAKKAR, 1, 1, 1);
        helper.succeedWhenEntityPresent(ModEntityTypes.DRAKKAR, 1, 1, 1);
    }
}
