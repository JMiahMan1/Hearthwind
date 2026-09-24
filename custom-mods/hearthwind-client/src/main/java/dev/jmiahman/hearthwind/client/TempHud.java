package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * EnvironmentZ 2.0.8 temperature HUD parity (Aged 3.1.2 ship version;
 * upstream Globox1997/EnvironmentZ TemperatureHudRendering + InGameHudMixin,
 * GPL-3.0 studied, clean-room reimplementation against our integer scale):
 *
 * <ol>
 *   <li>Body-status icon 13x13 at (width/2 - 7, height - 52): base silhouette
 *       plus a hot/cold fill whose step follows the reference band math -
 *       normal bands span 240..1800 (smooth = (|body| - 240) / 1560) and the
 *       extreme bands span 1800..2400 (smooth = (|body| - 1800) / 600), split
 *       at 13/10/8/5/3/0 px.</li>
 *   <li>Wetness border while wet (alpha = wetness / 200), only in the normal
 *       body bands, matching where upstream draws row 39.</li>
 *   <li>Thermometer 16x32 at (width/2 - thermometerIconX, height -
 *       thermometerIconY) = (+95, -32) with Aged's thermometerIconX = -95.
 *       Full cold at &lt;= -6, cool step at -6..-3, full hot at &gt;= 6, warm
 *       step at 3..6, no mercury in the comfortable band.</li>
 *   <li>2.0.8 has NO trend arrow (that arrived in a later release).</li>
 * </ol>
 * Hidden in creative/spectator/invulnerable and with the HUD off. Body/
 * foreground/border art is original (upstream sheet not copied).
 */
public final class TempHud implements HudElement {
    private static final Identifier BODY_ICON =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/body_icon");
    private static final Identifier WETNESS_BORDER =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/wetness_border");
    private static final Identifier THERMOMETER_FRAME =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thermometer_frame");
    private static final Identifier ICON_SNOWFLAKE =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/icon_snowflake");
    private static final Identifier ICON_SUN =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/icon_sun");
    private static final Identifier UNIT_BOX =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/unit_box_f");
    private static final Identifier ARROW_UP =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/temp_arrow_up");
    private static final Identifier ARROW_DOWN =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/temp_arrow_down");

    /**
     * Aged exact: iconX 7, iconY 52, thermometerIconX -95 (i.e. +95 right of
     * center). The ComfortRoll-safe key-label arrangement is documented in
     * docs/PORTING.md; the gauge is raised 6 px above Aged's bottom-flush
     * anchor for readability (THERMOMETER_Y 32 -> 38).
     */
    private static final int ICON_X = 7;
    private static final int ICON_Y = 52;
    private static final int THERMOMETER_X = -95;
    private static final int THERMOMETER_Y = 38;

    public static final TempHud INSTANCE = new TempHud();
    private static int lastThermometer = Integer.MIN_VALUE;

