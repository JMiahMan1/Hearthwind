package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.client.chat.StartupChatFilter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.network.chat.Component;

/**
 * The startup chat filter is pure logic about which system messages survive, so
 * it is tested without a world: a mod's greeting must be dropped, the pack's
 * own instructions must not, and the filter must go quiet once its window
 * closes so nothing is lost later in the session.
 */
public class ChatFilterGameTests implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> {
            // A mod announcing itself, which is the noise being removed.
            Component modNoise = Component.literal("Backpack loaded 3 items");
            // The pack's own lines, all of which carry its prefix.
            Component ourInstructions = Component.literal("§a[Hearthwind] Granted Survival Guidebook.");
            Component ourMultiLine = Component.literal("§a[Hearthwind] Starter loadout:\nBread x4");
            // A player typing, and a mod's overlay card, which are never chat.
            Component playerChat = Component.literal("hello everyone");

            if (StartupChatFilter.isOurs(modNoise)) {
                throw new AssertionError("a mod's system message must not be treated as ours");
            }
            if (!StartupChatFilter.isOurs(ourInstructions)) {
                throw new AssertionError("our own instructions must survive the filter");
            }
            if (!StartupChatFilter.isOurs(ourMultiLine)) {
                throw new AssertionError("a multi-line block must be judged by its header line");
            }
            if (StartupChatFilter.isOurs(playerChat)) {
                throw new AssertionError("player chat is not one of our system messages");
            }
        });

        // The window opens on join, is 30 seconds long, and closes itself.
        StartupChatFilter.onLeaveWorld();
        if (StartupChatFilter.isSuppressing()) {
            throw new AssertionError("the filter must start quiet outside a world");
        }
        StartupChatFilter.onJoinWorld();
        if (!StartupChatFilter.isSuppressing()) {
            throw new AssertionError("joining a world must open the suppression window");
        }
        for (int i = 0; i < StartupChatFilter.SUPPRESS_TICKS; i++) {
            StartupChatFilter.tick();
        }
        if (StartupChatFilter.isSuppressing()) {
            throw new AssertionError("the window must close so later mod messages are not lost");
        }
        StartupChatFilter.onJoinWorld();
        if (!StartupChatFilter.isSuppressing()) {
            throw new AssertionError("re-joining a world must reopen the window");
        }
        StartupChatFilter.onLeaveWorld();
        if (StartupChatFilter.isSuppressing()) {
            throw new AssertionError("disconnecting must close the window");
        }
    }
}
