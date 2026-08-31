package dev.jmiahman.hearthwind.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.Identifier;

/**
 * Job HUD: shows current job icon, title, level, and XP progress bar above the hotbar.
 * Only renders when the player has an active job.
 */
public final class JobHud implements HudElement {
    public static final JobHud INSTANCE = new JobHud();

    private JobHud() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath("hearthwind", "job"),
                INSTANCE);
    }

    private static Item getJobIcon(String job) {
        if (job == null) return Items.WRITABLE_BOOK;
        return switch (job.toLowerCase()) {
            case "fisher" -> Items.FISHING_ROD;
            case "miner" -> Items.IRON_PICKAXE;
            case "farmer" -> Items.GOLDEN_HOE;
            case "warrior" -> Items.IRON_SWORD;
            case "smither" -> Items.ANVIL;
            case "brewer" -> Items.BREWING_STAND;
            case "builder" -> Items.BRICKS;
            case "lumberjack" -> Items.IRON_AXE;
            default -> Items.WRITABLE_BOOK;
        };
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }
        if (!ClientJobData.hasJob()) {
            return;
        }

        Font font = mc.font;
        String rawJob = ClientJobData.jobId();
        String jobCap = rawJob.isEmpty() ? "Job" : rawJob.substring(0, 1).toUpperCase() + rawJob.substring(1).toLowerCase();
        int lvl = ClientJobData.level();
        String text = jobCap + " Lv." + lvl;

        int textW = font.width(text);
        int barW = 60;
        int totalW = 20 + Math.max(textW, barW) + 6;
        int totalH = 20;

        int left = 4;
        int top = graphics.guiHeight() - 56;

        // Background chip
        graphics.fill(left, top, left + totalW, top + totalH, 0x80000000);
        graphics.fill(left, top, left + totalW, top + 1, 0xFF4A4A4A);

        // Job icon
        graphics.item(new ItemStack(getJobIcon(rawJob)), left + 2, top + 2);

        // Text & XP bar
        graphics.text(font, text, left + 20, top + 2, 0xFFFFFFFF, true);

        int xpBarX = left + 20;
        int xpBarY = top + 12;
        graphics.fill(xpBarX, xpBarY, xpBarX + barW, xpBarY + 4, 0xFF333333);
        int fill = Math.round(barW * ClientJobData.xpProgress());
        if (fill > 0) {
            graphics.fill(xpBarX, xpBarY, xpBarX + fill, xpBarY + 4, 0xFF43A047);
        }
    }
}
