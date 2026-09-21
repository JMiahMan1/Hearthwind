package dev.jmiahman.hearthwind.client.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Live proof that Athena CTM works in a real rendering client.
 *
 * <p>Background: athena just regained the {@code DefaultModels::init}
 * client entrypoint; a prior audit showed empty
 * {@code FactoryManager.LOADERS}, which meant every
 * {@code athena:ctm/pillar/giant/pane_ctm/carpet_ctm/pane_pillar/limited_pillar}
 * model (1,427 Chipped blockstates) fell back. This scenario:
 *
 * <ol>
 *   <li>asserts on the CLIENT that {@code FactoryManager} holds all 8
 *       loaders (root-cause proof that {@code DefaultModels::init} ran),
 *       via reflection so hearthwind-client keeps no compile dep on athena;
 *   <li>places one Chipped block per used loader type in-world (3x3 pads
 *       so CTM centres have neighbours; 3-high columns for pillar types;
 *       5-long line for panes; giant gets a 3x3 pad which covers the 2x2+
 *       requirement);
 *   <li>screenshots each island for seamless-vs-seams inspection;
 *   <li>best-effort scans the client log for {@code Unknown loader} /
 *       model-load failures and fails if any are present.
 * </ol>
 *
 * <p>Block IDs were verified against
 * {@code custom-mods/chipped/src/main/resources/assets/chipped/blockstates}
 * (all 7 exist with the expected {@code athena:loader} values).
 */
public class AthenaCtmGameTests implements FabricClientGameTest {
    // waitFor* timeout units are TICKS (20/s). Software-GL chunk rendering
    // under xvfb needs minutes, not the 10s defaults.
    private static final int SLOW_TIMEOUT_TICKS = 20 * 300;
    private static final int Y = 70;

    private record Island(String loader, String blockId, String shot, Kind kind) {}

    private enum Kind {
        PAD_3X3,
        COLUMN_3,
        LINE_5
    }

    private static final List<Island> ISLANDS = List.of(
            new Island("ctm", "chipped:bordered_smooth_stone", "athena_ctm", Kind.PAD_3X3),
            new Island("pillar", "chipped:fine_nether_bricks_pillar", "athena_pillar", Kind.COLUMN_3),
            new Island("giant", "chipped:massive_ice_bricks", "athena_giant", Kind.PAD_3X3),
            new Island("pane_ctm", "chipped:raster_blue_stained_glass_pane", "athena_pane_ctm", Kind.LINE_5),
            new Island("carpet_ctm", "chipped:harsh_quilted_purple_carpet", "athena_carpet_ctm", Kind.PAD_3X3),
            new Island("pane_pillar", "chipped:arched_blue_stained_glass_pane_pillar", "athena_pane_pillar", Kind.COLUMN_3),
            new Island("limited_pillar", "chipped:arched_lime_stained_glass_pillar", "athena_limited_pillar", Kind.COLUMN_3));

    private static void aimAt(ClientGameTestContext context, double aimX, double aimY, double aimZ) {
        double[] angles = context.computeOnClient(minecraft -> {
            net.minecraft.world.phys.Vec3 eye = minecraft.player.getEyePosition();
            double dx = aimX - eye.x;
            double dy = aimY - eye.y;
            double dz = aimZ - eye.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) Math.toDegrees(Math.atan2(-dy, horizontal));
            return new double[]{yaw, pitch};
        });
        context.getInput().lookAt((float) angles[0], (float) angles[1]);
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(minecraft -> {
            minecraft.options.renderDistance().set(4);
            minecraft.options.simulationDistance().set(4);
        });

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender(SLOW_TIMEOUT_TICKS);
            context.waitTicks(40);
            for (int i = 0; i < 3; i++) {
                context.setScreen(() -> null);
                context.waitTicks(10);
            }
            world.getServer().runCommand("time set 6000");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("gamerule doDaylightCycle false");
            // Let join chat (welcome / Terralith / advancement toast) fade so
            // the evidence screenshots show blocks, not chat.
            context.waitTicks(200);

