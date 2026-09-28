package dev.jmiahman.hearthwind.client;

import dev.jmiahman.hearthwind.survival.WelcomeStartPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/**
 * Client half of the Aged welcome screen.
 *
 * <p>Aged 3.1.2 opens a welcome screen when a player joins a world for the
 * first time (the {@code welcomescreen} mod reading the
 * {@code aged_welcome_screen} datapack) and its {@code Start} button is what
 * hands over the starter loadout. The server sends
 * {@code WelcomeScreenPayload} when the screen belongs to this player; the
 * screen opens on the next client tick that has a world, and pressing
 * {@code Start} answers with {@link WelcomeStartPayload}.
 *
 * <p>{@code shown} is latched when the screen opens so the screen appears at
 * most once per join: a harness (or a player) that closes it with Escape
 * instead of the button is not re-interrupted, exactly like a client that
 * dismissed Aged's screen.
 */
public final class ClientWelcomeData {
    private static boolean pending;
    private static boolean shown;
    private static Object lastLevel;

    private ClientWelcomeData() {}

    /** Server says the welcome screen is waiting for this player. */
    public static void onServerSaysShow() {
        pending = true;
    }

    public static boolean isPending() {
        return pending;
    }

    public static void reset() {
        pending = false;
        shown = false;
        lastLevel = null;
    }

    /**
     * Client tick hook: opens the screen once a world exists and nothing else
     * is in the way, so a late payload cannot be swallowed by the loading
     * screen and the screen never steals focus from another one.
     *
     * <p>Having no world at all means the request is dead: a payload that
     * arrives for a world the player already left must not pop the screen up
     * in the next one. Entering a world only clears the {@code shown} latch,
     * never {@code pending}: the payload can land on either side of the level
     * swap that creates the local player, and a request that was already made
     * must not be lost.
     */
    public static void tick(Minecraft minecraft) {
        if (minecraft == null) {
            return;
        }
        if (minecraft.level == null) {
            pending = false;
            shown = false;
            lastLevel = null;
            return;
        }
        if (minecraft.level != lastLevel) {
            lastLevel = minecraft.level;
            shown = false;
        }
        if (!pending || shown || minecraft.player == null) {
            return;
        }
        if (minecraft.gui.screen() != null) {
            return;
        }
        shown = true;
        pending = false;
        minecraft.setScreenAndShow(new WelcomeScreen());
    }

    /**
     * The player pressed {@code Start} (or pressed Escape, which we treat the
     * same way so nobody is ever stuck behind the screen without supplies).
     *
     * <p>Sent unconditionally, never behind a {@code canSend} check: that check
     * only reports what the server's channel registration said when it arrived,
     * and fabric-api sends its registration right after the join event, so a
     * player who presses Start early would be left with no supplies at all.
     * The server-side fallback in {@code StarterKit} covers a client that never
     * manages to send.
     */
    public static void sendStart() {
        ClientPlayNetworking.send(new WelcomeStartPayload(true));
    }
}
