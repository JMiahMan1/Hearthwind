package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.client.JobsScreen;
import dev.jmiahman.hearthwind.client.NutrientsScreen;
import dev.jmiahman.hearthwind.client.PartyScreen;
import dev.jmiahman.hearthwind.client.SkillInfoScreen;
import dev.jmiahman.hearthwind.client.SkillRestrictionScreen;
import dev.jmiahman.hearthwind.client.SkillsScreen;
import dev.jmiahman.hearthwind.client.SurvivalInfoScreen;
import dev.jmiahman.hearthwind.client.TabStrip;
import dev.jmiahman.hearthwind.client.WelcomeScreen;
import dev.jmiahman.hearthwind.survival.StarterKit;
import draylar.inmis.client.InmisBackpackLayer;
import draylar.inmis.mixin.client.LivingEntityRendererInvoker;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Tours every Hearthwind UI surface in one pass against a real client:
 * inventory (E), Nutrients (N), Skills (K), Jobs (J), Party (P) - one
 * screenshot each. Catches screen/keybind/registration regressions across
 * the whole client mod set in a single headless run.
 *
 * <p>Every screen is driven through {@link ClientGameTestContext#setScreen}
 * rather than the keybinds: the fabric-gametest keypress plumbing
 * (holdKey -&gt; WindowMixin -&gt; KeyboardHandler -&gt; KeyMapping.click) does
 * not reliably deliver custom keybinds under headless software GL, so
 * keybind-driven screen transitions time out even though the screens
 * themselves are fine.
 */
public class ScreensTourGameTests implements FabricClientGameTest {
    private static final int SLOW_TIMEOUT_TICKS = 20 * 300;

    @Override
    public void runTest(ClientGameTestContext context) {
        // Capture the Aged/FancyMenu title-screen layout before entering a
        // world - first-impression parity evidence for every run.
        context.waitForScreen(net.minecraft.client.gui.screens.TitleScreen.class);
        context.waitTicks(40);
        context.takeScreenshot("tour_title");

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender(SLOW_TIMEOUT_TICKS);
            context.waitTicks(40);

            // Aged's welcome screen: it opens on a first join and its Start
            // button is what hands over the starter loadout, so this pass
            // covers the whole path - screen, button, five hotbar slots.
            // The tour asks the server for a deterministic first join (a
            // session that joined a world earlier already has the starter tag,
            // so the screen would legitimately stay closed). waitFor fails the
            // test on timeout.
            world.getServer().computeOnServer(server -> {
                net.minecraft.server.level.ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                StarterKit.resetAndOfferWelcomeScreen(p);
                return Boolean.TRUE;
            });
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof WelcomeScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_welcome");
            context.clickScreenButton("screen.hearthwind.welcome.start");
            context.waitFor(minecraft -> minecraft.gui.screen() == null, SLOW_TIMEOUT_TICKS);
            // Twenty ticks is far short of the server's 60-tick fallback, and
            // in singleplayer the two tick together: the loadout can only be in
            // the hotbar this early if the Start button's payload did it.
            context.waitTicks(20);
            for (int slot : new int[] {StarterKit.SLOT_BREAD, StarterKit.SLOT_APPLE, StarterKit.SLOT_GUIDE,
                    StarterKit.SLOT_PURIFIED_WATER, StarterKit.SLOT_CAMPFIRE}) {
                int count = world.getServer().computeOnServer(server -> {
                    net.minecraft.server.level.ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                    return p.getInventory().getItem(slot).getCount();
                });
                if (count == 0) {
                    throw new AssertionError("Start left hotbar slot " + slot + " empty");
                }
            }
            closeAnyScreen(context);

            // Drive every screen through context.setScreen: the fabric-gametest
            // keypress plumbing (holdKey -> WindowMixin -> KeyboardHandler ->
            // KeyMapping.click) does not reliably deliver custom keybinds under
            // headless software GL, so keybind-driven screen transitions time
            // out even though the screens themselves are fine.
            Player player = context.computeOnClient(minecraft -> minecraft.player);
            context.setScreen(() -> new InventoryScreen(player));
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof InventoryScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_inventory");

            // Inmis parity: a chest-slot backpack renders on the player's
            // back. Equip one client-side, assert the layer is registered on
            // the avatar renderer, then spin the inventory preview away from
            // the camera so the screenshot shows the back.
            context.runOnClient(minecraft -> minecraft.player.setItemSlot(EquipmentSlot.CHEST,
                    new ItemStack(BuiltInRegistries.ITEM.get(Identifier.parse("inmis:frayed_backpack"))
                            .orElseThrow().value())));
            context.waitTicks(5);
            context.runOnClient(minecraft -> {
                var renderer = minecraft.getEntityRenderDispatcher().getRenderer(minecraft.player);
                if (!(renderer instanceof LivingEntityRendererInvoker invoker)
                        || invoker.inmis$layers().stream().noneMatch(layer -> layer instanceof InmisBackpackLayer)) {
                    throw new AssertionError("the Inmis backpack layer must be registered on the avatar renderer");
                }
                if (minecraft.gui.screen() instanceof InventoryScreen screen) {
                    screen.mouseMoved(20, minecraft.getWindow().getGuiScaledHeight() / 2.0);
                }
            });
            context.waitTicks(10);
            context.takeScreenshot("tour_backpack_on_back");

            // BackSlot parity: the back and belt slots render their items on
            // the avatar and in the HUD. Fill both client-side, assert the
            // layers are registered, and screenshot the inventory (the two
            // extra slots sit right of the shield slot at Aged's positions).
            context.runOnClient(minecraft -> {
                net.backslot.BackSlotSlots.set(minecraft.player, net.backslot.BackSlot.BACK_SLOT,
                        new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
                net.backslot.BackSlotSlots.set(minecraft.player, net.backslot.BackSlot.BELT_SLOT,
                        new ItemStack(net.minecraft.world.item.Items.LANTERN));
            });
            context.waitTicks(5);
            context.runOnClient(minecraft -> {
                var renderer = minecraft.getEntityRenderDispatcher().getRenderer(minecraft.player);
                if (!(renderer instanceof net.backslot.mixin.client.AvatarRendererInvoker invoker)
                        || invoker.backslot$layers().stream()
                                .noneMatch(layer -> layer instanceof net.backslot.client.BackItemLayer)
                        || invoker.backslot$layers().stream()
                                .noneMatch(layer -> layer instanceof net.backslot.client.BeltItemLayer)) {
                    throw new AssertionError("the BackSlot layers must be registered on the avatar renderer");
                }
                if (minecraft.gui.screen() instanceof InventoryScreen screen) {
                    screen.mouseMoved(20, minecraft.getWindow().getGuiScaledHeight() / 2.0);
                }
            });
            context.waitTicks(10);
            context.takeScreenshot("tour_backslot");

            // BackSlot Addon parity: a shield on the back and a sword on the
            // belt get their own hip-mounted transforms.
            context.runOnClient(minecraft -> {
                net.backslot.BackSlotSlots.set(minecraft.player, net.backslot.BackSlot.BACK_SLOT,
                        new ItemStack(net.minecraft.world.item.Items.SHIELD));
                net.backslot.BackSlotSlots.set(minecraft.player, net.backslot.BackSlot.BELT_SLOT,
                        new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
            });
            context.waitTicks(10);
            context.takeScreenshot("tour_backslot_addon");

            context.setScreen(NutrientsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof NutrientsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_nutrients");

            context.setScreen(SkillsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skills");

            // Aged hub parity: the right rail's attributes slide-out and the
            // mining/crafting gate buttons.
            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof SkillsScreen screen) {
                    screen.toggleAttributes();
                }
            });
            context.waitTicks(5);
            context.takeScreenshot("tour_skills_attributes");

            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof SkillsScreen screen) {
                    screen.openRestrictions(SkillRestrictionScreen.ListKind.MINING);
                }
            });
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillRestrictionScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skill_restrictions_mining");

            context.setScreen(SkillsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillsScreen, SLOW_TIMEOUT_TICKS);
            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof SkillsScreen screen) {
                    screen.openRestrictions(SkillRestrictionScreen.ListKind.CRAFTING);
                }
            });
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillRestrictionScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skill_restrictions_crafting");

            context.setScreen(SkillsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);

            context.setScreen(() -> new SkillInfoScreen("mining"));
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillInfoScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skill_info");

            // The unlock list scrolls through every level (Aged's scrollable
            // info page); jump a few rows down and confirm the screen keeps
            // rendering with the slider thumb moved.
            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof SkillInfoScreen screen) {
                    screen.scrollTo(4);
                }
            });
            context.waitTicks(5);
            context.takeScreenshot("tour_skill_info_scrolled");

            context.setScreen(SkillsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);

            context.setScreen(JobsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof JobsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            // Aged's panel chrome: the four LibZ tabs sit INSIDE the 20 px
            // black band at the top of the panel, so a click in the band picks
            // a tab and a click just below the face edge must not.
            context.runOnClient(minecraft -> {
                if (!(minecraft.gui.screen() instanceof JobsScreen screen)) {
                    throw new AssertionError("the Jobs screen should be open for the tab band check");
                }
                int left = screen.panelLeft();
                int top = screen.panelTop();
                if (TabStrip.clickedInBand(left + 4, top + 6, left, top, TabStrip.Tab.JOBS)
                        != TabStrip.Tab.INVENTORY) {
                    throw new AssertionError("the first tab must be clickable inside the panel band");
                }
                if (TabStrip.clickedInBand(left + 4, top + 30, left, top, TabStrip.Tab.JOBS) != null) {
                    throw new AssertionError("a click below the tab band must not hit a tab");
                }
            });
            context.takeScreenshot("tour_jobs");

            context.setScreen(PartyScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof PartyScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_party");

            context.setScreen(() -> new SurvivalInfoScreen(SurvivalInfoScreen.Kind.THIRST));
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SurvivalInfoScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_thirst");

            context.setScreen(() -> new SurvivalInfoScreen(SurvivalInfoScreen.Kind.TEMPERATURE));
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SurvivalInfoScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_temperature");

            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitTicks(5);
        }
    }

    private static void closeAnyScreen(ClientGameTestContext context) {
        for (int i = 0; i < 3 && Boolean.TRUE.equals(
                context.computeOnClient(minecraft -> minecraft.gui.screen() != null)); i++) {
            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitTicks(5);
        }
        context.waitFor(minecraft -> minecraft.gui.screen() == null, 100);
    }
}