            // ---- 1. loader registration proof (CLIENT side: DefaultModels::init
            // is a client entrypoint; the integrated server has no loaders). ----
            String loaderReport = context.computeOnClient(minecraft -> {
                try {
                    Class<?> fm = Class.forName("earth.terrarium.athena.api.client.models.FactoryManager");
                    java.lang.reflect.Method loadersMethod = fm.getMethod("loaders");
                    java.util.Collection<?> loaders =
                            (java.util.Collection<?>) loadersMethod.invoke(null);
                    java.lang.reflect.Method getMethod =
                            fm.getMethod("get", net.minecraft.resources.Identifier.class);
                    StringBuilder sb = new StringBuilder();
                    sb.append("count=").append(loaders.size()).append(";");
                    for (String name : new String[]{
                            "ctm", "carpet_ctm", "pane_ctm", "giant",
                            "mural", "pillar", "limited_pillar", "pane_pillar"}) {
                        Object loader = getMethod.invoke(
                                null,
                                net.minecraft.resources.Identifier.fromNamespaceAndPath("athena", name));
                        sb.append(name).append('=').append(loader == null ? "MISSING" : "ok").append(';');
                        if (loader == null) {
                            throw new AssertionError("athena loader missing: athena:" + name
                                    + " (DefaultModels::init did not run?)");
                        }
                    }
                    System.out.println("PROBE athena_loaders " + sb);
                    return sb.toString();
                } catch (AssertionError e) {
                    throw e;
                } catch (Exception e) {
                    throw new AssertionError("athena FactoryManager not reachable on client", e);
                }
            });
            System.out.println("PROBE athena_loaders(server-log) " + loaderReport);

            // ---- 2. verify every island block id is registered (fail loudly
            // on typos rather than placing air). ----
            String missing = world.getServer().computeOnServer(server -> {
                List<String> absent = new ArrayList<>();
                for (Island island : ISLANDS) {
                    if (!BuiltInRegistries.BLOCK.containsKey(
                            net.minecraft.resources.Identifier.parse(island.blockId()))) {
                        absent.add(island.blockId());
                    }
                }
                return String.join(",", absent);
            });
            if (!missing.isEmpty()) {
                throw new AssertionError("chipped blocks missing from registry: " + missing);
            }

