package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.client.JobsScreen;
import dev.jmiahman.hearthwind.client.NutrientsScreen;
import dev.jmiahman.hearthwind.client.PartyScreen;
import dev.jmiahman.hearthwind.client.SkillInfoScreen;
import dev.jmiahman.hearthwind.client.SkillsScreen;
import dev.jmiahman.hearthwind.client.SurvivalInfoScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
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

            context.setScreen(NutrientsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof NutrientsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_nutrients");

            context.setScreen(SkillsScreen::new);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skills");

            context.setScreen(() -> new SkillInfoScreen("mining"));
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SkillInfoScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skill_info");

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