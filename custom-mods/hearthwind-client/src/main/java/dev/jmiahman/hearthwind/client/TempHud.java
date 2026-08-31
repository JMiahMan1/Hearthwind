package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Exact Aged EnvironmentZ 2.0.8 Thermometer Trio parity:
 * 1. Thermometer (#5): x = width/2 + 99 (hotbar right + 8), y = height - 28 (7x27 vertical tube with bulb).
 * 2. Unit Box (#6): x = width/2 + 112, y = height - 17 (12x12 beveled box with "F").
 * 3. Trend Arrow (#7): x = width/2 + 124, y = height - 18 (~15x15 chevron).
 */
public final class TempHud implements HudElement {
    private static final Identifier THERMOMETER_FRAME =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thermometer_frame");
    private static final Identifier MERCURY_HOT =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thermometer_hot");
    private static final Identifier MERCURY_COLD =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thermometer_cold");
    private static final Identifier UNIT_BOX_F =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/unit_box_f");
    private static final Identifier ARROW_UP =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/temp_arrow_up");
    private static final Identifier ARROW_DOWN =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/temp_arrow_down");

    public static final TempHud INSTANCE = new TempHud();

    private TempHud() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath("hearthwind", "temp"),
                INSTANCE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        // 1. Thermometer: hotbar right edge + 8 (x = width/2 + 99, y = height - 28)
        int tubeX = width / 2 + 99;
        int tubeY = height - 28;

        float temp = ClientTempData.getTemperature();
        float norm = Math.max(0f, Math.min(1f, (temp + 10f) / 20f));
        int mercuryHeight = Math.max(1, Math.round(18f * norm));

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, THERMOMETER_FRAME, tubeX, tubeY, 7, 27);
        Identifier mercury = temp >= 0f ? MERCURY_HOT : MERCURY_COLD;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, mercury,
                tubeX + 1, tubeY + 3 + (18 - mercuryHeight), 5, mercuryHeight);

        // 2. Unit Box ("F"): x = width/2 + 112, y = height - 17 (12x12)
        int boxX = width / 2 + 112;
        int boxY = height - 17;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, UNIT_BOX_F, boxX, boxY, 12, 12);

        // 3. Trend Arrow: x = width/2 + 126, y = height - 16 (7x9 native sprite)
        int arrowX = width / 2 + 126;
        int arrowY = height - 16;
        int trend = ClientTempData.trendDirection();
        if (trend != 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    trend > 0 ? ARROW_UP : ARROW_DOWN,
                    arrowX, arrowY, 7, 9);
        }
    }
}
