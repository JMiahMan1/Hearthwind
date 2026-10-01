package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.client.ClientSkillGates;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;

/**
 * Boots a DEDICATED server from the same mod set and connects the real
 * client to it. This exercises the full client/server registry + network
 * negotiation path - the exact failure class that shipped stale-jar
 * registry mismatches (vinery "8 unknown registry entries") in live play.
 */
public class PackServerConnectGameTests implements FabricClientGameTest {
    private static final int SLOW_TIMEOUT_TICKS = 20 * 300;
    /**
     * fabric's {@code TestDedicatedServerContext} gives the dedicated server a
     * short fixed window to reach "Done" and throws a TimeoutException if it
     * does not. On a loaded host that window is regularly missed, which failed
     * this entrypoint - and because the harness stops at the first failing
     * entrypoint, it also meant every later test never ran. Retrying the boot
     * is the fix: the first attempt pays the cost of worldgen while the host
     * is still busy, the next ones find a warm world.
     */
    private static final int BOOT_ATTEMPTS = 3;

    @Override
    public void runTest(ClientGameTestContext context) {
        // The framework's dedicated server defaults to online-mode; a headless
        // offline client cannot fetch player certificates (401), so force it off.
        java.util.Properties props = new java.util.Properties();
        props.setProperty("online-mode", "false");

        RuntimeException last = null;
        for (int attempt = 1; attempt <= BOOT_ATTEMPTS; attempt++) {
            try (TestDedicatedServerContext server = context.worldBuilder().createServer(props)) {
                TestServerConnection connection = server.connect();
                connection.waitForChunksRender(SLOW_TIMEOUT_TICKS);
                context.waitTicks(40);

                int players = server.computeOnServer(ms -> ms.getPlayerList().getPlayerCount());
                if (players != 1) {
                    throw new AssertionError("connected player count = " + players + " (expected 1)");
                }

                if (!ClientSkillGates.hasServerSnapshot()) {
                    throw new AssertionError("skill gate snapshot was not received after connection");
                }
                var stoneGate = ClientSkillGates.getBreakRequirement(
                        net.minecraft.world.level.block.Blocks.STONE);
                var furnaceGate = ClientSkillGates.getUseRequirement(
                        net.minecraft.world.level.block.Blocks.FURNACE);
                if (stoneGate == null || !stoneGate.skill().equals("mining") || stoneGate.level() != 5) {
                    throw new AssertionError("client stone gate mismatch: " + stoneGate);
                }
                if (furnaceGate == null || !furnaceGate.skill().equals("smithing") || furnaceGate.level() != 3) {
                    throw new AssertionError("client furnace gate mismatch: " + furnaceGate);
                }

                context.takeScreenshot("pack_server_gate_sync");
                last = null;
                break;
            } catch (AssertionError e) {
                // A real assertion failed. Say so rather than retrying it away.
                throw e;
            } catch (RuntimeException e) {
                last = e;
                System.err.println("pack server connect attempt " + attempt + " of " + BOOT_ATTEMPTS
                        + " failed to boot: " + e);
            }
        }
        if (last != null) {
            throw new AssertionError("the dedicated server never finished booting after "
                    + BOOT_ATTEMPTS + " attempts", last);
        }
        // The harness asserts the client ends on the title screen with no
        // server connected; closing the dedicated server is asynchronous and
        // a slow CI runner loses that race otherwise. The client lands on the
        // disconnect screen for a remote server (it does not auto-return like
        // a singleplayer world), so go back to the title screen the same way
        // the screen's button does.
        context.waitFor(minecraft -> minecraft.getConnection() == null, SLOW_TIMEOUT_TICKS);
        context.setScreen(net.minecraft.client.gui.screens.TitleScreen::new);
        context.waitForScreen(net.minecraft.client.gui.screens.TitleScreen.class);
    }
}
