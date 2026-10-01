package dev.jmiahman.hearthwind.client.gametest;

import dev.jmiahman.hearthwind.client.DownedHud;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/**
 * The downed overlay changed shape in 0.1.47 to match revive 1.0.7: the
 * bleedout countdown is gone (Aged's {@code timer} is {@code -1}, so its death
 * screen never renders the {@code Time: %d} line either), the death
 * coordinates are always shown instead, and the Revive button is only a live
 * hit target once an ally has armed it.
 *
 * <p>These are pure geometry/state assertions - no world needed - because the
 * expensive part of this mechanic is the server side, which the headless server
 * suite already pins. What this guards is the client half: that the button is
 * where it is drawn, and that it cannot be clicked before it is armed.
 *
 * <p><b>No screenshot is taken, deliberately.</b> Three attempts produced a
 * file byte-identical to {@code tour_title}: the overlay is a HUD element, and
 * by the time this entrypoint runs there is either no level at all or a screen
 * open, so the HUD never extracts and the capture is the title screen. A
 * screenshot that shows the wrong thing is worse than none, so the proof here
 * is the assertions plus the fact that the drawing code and the hit test read
 * the same layout constants from this class. A real picture needs a second
 * player to down you in a live world.
 */
public class DownedOverlayGameTests implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> {
            int guiW = 427;
            int guiH = 240;
            int centreX = guiW / 2;
            // The overlay draws its title at height/2 - 40, so the button sits
            // 40 px below that. Recomputing it here is deliberate: if the draw
            // code moves, this assertion fails loudly instead of silently
            // clicking nothing.
            int buttonY = guiH / 2;

            // Not downed: no hit target anywhere, even in the button's place.
            DownedHud.update(false, false, 10, 64, -20);
            if (DownedHud.isReviveButtonAt(guiW, guiH, centreX, buttonY + 10)) {
                throw new AssertionError("a healthy player must not have a Revive hit target");
            }

            // Downed but unarmed: the reference's canRevive is false until an
            // ally interacts, so the button is drawn greyed and must not fire.
            DownedHud.update(true, false, 10, 64, -20);
            if (!DownedHud.isDowned()) {
                throw new AssertionError("update(true, ...) must mark the player downed");
            }
            if (DownedHud.isArmed()) {
                throw new AssertionError("an unarmed downed player must not report itself armed");
            }
            if (DownedHud.isReviveButtonAt(guiW, guiH, centreX, buttonY + 10)) {
                throw new AssertionError("the Revive button must not be clickable before an ally arms it");
            }

            // Armed: live, inside the button, and nowhere else.
            DownedHud.update(true, true, -118, 64, 302);
            if (!DownedHud.isArmed()) {
                throw new AssertionError("update(true, true, ...) must arm the Revive button");
            }
            if (!DownedHud.isReviveButtonAt(guiW, guiH, centreX, buttonY + 10)) {
                throw new AssertionError("the centre of the drawn button must be a hit target once armed");
            }
            if (DownedHud.isReviveButtonAt(guiW, guiH, centreX, buttonY - 30)) {
                throw new AssertionError("a click above the button must miss it");
            }
            if (DownedHud.isReviveButtonAt(guiW, guiH, 4, buttonY + 10)) {
                throw new AssertionError("a click left of the button must miss it");
            }
            if (DownedHud.isReviveButtonAt(guiW, guiH, guiW - 4, buttonY + 10)) {
                throw new AssertionError("a click right of the button must miss it");
            }

            // Clear it again, so a later entrypoint is not left downed.
            DownedHud.update(false, false, 0, 0, 0);
        });
    }
}