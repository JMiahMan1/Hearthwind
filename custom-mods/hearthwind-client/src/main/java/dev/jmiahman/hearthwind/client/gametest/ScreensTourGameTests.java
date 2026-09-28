package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.client.JobsScreen;
import dev.jmiahman.hearthwind.client.NutrientsScreen;
import dev.jmiahman.hearthwind.client.PartyScreen;
import dev.jmiahman.hearthwind.client.SkillInfoScreen;
import dev.jmiahman.hearthwind.client.SkillRestrictionScreen;
import dev.jmiahman.hearthwind.client.SkillsScreen;
import dev.jmiahman.hearthwind.client.SurvivalInfoScreen;
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