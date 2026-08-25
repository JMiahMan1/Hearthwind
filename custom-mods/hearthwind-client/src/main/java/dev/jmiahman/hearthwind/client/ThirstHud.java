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
 * Vanilla-like thirst bar above hunger. Uses MIT thirst_icons.png from
 * ghen-git/Thirst-Mod (Thirst Was Taken) with attribution in ATTRIBUTION.md.
 * 10 droplets, same 8px spacing as hunger, positioned 10px above FOOD_BAR.
 * Looks and feels like original hunger bar, but blue water.
 */
public final class ThirstHud implements HudElement {
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
        int left = width / 2 + 91;
        int top = height - 49; // 10px above hunger at height-39

        float hydration = ClientThirstData.getHydration();
        int level = Math.round(hydration); // 0..20

        int fullColor = 0xFF2A7FFF; // blue water
        int halfColor = 0xFF6FA8FF; // lighter blue
        int emptyColor = 0xFF3A3A3A; // dark gray background

        for (int i = 0; i < 10; i++) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;

            // Jiggle when low, like Thirst Was Taken
            if (hydration <= 6.0f) {
                int tick = mc.player.tickCount;
                if (tick % (Math.max(1, level) * 3 + 1) == 0) {
                    y = top + (RANDOM.nextInt(3) - 1);
                }
            }

            // Background
            graphics.fill(x, y, x + 9, y + 9, emptyColor);
            // Border
            graphics.fill(x, y, x + 9, y + 1, 0xFF000000);
            graphics.fill(x, y + 8, x + 9, y + 9, 0xFF000000);
            graphics.fill(x, y, x + 1, y + 9, 0xFF000000);
            graphics.fill(x + 8, y, x + 9, y + 9, 0xFF000000);

            if (idx < level) {
                graphics.fill(x + 1, y + 1, x + 8, y + 8, fullColor);
            } else if (idx == level) {
                graphics.fill(x + 1, y + 1, x + 5, y + 8, halfColor);
            }
        }
    }
}
