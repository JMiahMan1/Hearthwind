package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalDiet;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

/**
 * Full diet-loop integration: give an apple, eat it through the real input
 * pipeline (hold use-key, watch isUsingItem start AND finish), then assert
 * on the SERVER that the fruits nutrient attachment actually moved and the
 * player survived the consume hook (guards against the historical
 * Consumable mixin clinit crash class that disconnected live players).
 */
public class DietGameTests implements FabricClientGameTest {
    private static final int SLOW_TIMEOUT_TICKS = 20 * 300;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender(SLOW_TIMEOUT_TICKS);
            context.waitTicks(40);

            // The survival guide book auto-opens a few seconds after first join
            // (InHandSpreadBookViewScreen) and swallows all keybinds. Force-close
            // any screen repeatedly until the HUD stays clear for a stability window.
            dismissAutoScreens(context);

            // A fresh player has full hunger and an apple is not canAlwaysEat ->
            // canEat() is false and the use action would be a no-op. Make the
            // player hungry on BOTH sides so the eat cycle actually starts.
            world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                p.getFoodData().setFoodLevel(2);
                p.getFoodData().setSaturation(0.0F);
                return p.getFoodData().getFoodLevel();
            });
            context.runOnClient(minecraft -> {
                minecraft.player.getFoodData().setFoodLevel(2);
                minecraft.player.getFoodData().setSaturation(0.0F);
            });
            context.waitTicks(5);

            // StarterKit puts the survival guide book in the inventory/offhand;
            // a failed main-hand use falls through to the offhand and opens the
            // book screen. Clear it so only the apple is usable.
            context.runOnClient(minecraft -> {
                minecraft.player.getInventory().setItem(net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND,
                        net.minecraft.world.item.ItemStack.EMPTY);
            });
            context.waitTicks(2);

            double fruitsBefore = nutrient(world.getServer(), "fruits");

            // StarterKit fills the hotbar (guide book, bottle, campfire) on join,
            // so clear the inventory first to give the apple a predictable slot 0.
            world.getServer().computeOnServer(server -> {
                ServerPlayer p = server.getPlayerList().getPlayers().get(0);
                p.getInventory().clearContent();
                return Boolean.TRUE;
            });
            world.getServer().runCommand("give @p minecraft:apple 8");
            context.getInput().pressKey(GLFW.GLFW_KEY_1); // hotbar slot 0
            context.waitTicks(5);

            // STRICT check: the held ITEM must be the apple (not a substring match
            // over a dump string - the guide book in slot 0 once passed it).
            String held = context.computeOnClient(minecraft -> minecraft.player == null
                    ? "no-player"
                    : String.valueOf(minecraft.player.getMainHandItem().getItem()));
            if (!"minecraft:apple".equals(held)) {
                String invDump = context.computeOnClient(minecraft -> {
                    var inv = minecraft.player.getInventory();
                    StringBuilder slots = new StringBuilder();
                    for (int i = 0; i < 9; i++) {
                        slots.append(i).append('=').append(inv.getItem(i).getItem()).append(" ");
                    }
                    return slots.toString();
                });
                throw new AssertionError("held item is " + held + " after give+select; hotbar: " + invDump);
            }

            // Look straight up so no block/entity intercepts the use action,
            // then hold use-key until the eat cycle visibly starts and ends.
            context.getInput().lookAt(0.0F, -90.0F);
            context.waitTicks(2);
            context.getInput().holdKey(options -> options.keyUse);

            java.util.List<String> timeline = new java.util.ArrayList<>();
            for (int i = 0; i < 25; i++) {
                final int t = i;
                timeline.add(context.computeOnClient(minecraft ->
                        "t" + t
                                + " overlay=" + (minecraft.gui.overlay() == null ? "null" : minecraft.gui.overlay().getClass().getSimpleName())
                                + " screen=" + (minecraft.gui.screen() == null ? "null" : minecraft.gui.screen().getClass().getSimpleName())
                                + " down=" + minecraft.options.keyUse.isDown()
                                + " using=" + minecraft.player.isUsingItem()
                                + " busy=" + minecraft.player.isHandsBusy()
                                + " hit=" + (minecraft.hitResult == null ? "null" : minecraft.hitResult.getType())
                                + " food=" + minecraft.player.getFoodData().getFoodLevel()
                                + " off=" + minecraft.player.getOffhandItem().getItem()
                                + " rcd=" + rightClickDelay(minecraft)));
                context.waitTick();
            }
            Boolean startedEating = context.computeOnClient(m -> m.player.isUsingItem());
            if (!Boolean.TRUE.equals(startedEating)) {
                throw new AssertionError("eat never started; timeline: " + String.join(" | ", timeline));
            }
            context.waitFor(minecraft -> !minecraft.player.isUsingItem(), SLOW_TIMEOUT_TICKS);
            context.getInput().releaseKey(options -> options.keyUse);
            context.waitTicks(10);

            double fruitsAfter = nutrient(world.getServer(), "fruits");
            if (fruitsAfter <= fruitsBefore) {
                throw new AssertionError("fruits nutrient did not increase after eating an apple: "
                        + fruitsBefore + " -> " + fruitsAfter);
            }

            // Player must still be online (a Ticking-player crash would kick them)
            int players = world.getServer().computeOnServer(server -> server.getPlayerList().getPlayerCount());
            if (players != 1) {
                throw new AssertionError("player count after eating = " + players + " (expected 1)");
            }

            context.takeScreenshot("diet_after_apple");
        }
    }

    private static void dismissAutoScreens(ClientGameTestContext context) {
        for (int round = 0; round < 5; round++) {
            context.setScreen(() -> null);
            context.waitTicks(20);
            if (Boolean.FALSE.equals(context.computeOnClient(minecraft -> minecraft.gui.screen() != null))) {
                // still clear after another stability window
                context.waitTicks(20);
                if (Boolean.FALSE.equals(context.computeOnClient(minecraft -> minecraft.gui.screen() != null))) {
                    return;
                }
            }
        }
        throw new AssertionError("a screen keeps re-opening after join; last screen refused to stay closed");
    }

    private static int rightClickDelay(net.minecraft.client.Minecraft minecraft) {
        try {
            java.lang.reflect.Field f = net.minecraft.client.Minecraft.class.getDeclaredField("rightClickDelay");
            f.setAccessible(true);
            return f.getInt(minecraft);
        } catch (Exception e) {
            return -1;
        }
    }

    private static double nutrient(TestServerContext server, String key) {
        Double v = server.computeOnServer(minecraftServer -> {
            ServerPlayer p = minecraftServer.getPlayerList().getPlayers().get(0);
            Map<String, Double> nutrients = p.getAttached(HearthwindSurvivalDiet.NUTRIENTS);
            return nutrients == null ? 0.0D : nutrients.getOrDefault(key, 0.0D);
        });
        return v == null ? 0.0D : v;
    }
}
