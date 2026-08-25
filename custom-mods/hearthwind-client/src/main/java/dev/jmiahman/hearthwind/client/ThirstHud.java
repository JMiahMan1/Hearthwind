package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

/**
 * Vanilla-like thirst bar above hunger. Sprite sheet is the MIT-licensed
 * thirst_icons.png from ghen-git/Thirst-Mod (Thirst Was Taken), recorded in
 * ATTRIBUTION.md: 25x9 px, three 9x9 droplets - empty (u 0), half (u 8),
 * full (u 16). Rendered exactly like vanilla hunger: right-aligned above
 * the food bar, 8px per icon, wobbles when dehydrated.
 */
public final class ThirstHud implements HudElement {
    private static final Identifier THIRST_ICONS =
            Identifier.fromNamespaceAndPath("hearthwind", "textures/gui/thirst_icons.png");
    private static final RandomSource RANDOM = RandomSource.create();

    // Sprite sheet geometry (px / px-width -> normalized UV)
    private static final float TEX_W = 25.0f;
    private static final int ICON = 9;

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
        int left = width / 2 + 91;      // same right edge as hunger
        int top = height - 49;          // 10px above the food bar (height-39)

        float hydration = ClientThirstData.getHydration();
        int level = Math.round(hydration); // 0..20

        for (int i = 0; i < 10; i++) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;

            // Dehydrated wobble, same feel as the vanilla hunger shake
            if (hydration <= 6.0f && mc.player.tickCount % (Math.max(1, level) * 3 + 1) == 0) {
                y = top + (RANDOM.nextInt(3) - 1);
            }

            // background (empty droplet)
            blit(graphics, x, y, 0);

            if (idx < level) {
                blit(graphics, x, y, 16); // full
            } else if (idx == level) {
                blit(graphics, x, y, 8);  // half
            }
        }
    }

    private static void blit(GuiGraphicsExtractor g, int x, int y, int uPx) {
        g.blit(THIRST_ICONS, x, y, ICON, ICON,
                uPx / TEX_W, 0.0f, (uPx + ICON) / TEX_W, 1.0f);
    }
}
