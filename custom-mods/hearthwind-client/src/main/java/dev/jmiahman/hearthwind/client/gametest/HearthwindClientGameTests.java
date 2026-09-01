package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.client.NutrientsKey;
import dev.jmiahman.hearthwind.client.NutrientsScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Client gametests for the hearthwind-client companion mod.
 *
 * <p>Runs under the fabric-client-gametest-api-v1 runner (system property
 * {@code -Dfabric.client.gametest}); each registered class is one test
 * executed against a real client. Screenshots land in the run's
 * {@code screenshots/} directory for artifact upload / local inspection.
 *
 * <p>NutrientsScreen scenario: boots a fresh singleplayer world, presses
 * the real {@code NutrientsKey.openNutrients} keymapping (N), waits for the
 * screen, screenshots it, then tears the world down.
 */
public class HearthwindClientGameTests implements FabricClientGameTest {
    // waitFor* timeout units are TICKS (20/s). Software-GL (xvfb/llvmpipe in
    // CI + docker) chunk rendering and screen transitions need minutes, not
    // the 10s (=200 tick) defaults.
    private static final int SLOW_TIMEOUT_TICKS = 20 * 300;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender(SLOW_TIMEOUT_TICKS);
            context.waitTicks(20);

            context.getInput().pressKey(NutrientsKey.openNutrients);
            context.waitFor(minecraft -> minecraft.gui.screen() instanceof NutrientsScreen, SLOW_TIMEOUT_TICKS);
            context.waitTicks(10);
            context.takeScreenshot("nutrients_screen");
        }
    }
}
