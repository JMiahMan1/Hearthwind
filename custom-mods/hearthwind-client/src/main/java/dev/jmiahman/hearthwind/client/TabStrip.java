package dev.jmiahman.hearthwind.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Authentic 4-tab strip matching Aged modpack parity (Inventory, Skills, Jobs, Nutrients).
 * Rendered from tab sprites: selected tab is raised and seamlessly merged into the panel;
 * unselected tabs sit lower with a dark border.
 */
@Environment(EnvType.CLIENT)
public final class TabStrip {

    public enum Tab {
        INVENTORY("Inventory [E]", Items.BUNDLE),
        SKILLS("Skills [K]", Items.IRON_SWORD),
        JOBS("Jobs [J]", Items.IRON_AXE),
        PARTY("Party [P]", Items.PLAYER_HEAD);

        public final String label;
        public final Item icon;

        Tab(String label, Item icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    private static final Identifier TAB_INACTIVE =
            Identifier.fromNamespaceAndPath("hearthwind", "tab/tab_inactive");
    private static final Identifier TAB_ACTIVE =
            Identifier.fromNamespaceAndPath("hearthwind", "tab/tab_active");
    private static final Identifier TAB_FIRST_ACTIVE =
            Identifier.fromNamespaceAndPath("hearthwind", "tab/tab_first_active");

    public static final int TAB_WIDTH = 24;
    public static final int TAB_STRIDE = 25;

    private TabStrip() {}

    public static int tabX(int panelX, int index) {
        return panelX + index * TAB_STRIDE;
    }

    /** Draws the tab strip above the panel with true tab_sheet texture. */
    public static int draw(GuiGraphicsExtractor graphics, Font font, int panelX, int panelY,
            Tab active, double mouseX, double mouseY) {
        Tab[] tabs = Tab.values();
        Tab hoveredTab = null;

        // 1. Draw unselected tabs first
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            if (tab == active) {
                continue;
            }
            int x = tabX(panelX, i);
            int drawY = panelY - 22;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TAB_INACTIVE, x, drawY, TAB_WIDTH, 25);
            graphics.item(new ItemStack(tab.icon), x + 4, drawY + 4);

            if (isOver(i, panelX, panelY, mouseX, mouseY, false)) {
                hoveredTab = tab;
            }
        }

        // 2. Draw active tab on top (raised, seamlessly merges into panel)
        if (active != null) {
            int activeIdx = active.ordinal();
            int activeX = tabX(panelX, activeIdx);
            int activeDrawY = panelY - 24;
            Identifier activeSprite = (activeIdx == 0) ? TAB_FIRST_ACTIVE : TAB_ACTIVE;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, activeSprite, activeX, activeDrawY, TAB_WIDTH, 27);
            graphics.item(new ItemStack(active.icon), activeX + 4, activeDrawY + 6);

            if (isOver(activeIdx, panelX, panelY, mouseX, mouseY, true)) {
                hoveredTab = active;
            }
        }

        // 3. Tooltip on hover
        if (hoveredTab != null) {
            graphics.setTooltipForNextFrame(Component.literal(hoveredTab.label), (int) mouseX, (int) mouseY);
        }

        return tabs.length * TAB_STRIDE - 1;
    }

    private static boolean isOver(int index, int panelX, int panelY, double mx, double my, boolean active) {
        int x = tabX(panelX, index);
        int top = panelY - (active ? 24 : 22);
        return mx >= x && mx < x + TAB_WIDTH && my >= top && my < panelY;
    }

    /** Returns the tab clicked by the mouse, or null. */
    public static Tab clicked(double mouseX, double mouseY, int panelX, int panelY) {
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            int x = tabX(panelX, i);
            int top = panelY - 24;
            if (mouseX >= x && mouseX < x + TAB_WIDTH && mouseY >= top && mouseY < panelY) {
                return tabs[i];
            }
        }
        return null;
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
            case SKILLS -> mc.setScreenAndShow(new SurvivalInfoScreen(SurvivalInfoScreen.Kind.SKILLS));
            case JOBS -> mc.setScreenAndShow(new SurvivalInfoScreen(SurvivalInfoScreen.Kind.JOBS));
            case PARTY -> mc.setScreenAndShow(new SurvivalInfoScreen(SurvivalInfoScreen.Kind.PARTY));
        }
    }
}