            // ---- 3. place islands server-side (setBlock bypasses survival
            // placement rules). Each island gets a cleared air pocket (so
            // hills cannot bury the pad) plus a wide stone-brick apron that
            // doubles as the camera landing pad: the tour teleports the
            // player to (cx, 78, -6), which is INSIDE the apron, so physics
            // settles them onto the apron top (y=70) instead of free-falling
            // past the island to natural terrain (that fall is what aimed
            // the first version's cameras at the sky). ----
            Boolean placed = world.getServer().computeOnServer(server -> {
                ServerLevel level =
                        server.getPlayerList().getPlayers().get(0).level();
                for (int i = 0; i < ISLANDS.size(); i++) {
                    Island island = ISLANDS.get(i);
                    int cx = i * 20 + 10;
                    int cz = 0;
                    Block block = BuiltInRegistries.BLOCK.getValue(
                            net.minecraft.resources.Identifier.parse(island.blockId()));
                    for (int x = cx - 7; x <= cx + 7; x++) {
                        for (int z = cz - 7; z <= cz + 7; z++) {
                            for (int y = Y - 2; y <= Y + 12; y++) {
                                level.setBlock(new BlockPos(x, y, z),
                                        Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            }
                            level.setBlock(new BlockPos(x, Y - 1, z),
                                    Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                    switch (island.kind()) {
                        case PAD_3X3 -> {
                            for (int dx = -1; dx <= 1; dx++) {
                                for (int dz = -1; dz <= 1; dz++) {
                                    level.setBlock(new BlockPos(cx + dx, Y, cz + dz),
                                            block.defaultBlockState(), Block.UPDATE_ALL);
                                }
                            }
                        }
                        case COLUMN_3 -> {
                            for (int dy = 0; dy < 3; dy++) {
                                level.setBlock(new BlockPos(cx, Y + dy, cz),
                                        block.defaultBlockState(), Block.UPDATE_ALL);
                            }
                        }
                        case LINE_5 -> {
                            for (int dx = -2; dx <= 2; dx++) {
                                level.setBlock(new BlockPos(cx + dx, Y, cz),
                                        block.defaultBlockState(), Block.UPDATE_ALL);
                            }
                        }
                    }
                }
                return Boolean.TRUE;
            });
            if (!Boolean.TRUE.equals(placed)) {
                throw new AssertionError("athena island placement failed");
            }

            // ---- 4. tour the islands: teleport 1 block above the apron (a
            // longer drop kills a fresh 3-heart player and leaves every
            // later shot staring at the death screen), let physics settle,
            // aim, screenshot each. Aim heights frame the pad top (pads) or
            // mid-column (pillars) from the landed eye (~71.6). ----
            world.getServer().computeOnServer(server -> {
                net.minecraft.server.level.ServerPlayer p =
                        server.getPlayerList().getPlayers().get(0);
                p.setHealth(p.getMaxHealth());
                return Boolean.TRUE;
            });
            for (int i = 0; i < ISLANDS.size(); i++) {
                Island island = ISLANDS.get(i);
                int cx = i * 20 + 10;
                world.getServer().runCommand("tp @p " + cx + " 71 -6");
                world.getConnection().waitForChunksRender(20 * 45);
                context.waitTicks(40);
                double aimY = switch (island.kind()) {
                    case COLUMN_3 -> Y + 1.5D;
                    case LINE_5 -> Y + 0.5D;
                    case PAD_3X3 -> Y + 1.0D;
                };
                aimAt(context, cx, aimY, 1.0D);
                context.waitTicks(10);
                // Confirm the island centre is really our block (client view).
                final int fx = cx;
                String seen = context.computeOnClient(minecraft -> String.valueOf(
                        minecraft.level.getBlockState(new BlockPos(fx, Y, 0)).getBlock()));
                System.out.println("PROBE athena_island " + island.loader() + " centre=" + seen);
                Boolean alive = context.computeOnClient(
                        minecraft -> minecraft.player != null && minecraft.player.isAlive());
                if (!Boolean.TRUE.equals(alive)) {
                    throw new AssertionError("player dead before " + island.shot()
                            + " screenshot (a death-screen shot would be misleading)");
                }
                context.takeScreenshot(island.shot());
            }

            // ---- 5. best-effort client-log scan for loader/model failures.
            // The harness also greps the full log post-run; this in-test scan
            // fails fast when the log file is visible from the test JVM. ----
            Boolean logClean = context.computeOnClient(minecraft -> {
                java.nio.file.Path[] candidates = {
                        java.nio.file.Path.of(".tmp/logs/cgt-client.log"),
                        java.nio.file.Path.of("custom-mods/.tmp/logs/cgt-client.log"),
                        java.nio.file.Path.of("/work/repo/custom-mods/.tmp/logs/cgt-client.log"),
                };
                for (java.nio.file.Path p : candidates) {
                    try {
                        if (!java.nio.file.Files.isRegularFile(p)) {
                            continue;
                        }
                        String text = java.nio.file.Files.readString(p);
                        System.out.println("PROBE athena_logscan file=" + p
                                + " bytes=" + text.length());
                        for (String needle : new String[]{
                                "Unknown loader", "Failed to load model",
                                "Unable to load model", "missing model"}) {
                            if (text.contains(needle)) {
                                // Only fail on OUR namespaces / athena loaders;
                                // upstream mods ship their own broken refs.
                                for (String line : text.split("\n")) {
                                    if (line.contains(needle)
                                            && (line.contains("athena:")
                                                    || line.contains("chipped:"))) {
                                        throw new AssertionError(
                                                "client log reports model failure: " + line.trim());
                                    }
                                }
                            }
                        }
                        return Boolean.TRUE;
                    } catch (AssertionError e) {
                        throw e;
                    } catch (Exception e) {
                        System.out.println("PROBE athena_logscan skip " + p + ": " + e);
                    }
                }
                System.out.println("PROBE athena_logscan no-log-file-visible (post-run grep covers)");
                return Boolean.TRUE;
            });
            if (!Boolean.TRUE.equals(logClean)) {
                throw new AssertionError("athena client-log scan failed");
            }
        }
    }
}
