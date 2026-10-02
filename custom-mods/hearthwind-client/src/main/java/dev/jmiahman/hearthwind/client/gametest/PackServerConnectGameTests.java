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
     * is the fix: the first attempt pays the cost of loading 150-odd mods
     * server-side, the next ones find a warm JVM.
     */
    private static final int BOOT_ATTEMPTS = 4;

    /**
     * Ticks to wait after a failed attempt before starting the next one.
     *
     * <p>This is load-bearing, and it was the bug in the first version of this
     * retry loop. {@code TestDedicatedServerContext#close} stops the server but
     * does not wait for it to finish, so an immediate retry races the previous
     * server's shutdown and dies with {@code IllegalStateException: Cannot
     * create a server when a server is running} - which cannot ever succeed, so
     * a flaky boot became a hard failure. Measured on a loaded host: attempt 1
     * gave up at 02:40:09 while its own server logged {@code Starting minecraft
     * server} at 02:40:10 and reached {@code Done (7.552s)} at 02:40:18, so the
     * server really was booting fine and only needed a moment longer. A
     * shutdown of this pack takes about 5 s; 100 ticks is a wide margin.
     */
    private static final int BOOT_SETTLE_TICKS = 100;

    @Override
    public void runTest(ClientGameTestContext context) {
        // The framework's dedicated server defaults to online-mode; a headless
        // offline client cannot fetch player certificates (401), so force it off.
        java.util.Properties props = new java.util.Properties();
        props.setProperty("online-mode", "false");
        // Disable the vanilla server watchdog. fabric runs this dedicated server
        // INSIDE THE CLIENT'S JVM, so the client's own work - most visibly the
        // 8192x8192 block-atlas uploads - starves the server tick loop and the
        // watchdog concludes the server has hung and kills it. Measured on a
        // loaded host: "A single server tick took N seconds (should be max 60)"
        // with the render thread sitting in
        // TextureAtlas.uploadAnimationFrames, i.e. a loaded host, not a hung
        // server. DedicatedServer only starts the watchdog when
        // getMaxTickLength() > 0, so -1 turns it off outright - and a test
        // server has no business being watchdog-killed.
        props.setProperty("max-tick-time", "-1");

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
                if (attempt < BOOT_ATTEMPTS) {
                    abortLeakedServer();
                    context.waitTicks(BOOT_SETTLE_TICKS);
                }
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

    /**
     * Kill a dedicated server that fabric leaked when its 10-second boot window
     * expired, so the next attempt can actually start one.
     *
     * <p>This is the other half of {@link #BOOT_SETTLE_TICKS}, and without it a
     * retry can never succeed. {@code DedicatedServerImplUtil#start} does
     * {@code serverFuture.get(10, SECONDS)} - a hardcoded window, not a
     * property, and this pack needs far longer than 10 s to load server-side -
     * and on timeout it leaves the server thread RUNNING and
     * {@code serverFuture} still populated. The next attempt then dies with
     * vanilla's {@code IllegalStateException: Server is already running} before
     * it can even try. So: halt the leaked server, clear the static future and
     * fabric's {@code isServerRunning} flag, then let the caller settle.
     *
     * <p>Reflection into fabric internals is normally wrong, but this is a test
     * harness driving a harness, and the alternative is an entrypoint that can
     * never pass on a slow host. Best-effort: if the shape ever changes, the
     * next attempt simply fails the way it did before and the settle still
     * gives a clean shutdown time.
     */
    private static void abortLeakedServer() {
        try {
            Class<?> util = Class.forName(
                    "net.fabricmc.fabric.impl.client.gametest.util.DedicatedServerImplUtil");
            java.lang.reflect.Field future = util.getDeclaredField("serverFuture");
            future.setAccessible(true);
            Object pending = future.get(null);
            if (pending instanceof java.util.concurrent.CompletableFuture<?> cf
                    && cf.getNow(null) instanceof net.minecraft.server.MinecraftServer leaked) {
                System.err.println("pack server connect: halting the server fabric leaked after the "
                        + "boot window expired");
                leaked.halt(true);
            }
            future.set(null, null);

            Class<?> threading = Class.forName(
                    "net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl");
            java.lang.reflect.Field running = threading.getDeclaredField("isServerRunning");
            running.setAccessible(true);
            running.setBoolean(null, false);
        } catch (ReflectiveOperationException | RuntimeException e) {
            System.err.println("pack server connect: could not clean up the leaked server: " + e);
        }
    }
}
