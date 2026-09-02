package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.jobs.JobState;
import dev.jmiahman.hearthwind.skills.SkillXp;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

/**
 * Mining progression loop across three mods at once, split into two
 * complementary passes:
 *
 * 1. REAL INPUT PATH: place an earlystage:rock mound (a deliberate one-hit
 *    break by design), mine it through the real input pipeline (look +
 *    hold attack), then assert on the SERVER that SkillXp recorded mining
 *    XP. Guards the client input -> server break -> skills XP hook chain
 *    without depending on slow progressive mining (coal_ore needs ~2s of
 *    continuous dig packets; under the heavily loaded gametest world the
 *    fake-input hold does not converge reliably).
 *
 * 2. AWARD PLUMBING PATH: join the miner job, place a coal_ore (a miner
 *    level-1 block) and break it server-side through the player's real
 *    ServerPlayerGameMode.destroyBlock, then assert that BOTH the skills
 *    attachment (mining XP) and the jobs attachment (miner job XP) moved.
 *    Exercises the same PlayerBlockBreakEvents.AFTER hooks the real break
 *    would fire, with a real survival player, real block and real job.
 */
public class MiningGameTests implements FabricClientGameTest {
    private static final int SLOW_TIMEOUT_TICKS = 20 * 60;

    /**
     * Rotates the player so the crosshair points at an arbitrary point
     * (used for non-cube block shapes like the earlystage rock pebble).
     */
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
        // Keep the gametest world cheap (full pack on one docker core can
        // otherwise load thousands of chunks and starve the server thread).
        context.runOnClient(minecraft -> {
            minecraft.options.renderDistance().set(4);
            minecraft.options.simulationDistance().set(4);
        });

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender(SLOW_TIMEOUT_TICKS);
            context.waitTicks(40);

            // Defensive: close any auto-opened screen (guide book can swallow input)
            for (int i = 0; i < 3; i++) {
                context.setScreen(() -> null);
                context.waitTicks(10);
            }

            BlockPos base = context.computeOnClient(minecraft -> minecraft.player.blockPosition());
            String setblockPrefix = "execute as @p run setblock "
                    + (base.getX() + 2) + " " + base.getY() + " " + (base.getZ() + 2) + " ";

            // ---- Pass 1: real-input instant break of earlystage:rock ----
            world.getServer().runCommand(setblockPrefix + "earlystage:rock");
            context.waitTicks(10);

            BlockPos rockPos = base.offset(2, 0, 2);
            Boolean placed = context.computeOnClient(minecraft ->
                    !minecraft.level.getBlockState(rockPos).isAir());
            if (!Boolean.TRUE.equals(placed)) {
                throw new AssertionError("setblock did not place earlystage:rock at "
                        + rockPos.toShortString() + "; block is still air");
            }

            // Aim at the pebble BODY (HearthwindRockBlock.SHAPE occupies y
            // 0..5/16 of the cell), not the cell center - lookAt(BlockPos)
            // aims 0.5 up which is above the 5px-tall shape and the crosshair
            // ray misses entirely.
            aimAt(context, rockPos.getX() + 0.5D, rockPos.getY() + 0.15D, rockPos.getZ() + 0.5D);
            context.waitTicks(5);
            // Mine through client-context ticks ONLY: server-context waitFor
            // advances the integrated server without the client tick loop, so
            // the fake-input dig never processes there. Retry a few times in
            // case the first press is consumed by cursor-grab handling.
            boolean broke = false;
            for (int attempt = 0; attempt < 5 && !broke; attempt++) {
                context.getInput().holdMouse(0);
                context.waitTicks(30);
                context.getInput().releaseMouse(0);
                context.waitTicks(5);
                broke = world.getServer().computeOnServer(server -> {
                    ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                    return p.level().getBlockState(rockPos).isAir();
                });
            }
            if (!broke) {
                throw new AssertionError("earlystage:rock still present at " + rockPos.toShortString()
                        + " after 5 mining attempts");
            }

