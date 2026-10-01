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
 */
@Environment(EnvType.CLIENT)
public final class TabStrip {

    public enum Tab {
        INVENTORY("container.crafting", Items.BUNDLE),
        SKILLS("screen.hearthwind.skills", Items.IRON_SWORD),
        JOBS("screen.hearthwind.jobs", Items.IRON_AXE),
        PARTY("screen.hearthwind.party", Items.ARMOR_STAND),
        /**
         * The fifth tab the reference shows. InmisAddon registers it at
         * preferred position 99 (so it always lands last) and only shows it
         * when a backpack is equipped and holding something - an empty pack on
         * your back is not worth a tab. Its icon is the backpack itself.
         */
        BACKPACK("screen.hearthwind.backpack", null);

        /** Translation key; the reference uses translatable titles too. */
        public final String titleKey;
        /** Null for the backpack tab, whose icon is the equipped stack. */
        public final Item icon;

        Tab(String titleKey, Item icon) {
            this.titleKey = titleKey;
            this.icon = icon;
        }

        public Component title() {
            return Component.translatable(titleKey);
        }
    }

    /**
     * The backpack tab when the local player has a backpack with something in
     * it, else null. Same condition as the reference's BackpackTab.
     */
    public static Tab backpackTab(Minecraft mc) {
        if (mc.player == null) {
            return null;
        }
        ItemStack backpack = draylar.inmis.compat.TrinketsCompat.findEquippedBackpack(mc.player);
        if (backpack == null || backpack.isEmpty() || draylar.inmis.Inmis.isBackpackEmpty(backpack)) {
            return null;
        }
        return Tab.BACKPACK;
    }

    /**
     * The tabs to draw, in order. The backpack tab is conditional, so the
     * strip is 4 wide normally and 5 once the player is carrying something.
     * BACKPACK is declared last precisely so dropping it is a tail copy; a
     * gametest pins the 4-wide case, because returning all five with no
     * backpack would draw a tab with no icon for every player not wearing one.
     */
    public static Tab[] visibleTabs(Minecraft mc) {
        Tab[] all = Tab.values();
        if (backpackTab(mc) == null) {
            return java.util.Arrays.copyOf(all, all.length - 1);
        }
        return all;
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
     *
     * <p>The strip floats above the panel in every Aged capture: the Jobs and
     * Level panels are 200x215 and the tabs sit on the 21 rows directly above
     * the panel, with the selected tab raised 2 px and merging into the panel's
     * top edge.
     */
    public static void draw(GuiGraphicsExtractor graphics, int panelX, int panelY,
            Tab active, int mouseX, int mouseY) {
        drawStrip(graphics, panelX, panelY - 21, 2, active, mouseX, mouseY);
    }

    private static void drawStrip(GuiGraphicsExtractor graphics, int panelX, int restTop, int raisedBy,
            Tab active, int mouseX, int mouseY) {
        Tab hovered = null;
        Tab[] tabs = visibleTabs(Minecraft.getInstance());
        ItemStack backpackIcon = active == Tab.BACKPACK ? equippedBackpack() : ItemStack.EMPTY;
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
            if (tab == Tab.BACKPACK) {
                // The reference draws the equipped backpack as this tab's icon.
                graphics.item(backpackIcon, tabX + 4, restTop + 4);
            } else {
                graphics.item(new ItemStack(tab.icon), tabX + 4, restTop + 4);
            }

            if (!selected && isOver(i, tabs.length, panelX, restTop, raisedBy, mouseX, mouseY, false)) {
                hovered = tab;
            }
        }
        if (hovered != null) {
            graphics.setTooltipForNextFrame(hovered.title(), mouseX, mouseY);
        }
    }

    private static ItemStack equippedBackpack() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = draylar.inmis.compat.TrinketsCompat.findEquippedBackpack(mc.player);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    /** Returns the tab under the cursor, or null. Mirrors LibZ click bounds. */
    public static Tab clicked(double mouseX, double mouseY, int panelX, int panelY, Tab active) {
        return clickStrip(mouseX, mouseY, panelX, panelY - 21, 2, active);
    }

    private static Tab clickStrip(double mouseX, double mouseY, int panelX, int restTop, int raisedBy,
            Tab active) {
        Tab[] tabs = visibleTabs(Minecraft.getInstance());
        for (int i = 0; i < tabs.length; i++) {
            if (isOver(i, tabs.length, panelX, restTop, raisedBy, mouseX, mouseY, tabs[i] == active)) {
                return tabs[i];
            }
        }
        return null;
    }

    /**
     * LibZ {@code isPointWithinBounds} reproduction: X spans the 24 px tab
     * body, Y spans the tab's own box (raised tabs start higher).
     *
     * <p>Every UNSELECTED tab hit-tests as 21 rows tall, including the first:
     * the reference's own bounds say so, even though the first tab is drawn 25
     * tall. Ours used to swallow 4 extra rows of the panel's top edge, which
     * meant the Inventory tab could steal clicks from the first row of a panel
     * below it.
     */
    private static boolean isOver(int index, int tabCount, int panelX, int restTop, int raisedBy,
            double mouseX, double mouseY, boolean selected) {
        double localX = mouseX - panelX - index * TAB_PITCH;
        double localY = mouseY - restTop;
        boolean first = index == 0;
        int height = selected ? 27 : 21;
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
            case BACKPACK -> {
                ItemStack pack = equippedBackpack();
                if (!pack.isEmpty()) {
                    draylar.inmis.item.BackpackItem.openScreen(mc.player, pack);
                }
            }
        }
    }
}
