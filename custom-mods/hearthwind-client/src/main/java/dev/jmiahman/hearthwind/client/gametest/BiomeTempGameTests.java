package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalTemperature;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * World-to-survival integration: teleport the player into a desert biome
 * and assert the temperature attachment rises (biome heat offset applied
 * by the survival temperature system). Skips gracefully (screenshot only)
 * when the world seed happens to have no desert within the search radius,
 * so biome RNG cannot fail CI.
 */
public class BiomeTempGameTests implements FabricClientGameTest {
    private static final int SLOW_TIMEOUT_TICKS = 20 * 300;
    private static final int SEARCH_RADIUS = 6400;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender(SLOW_TIMEOUT_TICKS);
            context.waitTicks(40);

            double tempBefore = temp(world);

            String desertPos = world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                var found = server.overworld().findClosestBiome3d(
                        h -> h.is(Biomes.DESERT), p.blockPosition(), SEARCH_RADIUS, 32, 64);
                if (found == null) {
                    return "";
                }
                BlockPos pos = found.getFirst();
                return pos.getX() + " " + pos.getY() + " " + pos.getZ();
            });

            if (desertPos.isEmpty()) {
                // No desert within range on this seed - record and pass.
                context.takeScreenshot("temperature_skipped_no_desert");
                return;
            }

            String[] parts = desertPos.split(" ");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]) + 15; // land above surface, short safe fall
            int z = Integer.parseInt(parts[2]);
            world.getServer().runCommand("tp @p " + x + " " + y + " " + z);
            context.waitTicks(240); // let the temperature system tick in the hot biome

            double tempAfter = temp(world);
            if (tempAfter <= tempBefore) {
                throw new AssertionError("temperature did not rise after teleporting into a desert: "
                        + tempBefore + " -> " + tempAfter);
            }

            context.takeScreenshot("temperature_desert");
        }
    }

    private static double temp(TestSingleplayerContext world) {
        Double v = world.getServer().computeOnServer(server -> {
            ServerPlayer p = server.getPlayerList().getPlayers().get(0);
            Double t = p.getAttached(HearthwindSurvivalTemperature.TEMPERATURE);
            return t == null ? 0.0D : t;
        });
        return v == null ? 0.0D : v;
    }
}
