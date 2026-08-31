package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class DownedHud implements HudElement {
    public static final DownedHud INSTANCE = new DownedHud();

    private static boolean isDowned = false;
    private static int remainingSeconds = 0;
    private static int progressPercent = 0;

    private DownedHud() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("hearthwind", "downed_overlay"),
                INSTANCE);
    }

    public static void update(boolean downed, int seconds, int progress) {
        isDowned = downed;
        remainingSeconds = seconds;
        progressPercent = progress;
    }

    public static boolean isDowned() {
        return isDowned;
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

        // Pulsing red vignette overlay
        float pulse = (float) ((Math.sin(System.currentTimeMillis() / 250.0) + 1.0) / 2.0);
        int alpha = (int) (40 + pulse * 60);
        int color = (alpha << 24) | 0x880000;
        graphics.fill(0, 0, width, height, color);

        // Centered Downed Title
        String title = "§c§lYOU ARE DOWNED";
        int titleX = width / 2 - font.width(title) / 2;
        int titleY = height / 2 - 40;
        graphics.text(font, title, titleX, titleY, 0xFFFF3333, true);

        // Subtitle with Bleedout Timer
        String timer = "§eBleeding out in " + remainingSeconds + "s";
        int timerX = width / 2 - font.width(timer) / 2;
        graphics.text(font, timer, timerX, titleY + 14, 0xFFFFFFFF, true);

        String help = "§7An ally must crouch and use (right-click) on you to revive!";
        int helpX = width / 2 - font.width(help) / 2;
        graphics.text(font, help, helpX, titleY + 26, 0xFFAAAAAA, true);

        // Revive progress bar if being revived
        if (progressPercent > 0) {
            int barW = 120;
            int barH = 6;
            int barX = width / 2 - barW / 2;
            int barY = titleY + 40;

            graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF000000);
            graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF444444);

            int fill = (int) (barW * (progressPercent / 100.0f));
            if (fill > 0) {
                graphics.fill(barX, barY, barX + fill, barY + barH, 0xFF2E7D32);
            }

            String prog = "§aReviving... " + progressPercent + "%";
            graphics.text(font, prog, width / 2 - font.width(prog) / 2, barY + 9, 0xFF55FF55, true);
        }
    }
}
