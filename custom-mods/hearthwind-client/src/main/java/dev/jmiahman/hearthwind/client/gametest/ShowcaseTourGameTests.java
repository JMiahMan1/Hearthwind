package dev.jmiahman.hearthwind.client.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Places one of every registered block from our namespaces on a grid and
 * screenshots each module band with a real rendering client, then fills
 * the inventory with a sample of our items and screenshots that too.
 * Any purple-black missing texture fails the run visually (screenshots)
 * and any unknown id fails loudly at placement time.
 *
 * Mirrors custom-mods/showcase (the RCON-driven equivalent for live dev
 * servers); the authoritative id lists live in the registries here.
 */
public class ShowcaseTourGameTests implements FabricClientGameTest {
    private static final int SLOW_TIMEOUT_TICKS = 20 * 300;
    private static final int Y = 70;
    private static final int COLS = 40;
    private static final int CELL = 2;

    // Module -> asset namespaces, first claim wins (mirrors gen_showcase.py).
    private static final Map<String, List<String>> MODULE_NS = new LinkedHashMap<>();

    static {
        MODULE_NS.put("survival", List.of("dehydration", "environmentz", "hearthwind", "hearthwind_survival"));
        MODULE_NS.put("jobs", List.of("hearthwind_jobs"));
        MODULE_NS.put("primitive", List.of("earlystage", "agedaddition", "hearthwind_primitive"));
        MODULE_NS.put("skills", List.of("hearthwind_skills", "levelz"));
        MODULE_NS.put("world", List.of("hearthwind_world", "naturalist", "adventurez", "antiqueatlas", "exposure", "inmis"));
        MODULE_NS.put("client", List.of("hearthwind_client"));
        MODULE_NS.put("meadow", List.of("meadow"));
        MODULE_NS.put("bakery", List.of("bakery"));
        MODULE_NS.put("brewery", List.of("brewery"));
        MODULE_NS.put("candlelight", List.of("candlelight"));
        MODULE_NS.put("farmcharm", List.of("farm_and_charm"));
        MODULE_NS.put("herbalbrews", List.of("herbalbrews"));
        MODULE_NS.put("vinery", List.of("vinery"));
        MODULE_NS.put("nethervinery", List.of("nethervinery"));
        MODULE_NS.put("smallships", List.of("smallships"));
    }

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

            // Place one of every block in our namespaces, banded per module.
            Map<String, List<String>> bands = world.getServer().computeOnServer(server -> {
                Map<String, List<String>> out = new LinkedHashMap<>();
                java.util.Set<String> claimed = new java.util.HashSet<>();
                for (Map.Entry<String, List<String>> e : MODULE_NS.entrySet()) {
                    List<String> ids = new ArrayList<>();
                    for (String ns : e.getValue()) {
                        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
                            if (!entry.getKey().identifier().getNamespace().equals(ns)) {
                                continue;
                            }
                            String id = entry.getKey().identifier().toString();
                            if (claimed.add(id)) {
                                ids.add(id);
                            }
                        }
                    }
                    out.put(e.getKey(), ids);
                }
                return out;
            });

            int total = bands.values().stream().mapToInt(List::size).sum();
            if (total == 0) {
                throw new AssertionError("no showcase blocks found in registries");
            }

            int band = 0;
            List<String> shotBands = new ArrayList<>();
            for (Map.Entry<String, List<String>> e : bands.entrySet()) {
                List<String> ids = e.getValue();
                if (ids.isEmpty()) {
                    continue;
                }
                int z0 = band * 80;
                int rows = Math.max(1, (ids.size() + COLS - 1) / COLS);
                Boolean ok = world.getServer().computeOnServer(server -> {
                    ServerLevel level = server.getPlayerList().getPlayers().get(0).level();
                    for (int i = 0; i < ids.size(); i++) {
                        Block block = BuiltInRegistries.BLOCK.getValue(
                                net.minecraft.resources.Identifier.parse(ids.get(i)));
                        BlockPos pos = new BlockPos((i % COLS) * CELL + 1, Y,
                                z0 + (i / COLS) * CELL + 1);
                        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    // stone-brick apron under the band
                    for (int x = 0; x <= COLS * CELL; x++) {
                        for (int z = 0; z <= rows * CELL + 8; z++) {
                            level.setBlock(new BlockPos(x, Y - 1, z0 + z),
                                    net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(),
                                    Block.UPDATE_ALL);
                        }
                    }
                    return Boolean.TRUE;
                });
                if (!Boolean.TRUE.equals(ok)) {
                    throw new AssertionError("showcase placement failed for band " + e.getKey());
                }
                shotBands.add(e.getKey() + ":" + z0);
                band++;
            }

            // Tour the bands: teleport above each, let chunks catch up,
            // aim down-grid, screenshot.
            for (String spec : shotBands) {
                String[] parts = spec.split(":");
                int z0 = Integer.parseInt(parts[1]);
                world.getServer().runCommand("tp @p 40 88 " + (z0 - 16));
                world.getConnection().waitForChunksRender(20 * 45);
                aimAt(context, 40.0D, Y, z0 + 24.0D);
                context.waitTicks(10);
                context.takeScreenshot("showcase_" + parts[0]);
            }

            // Item sample: first 36 registered items across our namespaces
            // into the inventory, open it, screenshot.
            world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                p.getInventory().clearContent();
                int slot = 0;
                java.util.Set<String> seen = new java.util.HashSet<>();
                for (var entry : BuiltInRegistries.ITEM.entrySet()) {
                    String ns = entry.getKey().identifier().getNamespace();
                    boolean ours = MODULE_NS.values().stream().flatMap(List::stream).anyMatch(ns::equals);
                    if (!ours || !seen.add(entry.getKey().identifier().toString())) {
                        continue;
                    }
                    p.getInventory().setItem(slot++, new ItemStack(entry.getValue(), 1));
                    if (slot >= 36) {
                        break;
                    }
                }
                return Boolean.TRUE;
            });
            context.waitTicks(10);
            context.getInput().pressKey(GLFW.GLFW_KEY_E);
            context.waitTicks(15);
            context.takeScreenshot("showcase_items");
            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitTicks(5);
        }
    }
}
