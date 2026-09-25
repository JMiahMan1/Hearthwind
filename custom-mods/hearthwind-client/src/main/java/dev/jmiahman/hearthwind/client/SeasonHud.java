package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * Exact Aged SeasonHUD widget parity:
 * - Position: Top-left at GUI (2, 2)
 * - Format: 9x9 icon + "[Season], Day [N]/[daysPerSeason]" (21 by default)
 * - Tint colors: Spring #FFA3BB, Summer #FEE92A, Autumn #BC5E27, Winter #E0FCFC
 */
public final class SeasonHud implements HudElement {
    public static final SeasonHud INSTANCE = new SeasonHud();

    private SeasonHud() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.FOOD_BAR,
                Identifier.fromNamespaceAndPath("hearthwind", "season"),
                INSTANCE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isSpectator()) {
            return;
        }

        int ordinal = ClientSeasonData.seasonOrdinal();
        int color = switch (ordinal) {
            case 1 -> 0xFFFEE92A; // Summer yellow
            case 2 -> 0xFFBC5E27; // Autumn amber
            case 3 -> 0xFFE0FCFC; // Winter cyan
            default -> 0xFFFFA3BB; // Spring pink
        };

        int x = 2;
        int y = 2;

        drawSeasonBadge(graphics, x, y, ordinal, color);
        String text = ClientSeasonData.displayText();
        graphics.text(mc.font, text, x + 18, y + 4, color, true);
    }

    private static void drawSeasonBadge(GuiGraphicsExtractor graphics, int x, int y, int ordinal, int color) {
        int highlight = switch (ordinal) {
            case 1 -> 0xFFFFFFFF;
            case 2 -> 0xFFFFD36A;
            case 3 -> 0xFFFFFFFF;
            default -> 0xFFFFD1E6;
        };
        int shadow = switch (ordinal) {
            case 1 -> 0xFFB38B00;
            case 2 -> 0xFF7A2D12;
            case 3 -> 0xFF6A9BA5;
            default -> 0xFF9C3F62;
        };
        graphics.fill(x, y, x + 9, y + 9, 0xFF1A1A1A);
        graphics.fill(x + 1, y + 1, x + 8, y + 8, color);
        graphics.fill(x + 2, y + 2, x + 7, y + 4, highlight);
        graphics.fill(x + 3, y + 5, x + 6, y + 7, shadow);
    }
}
