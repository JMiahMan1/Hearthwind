package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * The overlay shown while you are downed.
 *
 * <p>revive 1.0.7 puts its Revive button on the vanilla death screen. Our
 * player is never technically dead - the server cancels the death - so the
 * same control lives here. What matches the reference:
 *
 * <ul>
 *   <li><b>No countdown.</b> The reference's death screen only draws its
 *       {@code Time: %d} line when {@code timer != -1}, and Aged leaves
 *       {@code timer} at -1, so Aged never shows one. We used to show
 *       "Bleeding out in Ns"; that was our invention.</li>
 *   <li><b>Death coordinates.</b> The reference always draws them
 *       ({@code showDeathCoordinates} defaults to true).</li>
 *   <li><b>One click, not a hold.</b> The button is live once an ally has
 *       armed it, and pressing it revives immediately.</li>
 * </ul>
 */
public final class DownedHud implements HudElement {
    public static final DownedHud INSTANCE = new DownedHud();

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger("Hearthwind/DownedHud");

    private static final int BUTTON_W = 200;
    private static final int BUTTON_H = 20;

    private static volatile boolean isDowned = false;
    private static volatile boolean armed = false;
    private static volatile int deathX = 0;
    private static volatile int deathY = 0;
    private static volatile int deathZ = 0;

    private DownedHud() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("hearthwind", "downed_overlay"),
                INSTANCE);
    }

    public static void update(boolean downed, boolean isArmed, int x, int y, int z) {
        isDowned = downed;
        armed = isArmed;
        deathX = x;
        deathY = y;
        deathZ = z;
    }

    public static boolean isDowned() {
        return isDowned;
    }

    public static boolean isArmed() {
        return armed;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!isDowned) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        float pulse = (float) ((Math.sin(System.currentTimeMillis() / 250.0) + 1.0) / 2.0);
        int alpha = (int) (40 + pulse * 60);
        int color = (alpha << 24) | 0x880000;
        graphics.fill(0, 0, width, height, color);

        String title = "§c§lYOU ARE DOWNED";
        int titleX = width / 2 - font.width(title) / 2;
        int titleY = height / 2 - 40;
        graphics.text(font, title, titleX, titleY, 0xFFFF3333, true);

        // revive 1.0.7's DeathScreenMixin always draws the death coordinates.
        String coords = "§7" + deathX + " / " + deathY + " / " + deathZ;
        graphics.text(font, coords, width / 2 - font.width(coords) / 2, titleY + 14, 0xFFFFFFFF, true);

        String help = armed
                ? "§aPress Revive to get back up."
                : "§7An ally must crouch and use (right-click) you with an empty hand.";
        graphics.text(font, help, width / 2 - font.width(help) / 2, titleY + 26, 0xFFAAAAAA, true);

        int buttonX = width / 2 - BUTTON_W / 2;
        int buttonY = titleY + 40;
        int fill = armed ? 0xFF2E7D32 : 0xFF3A3A3A;
        graphics.fill(buttonX - 1, buttonY - 1, buttonX + BUTTON_W + 1, buttonY + BUTTON_H + 1, 0xFF000000);
        graphics.fill(buttonX, buttonY, buttonX + BUTTON_W, buttonY + BUTTON_H, fill);
        String label = armed ? "§aRevive" : "§8Revive";
        graphics.text(font, label, width / 2 - font.width(label) / 2, buttonY + 6,
                armed ? 0xFFFFFFFF : 0xFF808080, true);
    }

    /**
     * The click target for the Revive button, in the same coordinates
     * {@link #extractRenderState} drew it. Kept in one place so the two can
     * never drift apart.
     */
    public static boolean isReviveButtonAt(int guiWidth, int guiHeight, double mouseX, double mouseY) {
        if (!isDowned || !armed) {
            return false;
        }
        int width = guiWidth;
        int height = guiHeight;
        int buttonX = width / 2 - BUTTON_W / 2;
        int buttonY = height / 2 - 40 + 40;
        return mouseX >= buttonX && mouseX < buttonX + BUTTON_W
                && mouseY >= buttonY && mouseY < buttonY + BUTTON_H;
    }

    /** Fires the one-click self-revive the reference's death-screen button fires. */
    public static void requestRevive(Minecraft mc) {
        try {
            ClientPlayNetworking.send(new dev.jmiahman.hearthwind.survival.revive.DownedRevivePayload(true));
        } catch (Exception e) {
            if (mc != null) {
                LOGGER.warn("Could not send the downed revive request", e);
            }
        }
    }
}