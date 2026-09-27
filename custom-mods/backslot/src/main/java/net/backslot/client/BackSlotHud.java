package net.backslot.client;

import net.backslot.BackSlot;
import net.backslot.BackSlotConfig;
import net.backslot.BackSlotSlots;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * Aged's HUD pairing: two slot frames near the hotbar (hudSlotX -21 from
 * Aged's config), hidden while both slots are empty.
 */
@Environment(EnvType.CLIENT)
public final class BackSlotHud implements HudElement {

    public static final BackSlotHud INSTANCE = new BackSlotHud();

    private BackSlotHud() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.FOOD_BAR,
                BackSlot.id("slots"), INSTANCE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        BackSlotConfig config = BackSlot.CONFIG;
        if (minecraft.player == null || config.disableBackslotHud) {
            return;
        }
        ItemStack back = BackSlotSlots.get(minecraft.player, BackSlot.BACK_SLOT);
        ItemStack belt = BackSlotSlots.get(minecraft.player, BackSlot.BELT_SLOT);
        if (back.isEmpty() && belt.isEmpty()) {
            return;
        }
        int x = graphics.guiWidth() / 2 - 91 + config.hudSlotX;
        int y = graphics.guiHeight() - 23 + config.hudSlotY;
        drawSlot(graphics, x, y, back);
        drawSlot(graphics, x, y + 22, belt);
    }

    private static void drawSlot(GuiGraphicsExtractor graphics, int x, int y, ItemStack stack) {
        graphics.fill(x, y, x + 20, y + 20, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + 19, y + 19, 0xFF8B8B8B);
        if (!stack.isEmpty()) {
            graphics.item(stack, x + 2, y + 2);
        }
    }
}
