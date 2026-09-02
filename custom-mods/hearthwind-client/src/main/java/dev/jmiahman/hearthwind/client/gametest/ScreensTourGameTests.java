package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.client.NutrientsKey;
import dev.jmiahman.hearthwind.client.NutrientsScreen;
import dev.jmiahman.hearthwind.client.SurvivalInfoScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.lwjgl.glfw.GLFW;

/**
 * Tours every Hearthwind UI surface in one pass against a real client:
 * inventory (E), Nutrients (N), Skills (K), Jobs (J), Party (P) - one
 * screenshot each. Catches screen/keybind/registration regressions across
 * the whole client mod set in a single headless run.
 */
public class ScreensTourGameTests implements FabricClientGameTest {
    private static final int SLOW_TIMEOUT_TICKS = 20 * 300;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender(SLOW_TIMEOUT_TICKS);
            context.waitTicks(40);
            closeAnyScreen(context);

            // Inventory
            context.getInput().pressKey(GLFW.GLFW_KEY_E);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof InventoryScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_inventory");
            context.getInput().pressKey(GLFW.GLFW_KEY_E);
            context.waitTicks(5);

            // Nutrients
            context.getInput().pressKey(NutrientsKey.openNutrients);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof NutrientsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_nutrients");

            // Skills tab (survival info screen replaces the current one)
            context.getInput().pressKey(NutrientsKey.openSkills);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SurvivalInfoScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_skills");

            // Jobs tab
            context.getInput().pressKey(NutrientsKey.openJobs);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SurvivalInfoScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_jobs");

            // Party tab
            context.getInput().pressKey(NutrientsKey.openParty);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof SurvivalInfoScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("tour_party");

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