            double rockMiningXp = world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                Map<String, Double> xp = p.getAttached(SkillXp.XP);
                return xp == null ? 0.0D : xp.getOrDefault("mining", 0.0D);
            });
            if (rockMiningXp <= 0.0D) {
                throw new AssertionError("no mining XP recorded after breaking earlystage:rock "
                        + "through the real input pipeline");
            }

            // ---- Pass 2: job join + coal_ore award plumbing ----
            world.getServer().runCommand("execute as @p run job join miner");
            context.waitTicks(10);

            String jobId = world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                return JobState.jobId(p);
            });
            if (!"miner".equals(jobId)) {
                throw new AssertionError("job join failed, jobId=" + jobId);
            }

            // StarterKit fills slots 0-2 (guide book, bottle, campfire) on join;
            // give the miner a pickaxe so the break looks like a real mining action.
            world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                p.getInventory().clearContent();
                return Boolean.TRUE;
            });
            world.getServer().runCommand("give @p minecraft:wooden_pickaxe 1");
            context.getInput().pressKey(GLFW.GLFW_KEY_1); // slot 0
            context.waitTicks(5);

            String held = context.computeOnClient(minecraft -> minecraft.player == null
                    ? "no-player"
                    : String.valueOf(minecraft.player.getMainHandItem().getItem()));
            if (!"minecraft:wooden_pickaxe".equals(held)) {
                throw new AssertionError("held item is " + held + " after give+select (expected wooden_pickaxe)");
            }

            BlockPos orePos = base.offset(-2, 1, -2);
            world.getServer().runCommand("execute as @p run setblock "
                    + orePos.getX() + " " + orePos.getY() + " " + orePos.getZ() + " minecraft:coal_ore");
            context.waitTicks(10);

            // Gate arc: an under-leveled player must be DENIED the break by
            // the skills gate; granting the required skill level must UNLOCK
            // it, and the successful break must award both skill and job XP.
            var gateInfo = world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                dev.jmiahman.hearthwind.skills.SkillGates.Gate gate =
                        dev.jmiahman.hearthwind.skills.SkillGates.breakGate(p.level().getBlockState(orePos));
                return gate == null ? "ungated" : gate.skill().id + ":" + gate.level();
            });

            Boolean denied = world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                return p.gameMode.destroyBlock(orePos);
            });
            if (Boolean.TRUE.equals(denied) && !"ungated".equals(gateInfo)) {
                throw new AssertionError("gate " + gateInfo
                        + " did not deny the coal_ore break for an under-leveled player");
            }

            double jobXpBeforeUnlock = world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                return JobState.xp(p);
            });
            if (jobXpBeforeUnlock != 0.0D) {
                throw new AssertionError("denied break awarded job XP anyway: " + jobXpBeforeUnlock);
            }

            // Unlock: raise the gated skill to the required level.
            if (!"ungated".equals(gateInfo)) {
                world.getServer().computeOnServer(server -> {
                    ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                    String[] parts = gateInfo.split(":");
                    dev.jmiahman.hearthwind.skills.Skill skill =
                            dev.jmiahman.hearthwind.skills.Skill.byId(parts[0]);
                    dev.jmiahman.hearthwind.skills.SkillXp.setLevel(p, skill, Integer.parseInt(parts[1]));
                    return Boolean.TRUE;
                });
                context.waitTicks(10);
            }

            Boolean oreBroke = world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                return p.gameMode.destroyBlock(orePos);
            });
            if (!Boolean.TRUE.equals(oreBroke)) {
                throw new AssertionError("coal_ore break still denied after reaching level "
                        + gateInfo + " at " + orePos.toShortString());
            }
            context.waitTicks(10);

            double oreMiningXp = world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                Map<String, Double> xp = p.getAttached(SkillXp.XP);
                return xp == null ? 0.0D : xp.getOrDefault("mining", 0.0D);
            });
            if (oreMiningXp <= rockMiningXp) {
                throw new AssertionError("no additional mining XP after destroying coal_ore; xp=" + oreMiningXp);
            }

            double jobXp = world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                return JobState.xp(p);
            });
            if (jobXp <= 0.0D) {
                throw new AssertionError("no miner job XP recorded after destroying coal_ore");
            }

            context.takeScreenshot("mining_xp_after_break");
        }
    }
}
