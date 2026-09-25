package dev.jmiahman.hearthwind.client;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Aged 3.1.2 / JobsAddon JobScreen rebuild (clean-room: layout studied from
 * the GPL JobsAddon source, reimplemented on our job state and 26.2 APIs).
 *
 * <p>200x215 panel, "&lt;Name&gt; Jobs" title, "Job Cooldown: MM:SS" and an
 * employed summary, then eight 91x38 job cards in two columns: icon slot,
 * name, centred "Lv. N" and a segmented XP bar. Clicking a card sends the
 * existing {@code /job join|leave} command path.
 */
@Environment(EnvType.CLIENT)
public class JobsScreen extends HearthwindPanelScreen {

    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
            "hearthwind", "textures/gui/jobs/job_background.png");
    private static final Identifier BUTTONS = Identifier.fromNamespaceAndPath(
            "hearthwind", "textures/gui/jobs/job_buttons.png");
    private static final Identifier ICONS_TEX = Identifier.fromNamespaceAndPath(
            "hearthwind", "textures/gui/jobs/job_icons.png");

    /** Gallery / JobsAddon draw order (row-major). */
    private static final String[] JOB_ORDER = {
            "lumberjack", "miner", "farmer", "warrior",
            "builder", "smither", "fisher", "brewer" };

    private static final Item[] JOB_ICONS = {
            Items.IRON_AXE, Items.IRON_PICKAXE, Items.IRON_HOE, Items.IRON_SWORD,
            Items.BRICKS, Items.ANVIL, Items.FISHING_ROD, Items.BREWING_STAND };

    private static final String[] JOB_DESCS = {
            "Cut Trees", "Mine Ore / Use Tnt", "Cook Food / Farm Plants", "Kill Mobs",
            "Build Buildings", "Use Anvil / Smelt Ore / Smith Gear", "Catch Fish",
            "Brew Potions / Enchant Items" };

    public JobsScreen() {
        super(Component.translatable("screen.hearthwind.jobs"));
    }

    @Override
    protected TabStrip.Tab activeTab() {
        return TabStrip.Tab.JOBS;
    }

    @Override
    protected void drawPanel(GuiGraphicsExtractor graphics) {
        // Exact Aged: 200x215 blit from 256x256 job_background.png at (x,y) uv 0,0
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                this.x, this.y, 0F, 0F, PANEL_W, PANEL_H, 256, 256, 0xFFFFFFFF);
    }

    @Override
    protected void drawContent(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        String playerName = mc.player != null ? mc.player.getName().getString() : "Player";

        Component title = Component.translatable("screen.hearthwind.jobs.title", playerName);
        graphics.text(font, title, this.x + 100 - font.width(title) / 2, this.y + 7, INK, false);

        graphics.text(font, "Job Cooldown: " + formatCooldown(ClientJobData.cooldownRemainingMillis()),
                this.x + 12, this.y + 20, INK, false);
        graphics.text(font, employedText(), this.x + 12, this.y + 33, INK, false);

        boolean cooldown = ClientJobData.onCooldown();
        boolean slotsFull = ClientJobData.employed().size() >= ClientJobData.maxEmployed();
        String hovered = null;
        for (int i = 0; i < JOB_ORDER.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int cx = this.x + 7 + col * 95;
            int cy = this.y + 47 + row * 41;
            ClientJobData.JobInfo info = ClientJobData.job(JOB_ORDER[i]);
            boolean employed = info != null && info.employed();
            boolean blocked = !employed && (cooldown || slotsFull);
            hovered = drawCard(graphics, font, cx, cy, i, mouseX, mouseY, hovered, blocked);
        }
        if (hovered != null) {
            graphics.setTooltipForNextFrame(Component.literal(hovered), mouseX, mouseY);
        }
    }

    private String drawCard(GuiGraphicsExtractor graphics, Font font, int cx, int cy, int index,
            int mouseX, int mouseY, String hovered, boolean blocked) {
        String id = JOB_ORDER[index];
        ClientJobData.JobInfo info = ClientJobData.job(id);
        boolean employed = info != null && info.employed();
        int level = info != null ? info.level() : 0;
        double xp = info != null ? info.xp() : 0.0;
        double next = info != null && info.nextCost() > 0 ? info.nextCost() : 100.0;

        boolean hover = mouseX >= cx && mouseX < cx + 91 && mouseY >= cy && mouseY < cy + 38;

        // Aged parity: JOB_BUTTON_TEXTURES 256x256, 91x38 slice. textureY: 0 disabled,1 enabled,2 hover,3 employed,4 employed-hover
        int textureY;
        if (employed) {
            textureY = hover ? 4 : 3;
        } else if (blocked) {
            textureY = 0;
        } else {
            textureY = hover ? 2 : 1;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTONS,
                cx, cy, 0F, (float) textureY * 38F, 91, 38, 256, 256, 0xFFFFFFFF);

        // Aged parity: ICON_TEXTURES 256x256, job icon 14x14 at (index*14,10) inside card at (+5,+5) -> cx+5,cy+5
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS_TEX,
                cx + 5, cy + 5, (float) index * 14F, 10F, 14, 14, 256, 256, 0xFFFFFFFF);

        String name = id.substring(0, 1).toUpperCase() + id.substring(1);
        // Aged offsets: title at bx+23,by+4 (cx+5+18) white 16777215; level at bx+35,by+19 (cx+17+18?) – use exact bx+23/bx+35
        graphics.text(font, name, cx + 23, cy + 4, 0xFFFFFFFF, false);
        String lv = "Lv. " + level;
        graphics.text(font, lv, cx + 35, cy + 19, 0xFFFFFFFF, false);

        // XP bar: background 81x5 at (cx+5,cy+29) uv 0,0 ; filled at 0,5 with scaled w (80*xp/next). Aged uses 81x5 bg + scaled 5h.
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS_TEX,
                cx + 5, cy + 29, 0F, 0F, 81, 5, 256, 256, 0xFFFFFFFF);
        if (next > 0 && xp > 0) {
            int filled = (int) (81f * Math.min(1f, (float) (xp / next)));
            // bar is 80 wide inside? keep 81 for parity, but clamp
            if (filled > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS_TEX,
                        cx + 5, cy + 29, 0F, 5F, filled, 5, 256, 256, 0xFFFFFFFF);
            }
        }

        // Tooltip parity: icon hover shows job tooltip, bar hover shows XP; keep unified hover string for now matching Aged JobScreen logic (icon 14x14 at +5+5, bar 81x5 at +5+29)
        boolean overIcon = mouseX >= cx + 5 && mouseX < cx + 19 && mouseY >= cy + 5 && mouseY < cy + 19;
        boolean overBar = mouseX >= cx + 5 && mouseX < cx + 86 && mouseY >= cy + 29 && mouseY < cy + 34;
        if (hover) {
            if (overIcon) {
                hovered = name + " - " + JOB_DESCS[index];
            } else if (overBar) {
                int cur = (int) xp; int need = (int) next;
                hovered = "XP: " + cur + "/" + need;
            } else {
                hovered = name + " - " + JOB_DESCS[index]
                        + (employed ? " (employed)" : blocked ? " (unavailable)" : " (click to join)");
            }
        }
        return hovered;
    }

    private String employedText() {
        List<String> employed = ClientJobData.employedInOrder(java.util.Arrays.asList(JOB_ORDER));
        if (employed.isEmpty()) {
            return "Not Employed";
        }
        List<String> caps = new ArrayList<>();
        for (String id : employed) {
            caps.add(id.substring(0, 1).toUpperCase() + id.substring(1));
        }
        if (caps.size() == 1) {
            return "Employed Job: " + caps.get(0);
        }
        if (caps.size() == 2) {
            return "Employed Jobs: " + caps.get(0) + ", " + caps.get(1);
        }
        return "Employed Jobs: " + caps.get(0) + ", " + caps.get(1) + ", ...";
    }

    private static String formatCooldown(long millis) {
        int seconds = (int) (millis / 1000L);
        if (seconds >= 3600) {
            return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
        }
        return String.format("%02d:%02d", (seconds % 3600) / 60, seconds % 60);
    }

    @Override
    protected boolean onContentClick(MouseButtonEvent event) {
        if (event.button() != 0) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.connection == null) {
            return false;
        }
        for (int i = 0; i < JOB_ORDER.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int cx = this.x + 7 + col * 95;
            int cy = this.y + 47 + row * 41;
            if (event.x() >= cx && event.x() < cx + 91 && event.y() >= cy && event.y() < cy + 38) {
                String id = JOB_ORDER[i];
                ClientJobData.JobInfo info = ClientJobData.job(id);
                boolean employed = info != null && info.employed();
                boolean slotsFull = ClientJobData.employed().size() >= ClientJobData.maxEmployed();
                if (!employed && (ClientJobData.onCooldown() || slotsFull)) {
                    return false;
                }
                this.click();
                mc.player.connection.sendCommand(employed ? "job leave " + id : "job join " + id);
                return true;
            }
        }
        return false;
    }
}
