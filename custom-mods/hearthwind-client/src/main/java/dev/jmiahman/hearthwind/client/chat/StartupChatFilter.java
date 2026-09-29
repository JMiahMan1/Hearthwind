package dev.jmiahman.hearthwind.client.chat;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

/**
 * Keeps the start of a session readable.
 *
 * <p>A fresh Hearthwind world is loud. Roughly two dozen mods announce
 * themselves on join and on first tick - the backpack, the atlas, the season
 * widget, the level ring, waypoint hints, the day counter, "press K for
 * skills", config warnings from mods that failed to find their file, and the
 * occasional stack trace. The messages scroll past in about two seconds and
 * they bury the lines that matter: the ones Hearthwind itself sends when it
 * hands you the guide book, the starter loadout and the first quest hints.
 *
 * <p>So during a window after joining a world, system chat is dropped unless it
 * is one of ours. What is NOT touched:
 *
 * <ul>
 *   <li>Overlay packets, which are how a mod draws a title-card or a
 *       crosshair timer rather than writing a chat line.
 *   <li>Player chat, which arrives on a different packet and is never
 *       filtered - you can always read what other people say.
 *   <li>Everything after the window expires, so a mod that reports something
 *       important later still gets through.
 * </ul>
 *
 * <p>Hearthwind's own lines are recognised by their {@code [Hearthwind]} prefix
 * and are always let through, which is the point of the filter: the pack's own
 * instructions survive, everything else's startup chatter does not.
 */
public final class StartupChatFilter {

    /** Our own chat lines all start with this, in the game's legacy colour code. */
    public static final String OUR_PREFIX = "§a[Hearthwind]";
    private static final String OUR_PREFIX_PLAIN = "[Hearthwind]";

    /** Ticks after entering a world during which other mods' system chat is dropped. */
    public static final int SUPPRESS_TICKS = 20 * 30;

    private static int ticksLeft;

    private StartupChatFilter() {
    }

    /** Called when a world is joined, and on disconnect. */
    public static void onJoinWorld() {
        ticksLeft = SUPPRESS_TICKS;
    }

    public static void onLeaveWorld() {
        ticksLeft = 0;
    }

    /** Client tick: counts the window down. */
    public static void tick() {
        if (ticksLeft > 0) {
            ticksLeft--;
        }
    }

    public static boolean isSuppressing() {
        return ticksLeft > 0;
    }

    /**
     * @return true when the packet should be dropped before it reaches the chat
     *     listener. Overlays are never dropped: they are not chat lines.
     */
    public static boolean shouldDrop(ClientboundSystemChatPacket packet) {
        if (packet.overlay() || !isSuppressing()) {
            return false;
        }
        return !isOurs(packet.content());
    }

    /**
     * True when the message is one Hearthwind sent. Prefers the first line, so
     * a multi-line block is judged by its header like the rest of the pack's
     * output.
     */
    public static boolean isOurs(Component content) {
        String text = content.getString();
        if (text.isEmpty()) {
            return false;
        }
        int newline = text.indexOf('\n');
        String firstLine = newline < 0 ? text : text.substring(0, newline);
        return firstLine.contains(OUR_PREFIX) || firstLine.contains(OUR_PREFIX_PLAIN);
    }

    /** Exposed for the gametest: the ids of the lines a filter run dropped. */
    public static List<String> describeWindow() {
        return List.of("suppressing " + isSuppressing(), "ticksLeft=" + ticksLeft,
                "window=" + SUPPRESS_TICKS + " ticks", "our prefix=" + OUR_PREFIX_PLAIN);
    }
}