    private TempHud() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath("hearthwind", "temp"),
                INSTANCE);
    }

    private static Identifier mercuryId(boolean hot, int step) {
        return Identifier.fromNamespaceAndPath("hearthwind",
                hot ? "hud/thermometer_hot" + stepSuffix(step)
                        : "hud/thermometer_cold" + stepSuffix(step));
    }

    private static String stepSuffix(int step) {
        return step >= 100 ? "" : "_" + step;
    }

    private static Identifier bodyFillId(boolean hot, int px) {
        if (px >= 13) {
            return Identifier.fromNamespaceAndPath("hearthwind",
                    hot ? "hud/body_fill_hot" : "hud/body_fill_cold");
        }
        return Identifier.fromNamespaceAndPath("hearthwind",
                hot ? "hud/body_fill_hot_" + px : "hud/body_fill_cold_" + px);
    }

    /** Upstream iconTextureSplitValue steps, in px of 13. */
    private static int splitPx(float smooth) {
        if (smooth >= 0.99F) {
            return 13;
        } else if (smooth > 0.79F) {
            return 10;
        } else if (smooth > 0.59F) {
            return 8;
        } else if (smooth > 0.39F) {
            return 5;
        } else if (smooth > 0.19F) {
            return 3;
        }
        return 0;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()
                || mc.player.isInvulnerable() || mc.gui.hud.isHidden()) {
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int body = ClientTempData.getBodyTemperature();
        int wet = ClientTempData.getWetness();

        // 1. Body-status icon at (width/2 - 7, height - 52).
        int bodyX = width / 2 - ICON_X;
        int bodyY = height - ICON_Y;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BODY_ICON, bodyX, bodyY, 13, 13);

        boolean hot = body >= 0;
        boolean extremeTier = false;
        float smooth = 0F;
        if (body < ClientTempData.BODY_MIN_COLD) {
            hot = false;
            if (body < ClientTempData.BODY_MAX_COLD) {
                extremeTier = true;
                smooth = (Math.abs(body) - Math.abs(ClientTempData.BODY_MAX_COLD)) / 600F;
            } else {
                smooth = (Math.abs(body) - Math.abs(ClientTempData.BODY_MIN_COLD)) / 1560F;
            }
        } else if (body > ClientTempData.BODY_MIN_HOT) {
            hot = true;
            if (body > ClientTempData.BODY_MAX_HOT) {
                extremeTier = true;
                smooth = (body - ClientTempData.BODY_MAX_HOT) / 600F;
            } else {
                smooth = (body - ClientTempData.BODY_MIN_HOT) / 1560F;
            }
        }
        int fillPx = splitPx(Math.max(0F, Math.min(1F, smooth)));
        if (fillPx > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, bodyFillId(hot, fillPx),
                    bodyX, bodyY, 13, 13);
        }
        // 2. Wetness border: reference draws it only in the normal body bands.
        if (!extremeTier && wet > 0) {
            float alpha = Math.min(1F, wet / (float) ClientTempData.WETNESS_MAX);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, WETNESS_BORDER,
                    bodyX, bodyY, 13, 13, ARGB.white(alpha));
        }

        // 3. Thermometer at (width/2 - thermometerIconX, height - thermometerIconY).
        int tubeX = width / 2 - THERMOMETER_X;
        int tubeY = height - THERMOMETER_Y;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, THERMOMETER_FRAME, tubeX, tubeY, 16, 32);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, UNIT_BOX, tubeX + 18, tubeY + 10, 12, 12);
        int thermometer = ClientTempData.getThermometer();
        if (lastThermometer != Integer.MIN_VALUE && thermometer != lastThermometer) {
            Identifier arrow = thermometer > lastThermometer ? ARROW_UP : ARROW_DOWN;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrow, tubeX + 20, tubeY - 1, 7, 7);
        }
        lastThermometer = thermometer;
        if (thermometer <= ClientTempData.THERMOMETER_VERY_COLD) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    mercuryId(false, 100), tubeX, tubeY, 16, 32);
        } else if (thermometer <= ClientTempData.THERMOMETER_COLD) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    mercuryId(false, 75), tubeX, tubeY, 16, 32);
        } else if (thermometer >= ClientTempData.THERMOMETER_VERY_HOT) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    mercuryId(true, 100), tubeX, tubeY, 16, 32);
        } else if (thermometer >= ClientTempData.THERMOMETER_HOT) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    mercuryId(true, 75), tubeX, tubeY, 16, 32);
        }

        // 4. Decorative ambient glyph above the thermometer (Hearthwind
        // improvement over Aged): snowflake in cold bands, sun in hot bands.
        if (thermometer <= ClientTempData.THERMOMETER_COLD) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON_SNOWFLAKE,
                    tubeX + 4, tubeY - 10, 7, 7);
        } else if (thermometer >= ClientTempData.THERMOMETER_HOT) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON_SUN,
                    tubeX + 4, tubeY - 10, 7, 7);
        }

        // 5. Status word above the body figure (Hearthwind improvement):
        // only while in an active temperature band, so the normal HUD stays
        // clean. Red when hot, blue when cold; shadowed for readability.
        var font = mc.font;
        String status = null;
        int statusColor = 0xFFFFFFFF;
        if (body <= ClientTempData.BODY_MAX_COLD) {
            status = "Freezing";
            statusColor = 0xFF7FB2FF;
        } else if (body < ClientTempData.BODY_MIN_COLD) {
            status = "Cold";
            statusColor = 0xFF9CC8FF;
        } else if (body >= ClientTempData.BODY_MAX_HOT) {
            status = "Overheating";
            statusColor = 0xFFFF8066;
        } else if (body > ClientTempData.BODY_MIN_HOT) {
            status = "Hot";
            statusColor = 0xFFFFA58C;
        }
        if (status != null) {
            int tw = font.width(status);
            graphics.text(font, status, width / 2 - tw / 2, bodyY - 11, statusColor, true);
        }
    }
}
