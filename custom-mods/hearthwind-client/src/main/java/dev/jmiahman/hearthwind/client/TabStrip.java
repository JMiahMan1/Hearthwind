package dev.jmiahman.hearthwind.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * LibZ-parity inventory tab strip (clean-room reimplementation of
 * {@code net.libz.util.DrawTabHelper}, MIT, Globox1997 - layout only).
 *
 * <p>Exact geometry from the LibZ 1.0.3 source: tabs are drawn at the panel
 * origin with a 25 px pitch, unselected at {@code y-21} (first tab 25 px tall,
 * others 21 px) and selected at {@code y-23} (27 px tall, merging 4 px into
 * the panel). Backgrounds come from the bundled MIT tab sheet at columns
 * 0 (first selected), 24 (first normal), 48 (normal selected) and 72 (normal);
 * item icons are drawn at {@code (+4, -17)}. Hover shows the tab title.
 *
 * <p>Tab art from LevelZ / JobsAddon / PartyAddon is GPL and is NOT copied:
 * the four tabs use vanilla item icons (bundle, iron sword, anvil, player
 * head) to reproduce the gallery silhouette legally.
 */
@Environment(EnvType.CLIENT)
public final class TabStrip {

    public enum Tab {
        INVENTORY("Inventory [E]", Items.BUNDLE),
        SKILLS("Skills [K]", Items.IRON_SWORD),
        JOBS("Jobs [J]", Items.ANVIL),
        PARTY("Party [P]", Items.PLAYER_HEAD);

        public final String label;
        public final Item icon;

        Tab(String label, Item icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    /** Verbatim LibZ 1.0.3 {@code assets/libz/textures/gui/icons.png} (MIT), 256x256. */
    private static final Identifier TAB_SHEET =
            Identifier.fromNamespaceAndPath("hearthwind", "textures/gui/tab_sheet.png");

    public static final int TAB_WIDTH = 24;
    public static final int TAB_PITCH = 25;

    private TabStrip() {}

    public static int tabX(int panelX, int index) {
        return panelX + index * TAB_PITCH;
    }

    /**
     * Draws the four-tab strip above {@code panelX/panelY}. {@code active} may
     * be null (no selected tab).
     */
    public static void draw(GuiGraphicsExtractor graphics, int panelX, int panelY,
            Tab active, int mouseX, int mouseY) {
        Tab hovered = null;
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            boolean selected = tab == active;
            boolean first = i == 0;
            int tabX = tabX(panelX, i);

            int u = first ? 24 : 72;
            if (selected) {
                u -= 24;
            }
            int height = selected ? 27 : (first ? 25 : 21);
            int tabY = selected ? panelY - 23 : panelY - 21;

            graphics.blit(RenderPipelines.GUI_TEXTURED, TAB_SHEET, tabX, tabY, u, 0,
                    TAB_WIDTH, height, 256, 256, 0xFFFFFFFF);
            graphics.item(new ItemStack(tab.icon), tabX + 4, panelY - 17);

            if (!selected && isOver(i, panelX, panelY, mouseX, mouseY, false)) {
                hovered = tab;
            }
        }
        if (hovered != null) {
            graphics.setTooltipForNextFrame(Component.literal(hovered.label), mouseX, mouseY);
        }
    }

    /** Returns the tab under the cursor, or null. Mirrors LibZ click bounds. */
    public static Tab clicked(double mouseX, double mouseY, int panelX, int panelY, Tab active) {
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            if (isOver(i, panelX, panelY, mouseX, mouseY, tabs[i] == active)) {
                return tabs[i];
            }
        }
        return null;
    }

    /**
     * LibZ {@code isPointWithinBounds} reproduction: X spans the 24 px tab
     * body, Y spans either the raised (selected) or resting strip.
     */
    private static boolean isOver(int index, int panelX, int panelY, double mouseX, double mouseY, boolean selected) {
        double localX = mouseX - panelX - index * TAB_PITCH;
        double localY = mouseY - panelY;
        int minY = selected ? -25 : -21;
        return localX >= 0 && localX < TAB_WIDTH && localY >= minY && localY < 0;
    }

    /** Opens the destination panel for a given tab. */
    public static void open(Tab tab) {
        Minecraft mc = Minecraft.getInstance();
        switch (tab) {
            case INVENTORY -> {
                if (mc.player != null) {
                    mc.setScreenAndShow(new InventoryScreen(mc.player));
                }
            }
            case SKILLS -> mc.setScreenAndShow(new SkillsScreen());
            case JOBS -> mc.setScreenAndShow(new JobsScreen());
            case PARTY -> mc.setScreenAndShow(new PartyScreen());
        }
    }
}
