package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

/**
 * Exact Aged Dehydration 1.3.6 HUD parity:
 * - 10 discrete teardrops in the 9-px band immediately above hunger (right-aligned to hotbar right edge).
 * - Pitch: 8 GUI px, size: 9x9 sprite slot.
 * - Dynamic color: normal blue vs sickly green when thirst/dirty water effect is active.
 * - 13x13 bottle/flask icon immediately to the left of the droplet row.
 */
public final class ThirstHud implements HudElement {
    private static final Identifier EMPTY =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_empty");
    private static final Identifier FULL_BLUE =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_full");
    private static final Identifier HALF_BLUE =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_half");
    private static final Identifier FULL_GREEN =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_dirty_full");
    private static final Identifier HALF_GREEN =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_dirty_half");
    private static final Identifier FLASK_ICON =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/flask_icon");
    private static final RandomSource RANDOM = RandomSource.create();

    public static final ThirstHud INSTANCE = new ThirstHud();

    private ThirstHud() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.FOOD_BAR,
                Identifier.fromNamespaceAndPath("hearthwind", "thirst"),
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
        int left = width / 2 + 91;      // Right-aligned to hotbar right edge
        int top = height - 49;          // 9-px band immediately above hunger (height - 39)

        // If air bubbles are visible while underwater, shift up 10px
        if (mc.player.getAirSupply() < mc.player.getMaxAirSupply()) {
            top -= 10;
        }

        float hydration = ClientThirstData.getHydration();
        int level = Math.round(hydration); // 0..20

        // Check if player has dirty water / thirst effect active (dehydration:thirst)
        boolean isDirty = isThirstEffectActive(mc);

        Identifier fullSprite = isDirty ? FULL_GREEN : FULL_BLUE;
        Identifier halfSprite = isDirty ? HALF_GREEN : HALF_BLUE;

        // 10 discrete hydration droplets
        for (int i = 0; i < 10; i++) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;

            // Dehydration wobble
            if (hydration <= 6.0f && mc.player.tickCount % (Math.max(1, level) * 3 + 1) == 0) {
                y = top + (RANDOM.nextInt(3) - 1);
            }

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY, x, y, 9, 9);
            if (idx < level) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, fullSprite, x, y, 9, 9);
            } else if (idx == level) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, halfSprite, x, y, 9, 9);
            }
        }

        // 13x13 bottle/flask icon immediately left of the droplet row
        int flaskX = left - 9 * 8 - 9 - 15;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FLASK_ICON, flaskX, top - 2, 13, 13);
    }

    private static boolean isThirstEffectActive(Minecraft mc) {
        if (mc.player == null) return false;
        return mc.player.getActiveEffects().stream().anyMatch(e -> {
            var key = BuiltInRegistries.MOB_EFFECT.getKey(e.getEffect().value());
            return key != null && "dehydration".equals(key.getNamespace()) && "thirst".equals(key.getPath());
        });
    }
}
