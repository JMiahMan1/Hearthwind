package dev.jmiahman.hearthwind.client.gametest;

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

    @Override
    public void runTest(ClientGameTestContext context) {
        // The framework's dedicated server defaults to online-mode; a headless
        // offline client cannot fetch player certificates (401), so force it off.
        java.util.Properties props = new java.util.Properties();
        props.setProperty("online-mode", "false");
        try (TestDedicatedServerContext server = context.worldBuilder().createServer(props)) {
            TestServerConnection connection = server.connect();
            connection.waitForChunksRender(SLOW_TIMEOUT_TICKS);
            context.waitTicks(40);

            int players = server.computeOnServer(ms -> ms.getPlayerList().getPlayerCount());
            if (players != 1) {
                throw new AssertionError("connected player count = " + players + " (expected 1)");
            }

            context.takeScreenshot("pack_server_connected");
        }
    }
}
