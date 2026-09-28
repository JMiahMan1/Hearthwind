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
 * the four tabs use vanilla item icons (bundle, iron sword, iron axe, armor
 * stand) to reproduce the gallery's bag/sword/axe/figure silhouette legally.
 *
 * <p>Two placements exist: {@link #draw} keeps the LibZ float above the panel
 * (the inventory screen, which has no band) and {@link #drawInBand} drops the
 * same strip one pixel into the panel's black band, which is where the Aged
 * Jobs and Level captures put it.
 */
@Environment(EnvType.CLIENT)
public final class TabStrip {

    public enum Tab {
        INVENTORY("Inventory [E]", Items.BUNDLE),
        SKILLS("Skills [K]", Items.IRON_SWORD),
        JOBS("Jobs [J]", Items.IRON_AXE),
        PARTY("Party [P]", Items.ARMOR_STAND);

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
        drawStrip(graphics, panelX, panelY - 21, 2, active, mouseX, mouseY);
    }

    /**
     * Draws the strip inside a panel's black tab band: every tab starts one
     * pixel above the band and the selected one is only taller, which is what
     * the Aged Jobs and Level captures show (the tabs sit in the band, not
     * above the panel).
     */
    public static void drawInBand(GuiGraphicsExtractor graphics, int panelX, int panelTop,
            Tab active, int mouseX, int mouseY) {
        drawStrip(graphics, panelX, panelTop - 1, 0, active, mouseX, mouseY);
    }

    private static void drawStrip(GuiGraphicsExtractor graphics, int panelX, int restTop, int raisedBy,
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
            int tabY = (selected ? restTop - raisedBy : restTop);

            graphics.blit(RenderPipelines.GUI_TEXTURED, TAB_SHEET, tabX, tabY, u, 0,
                    TAB_WIDTH, height, 256, 256, 0xFFFFFFFF);
            graphics.item(new ItemStack(tab.icon), tabX + 4, restTop + 4);

            if (!selected && isOver(i, panelX, restTop, raisedBy, mouseX, mouseY, false)) {
                hovered = tab;
            }
        }
        if (hovered != null) {
            graphics.setTooltipForNextFrame(Component.literal(hovered.label), mouseX, mouseY);
        }
    }

    /** Returns the tab under the cursor, or null. Mirrors LibZ click bounds. */
    public static Tab clicked(double mouseX, double mouseY, int panelX, int panelY, Tab active) {
        return clickStrip(mouseX, mouseY, panelX, panelY - 21, 2, active);
    }

    /** Banded variant of {@link #clicked}: {@code panelTop} is the band's top. */
    public static Tab clickedInBand(double mouseX, double mouseY, int panelX, int panelTop, Tab active) {
        return clickStrip(mouseX, mouseY, panelX, panelTop - 1, 0, active);
    }

    private static Tab clickStrip(double mouseX, double mouseY, int panelX, int restTop, int raisedBy,
            Tab active) {
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            if (isOver(i, panelX, restTop, raisedBy, mouseX, mouseY, tabs[i] == active)) {
                return tabs[i];
            }
        }
        return null;
    }

    /**
     * LibZ {@code isPointWithinBounds} reproduction: X spans the 24 px tab
     * body, Y spans the tab's own box (raised tabs start higher).
     */
    private static boolean isOver(int index, int panelX, int restTop, int raisedBy,
            double mouseX, double mouseY, boolean selected) {
        double localX = mouseX - panelX - index * TAB_PITCH;
        double localY = mouseY - restTop;
        int height = selected ? 27 : (index == 0 ? 25 : 21);
        int top = selected ? -raisedBy : 0;
        return localX >= 0 && localX < TAB_WIDTH && localY >= top && localY < top + height;
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
