package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Exact Aged SeasonHUD widget parity:
 * - Position: Top-left at GUI (2, 2)
 * - Format: 9x9 icon + "[Season], Day [N]/18"
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
        Item icon = switch (ordinal) {
            case 1 -> Items.SUNFLOWER;
            case 2 -> Items.ORANGE_TULIP;
            case 3 -> Items.SNOWBALL;
            default -> Items.PINK_PETALS;
        };

        int color = switch (ordinal) {
            case 1 -> 0xFFFEE92A; // Summer yellow
            case 2 -> 0xFFBC5E27; // Autumn amber
            case 3 -> 0xFFE0FCFC; // Winter cyan
            default -> 0xFFFFA3BB; // Spring pink
        };

        int x = 2;
        int y = 2;

        // Draw 9x9 season icon + single line formatted string
        graphics.item(new ItemStack(icon), x, y);
        String text = ClientSeasonData.displayText();
        graphics.text(mc.font, text, x + 18, y + 4, color, true);
    }
}
