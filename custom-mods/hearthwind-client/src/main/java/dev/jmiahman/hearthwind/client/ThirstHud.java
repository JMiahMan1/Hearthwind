package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

/**
 * Vanilla-like thirst bar above hunger. Uses three 9x9 sprites split from the
 * MIT-licensed 25x9 thirst_icons.png sheet (Thirst Was Taken, ghen-git).
 * See ATTRIBUTION.md and assets/hearthwind/textures/gui/sprites/hud/.
 * Rendered exactly like vanilla hunger: right-aligned above the food bar,
 * 8px per icon, wobbles when dehydrated. Fix for the previous black-bar bug:
 * earlier version used GuiGraphicsExtractor.blit with a direct texture and
 * normalized UVs, which on 26.2's deferred GUI pipeline produced stretched
 * black quads. Now uses blitSprite with the GUI atlas (same as vanilla food)
 * via RenderPipelines.GUI_TEXTURED.
 */
public final class ThirstHud implements HudElement {
    private static final Identifier EMPTY =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_empty");
    private static final Identifier HALF =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_half");
    private static final Identifier FULL =
            Identifier.fromNamespaceAndPath("hearthwind", "hud/thirst_full");
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
        int left = width / 2 + 91;      // same right edge as hunger
        int top = height - 49;          // 10px above the food bar (height-39)

        float hydration = ClientThirstData.getHydration();
        int level = Math.round(hydration); // 0..20

        for (int i = 0; i < 10; i++) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;

            // Dehydrated wobble, same feel as vanilla hunger shake
            if (hydration <= 6.0f && mc.player.tickCount % (Math.max(1, level) * 3 + 1) == 0) {
                y = top + (RANDOM.nextInt(3) - 1);
            }

            // background (empty droplet)
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY, x, y, 9, 9);
            if (idx < level) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FULL, x, y, 9, 9);
            } else if (idx == level) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HALF, x, y, 9, 9);
            }
        }
    }
}
