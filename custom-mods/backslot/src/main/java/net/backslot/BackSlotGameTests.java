package net.backslot;

import net.backslot.slot.BackItemSlot;
import net.backslot.slot.BeltItemSlot;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BackSlotGameTests {

    public BackSlotGameTests() {}

    @GameTest
    public void attachmentsRoundTrip(GameTestHelper helper) {
        Player player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(BackSlotSlots.get(player, BackSlot.BACK_SLOT).isEmpty(), "back starts empty");
        helper.assertTrue(BackSlotSlots.get(player, BackSlot.BELT_SLOT).isEmpty(), "belt starts empty");

        BackSlotSlots.set(player, BackSlot.BACK_SLOT, new ItemStack(Items.SHIELD));
        BackSlotSlots.set(player, BackSlot.BELT_SLOT, new ItemStack(Items.LANTERN));
        helper.assertTrue(BackSlotSlots.get(player, BackSlot.BACK_SLOT).is(Items.SHIELD), "back holds the shield");
        helper.assertTrue(BackSlotSlots.get(player, BackSlot.BELT_SLOT).is(Items.LANTERN), "belt holds the lantern");
        helper.succeed();
    }

    @GameTest
    public void switchingSwapsHeldAndStored(GameTestHelper helper) {
        Player player = helper.makeMockServerPlayerInLevel();
        BackSlotSlots.set(player, BackSlot.BACK_SLOT, new ItemStack(Items.IRON_SWORD));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.STONE, 7));

        BackSlotNetworking.switchSlot(player, BackSlot.BACK_SLOT);

        helper.assertTrue(player.getMainHandItem().is(Items.IRON_SWORD),
                "the stored sword moves to the hand");
        helper.assertTrue(BackSlotSlots.get(player, BackSlot.BACK_SLOT).is(Items.STONE),
                "the held stone moves to the back slot");
        helper.succeed();
    }

    @GameTest
    public void inventoryMenuCarriesBothSlots(GameTestHelper helper) {
        Player player = helper.makeMockServerPlayerInLevel();
        InventoryMenu menu = new InventoryMenu(player.getInventory(), true, player);
        helper.assertTrue(menu.slots.size() == 50,
                "26.2 InventoryMenu has 48 slots; back and belt append two more, got " + menu.slots.size());
        var back = menu.slots.get(menu.slots.size() - 2);
        var belt = menu.slots.get(menu.slots.size() - 1);
        helper.assertTrue(back instanceof BackItemSlot, "the second-to-last slot is the back slot");
        helper.assertTrue(belt instanceof BeltItemSlot, "the last slot is the belt slot");
        helper.assertTrue(back.x == 77 + BackSlot.CONFIG.backSlotX && back.y == 44 + BackSlot.CONFIG.backSlotY,
                "back slot sits at the configured position");
        helper.assertTrue(belt.x == 77 + BackSlot.CONFIG.beltSlotX && belt.y == 26 + BackSlot.CONFIG.beltSlotY,
                "belt slot sits at the configured position");
        helper.succeed();
    }

    @GameTest
    public void configDefaultsMatchUpstream(GameTestHelper helper) {
        BackSlotConfig defaults = new BackSlotConfig();
        helper.assertTrue(defaults.backslotScaling == 1.0f && defaults.beltslotScaling == 1.0f,
                "upstream scaling defaults are 1.0");
        helper.assertTrue(defaults.dropHolding, "dropHolding defaults true");
        helper.assertTrue(!defaults.disableBackslotHud, "the HUD shows by default");
        helper.succeed();
    }
}
