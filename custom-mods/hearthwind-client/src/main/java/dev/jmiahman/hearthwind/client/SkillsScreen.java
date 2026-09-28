package dev.jmiahman.hearthwind.client;

import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Aged 3.1.2 / LevelZ SkillScreen rebuild (clean-room: layout studied from
 * the GPL LevelZ source, reimplemented on our data and 26.2 APIs).
 *
 * <p>200x215 panel centred, "&lt;Name&gt; Skills" title centred at x+120/y+7,
 * player model preview at the left, six attribute readouts in a 3x2 grid,
 * "Level N   Points N" line, segmented XP bar with "Xp n / next" text and
 * twelve skill rows (two columns, six rows) with [+1] buttons.
 */
@Environment(EnvType.CLIENT)
public class SkillsScreen extends HearthwindPanelScreen {

    /** LevelZ enum order: Health, Strength, Agility, Defense, Stamina, Luck | Archery, Trade, Smithing, Mining, Farming, Alchemy. */
    private static final String[] SKILL_ORDER = {
            "health", "strength", "agility", "defense", "stamina", "luck",
            "archery", "trade", "smithing", "mining", "farming", "alchemy" };

    private static final Item[] SKILL_ICONS = {
            Items.GOLDEN_APPLE, Items.IRON_SWORD, Items.FEATHER, Items.IRON_CHESTPLATE,
            Items.COOKED_BEEF, Items.RABBIT_FOOT,
            Items.BOW, Items.EMERALD, Items.ANVIL, Items.IRON_PICKAXE,
            Items.WHEAT, Items.BREWING_STAND };

    private static final String[] SKILL_TIPS = {
            "Increases HP", "Increases Melee Damage", "Increases Movement Speed",
            "Increases Protection", "Decreases Exhaustion", "Increases Loot Value",
            "Increases Bow Damage", "Decreases Trade Price", "Unlocks Smithing Items",
            "Unlocks Tools", "Unlocks Farming Items", "Unlocks Alchemy Items" };

    private static final Item[] STAT_ICONS = {
            Items.GOLDEN_APPLE, Items.IRON_CHESTPLATE,
            Items.FEATHER, Items.IRON_SWORD,
            Items.COOKED_BEEF, Items.RABBIT_FOOT };
    private static final String[] STAT_LABELS = {
            "Health", "Defense", "Agility", "Strength", "Stamina", "Luck" };
    private static final String[] STAT_TIPS = {
            "Increases HP", "Increases Protection", "Increases Movement Speed",
            "Increases Melee Damage", "Decreases Exhaustion", "Increases Loot Value" };

    private boolean showHelp;
    private boolean showAttributes;

    public SkillsScreen() {
        super(Component.translatable("screen.hearthwind.skills"));
    }

    /** Rail toggle for the attribute slide-out (called by tests too). */
    public void toggleAttributes() {
        this.showAttributes = !this.showAttributes;
    }

    public boolean attributesOpen() {
        return this.showAttributes;
    }

    /** Rail gate buttons: open a restriction list (called by tests too). */
    public void openRestrictions(SkillRestrictionScreen.ListKind kind) {
        Minecraft.getInstance().setScreenAndShow(new SkillRestrictionScreen(kind));
    }

    @Override
    protected TabStrip.Tab activeTab() {
        return TabStrip.Tab.SKILLS;
    }

    @Override
    protected void drawContent(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Map<String, Integer> levels = ClientSkillData.knownLevels();
        String playerName = player != null ? player.getName().getString() : "Player";

        // Title centred at x+120 like the reference (content column centre).
        Component title = Component.translatable("screen.hearthwind.skills.title", playerName);
        graphics.text(font, title, this.x + 120 - font.width(title) / 2, this.y + 7, INK, false);

        // Player model preview (26.2 static helper; box + scale + y offset).
        if (player != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                    this.x + 8, this.y + 14, this.x + 62, this.y + 100,
                    40, 0.0625f, mouseX, mouseY, player);
        }

        drawStats(graphics, font, levels, player, mouseX, mouseY);

        int overall = ClientSkillData.overallLevel();
        String levelText = "Level " + overall;
        graphics.text(font, levelText, this.x + 91 - font.width(levelText) / 2, this.y + 56, INK, false);

        int points = ClientSkillData.points();
        String pointsText = "Points " + points;
        graphics.text(font, pointsText, this.x + 156 - font.width(pointsText) / 2, this.y + 56, INK, false);

        // XP bar: action-XP progress toward the next overall level, priced
        // with the LevelZ curve (25 at level 0, matching the reference).
        // Each overall level banks one skill point; hearts and the other
        // bonuses follow the levels you buy, never the bar.
        int next = nextCost(overall);
        long earned = xpForLevel(overall);
        int current = (int) Math.max(0L, ClientSkillData.totalXp() - earned);
        float progress = next > 0 ? Math.min(1f, current / (float) next) : 0f;
        drawSegmentedBar(graphics, this.x + 58, this.y + 68, 131, 5, progress);

        String xpText = "Xp " + Math.min(current, next) + " / " + next;
        graphics.text(font, xpText, this.x + 123 - font.width(xpText) / 2, this.y + 78, INK, false);

        drawHelpButton(graphics, font, mouseX, mouseY);
        drawRailButtons(graphics, font, mouseX, mouseY);
        drawSkillRows(graphics, font, levels, player, mouseX, mouseY);

        if (this.showAttributes) {
            drawAttributesPanel(graphics, font, levels, player);
        }
        if (this.showHelp) {
            drawHelpOverlay(graphics, font);
        }
    }

    /**
     * Aged's right icon rail (LevelZ LevelScreen, x+178): attributes toggle
     * plus the mining and crafting gate buttons that open
     * {@link SkillRestrictionScreen}. Our stat/skill art is item stand-ins
     * (upstream sprites are GPL).
     */
    private void drawRailButtons(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        drawIconButton(graphics, mouseX, mouseY, 16, Items.EXPERIENCE_BOTTLE,
                this.showAttributes ? "Hide attributes" : "Show attributes");
        drawIconButton(graphics, mouseX, mouseY, 34, Items.IRON_PICKAXE, "Mining restrictions");
        drawIconButton(graphics, mouseX, mouseY, 52, Items.CRAFTING_TABLE, "Crafting restrictions");
    }

    private void drawIconButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int relY,
            Item icon, String tip) {
        int bx = this.x + 177;
        int by = this.y + relY;
        boolean hover = mouseX >= bx && mouseX < bx + 16 && mouseY >= by && mouseY < by + 16;
        graphics.fill(bx, by, bx + 16, by + 16, hover ? 0x40FFFFFF : 0x30000000);
        graphics.item(new ItemStack(icon), bx, by);
        if (hover) {
            graphics.setTooltipForNextFrame(Component.literal(tip), mouseX, mouseY);
        }
    }

    /** 82 px slide-out panel with live attribute values, Aged-style. */
    private void drawAttributesPanel(GuiGraphicsExtractor graphics, Font font, Map<String, Integer> levels,
            LocalPlayer player) {
        int px = this.x + 94;
        int py = this.y + 14;
        int pw = 82;
        int ph = 192;
        graphics.fill(px, py, px + pw, py + ph, 0xF01E1E1E);
        graphics.fill(px, py, px + pw, py + 1, 0xFF9A9A9A);
        graphics.fill(px, py, px + 1, py + ph, 0xFF9A9A9A);
        Component title = Component.literal("Attributes");
        graphics.text(font, title, px + pw / 2 - font.width(title) / 2, py + 4, 0xFFE0E0E0, false);

        String[] values = statValues(levels, player);
        for (int i = 0; i < 6; i++) {
            int rowY = py + 18 + i * 20;
            graphics.item(new ItemStack(STAT_ICONS[i]), px + 4, rowY);
            graphics.text(font, STAT_LABELS[i], px + 22, rowY + 2, 0xFFB0B0B0, false);
            graphics.text(font, values[i], px + 22, rowY + 10, 0xFFFFFFFF, false);
        }
    }

    private void drawStats(GuiGraphicsExtractor graphics, Font font, Map<String, Integer> levels,
            LocalPlayer player, int mouseX, int mouseY) {
        String[] values = statValues(levels, player);

        int[] columnX = { 58, 108, 155 };
        int[] valueX = { 76, 126, 173 };
        String hovered = null;
        for (int i = 0; i < 6; i++) {
            int col = i % 3;
            int row = i / 3;
            int iconY = this.y + 18 + row * 18;
            graphics.item(new ItemStack(STAT_ICONS[i]), this.x + columnX[col], iconY);
            graphics.text(font, values[i], this.x + valueX[col], iconY + 4, INK, false);
            if (isOver(columnX[col], 18 + row * 18, 34, 16, mouseX, mouseY)) {
                hovered = STAT_LABELS[i] + ": " + STAT_TIPS[i];
            }
        }
        if (hovered != null) {
            graphics.setTooltipForNextFrame(Component.literal(hovered), mouseX, mouseY);
        }
    }

    /** Live attribute readouts shared by the 3x2 grid and the slide-out. */
    private String[] statValues(Map<String, Integer> levels, LocalPlayer player) {
        var bonuses = dev.jmiahman.hearthwind.skills.SkillsConfig.get().bonuses;
        int health = levels.getOrDefault("health", 0);
        int strength = levels.getOrDefault("strength", 0);
        int agility = levels.getOrDefault("agility", 0);
        int defense = levels.getOrDefault("defense", 0);
        int luck = levels.getOrDefault("luck", 0);
        return new String[] {
                String.valueOf(Math.round(bonuses.baseStartingHealth + health * bonuses.healthHpPerLevel)),
                formatStat(defense * bonuses.defenseArmorPerLevel),
                formatStat((bonuses.agilityBaseMovement + agility * bonuses.agilitySpeedFractionPerLevel) * 10.0),
                formatStat(1.0 + strength * bonuses.strengthDamagePerLevel),
                String.valueOf(player != null ? player.getFoodData().getFoodLevel() : 0),
                formatStat(luck * bonuses.luckPerLevel) };
    }

    private void drawSkillRows(GuiGraphicsExtractor graphics, Font font, Map<String, Integer> levels,
            LocalPlayer player, int mouseX, int mouseY) {
        int max = maxLevel();
        int points = ClientSkillData.points();
        String hovered = null;
        for (int i = 0; i < SKILL_ORDER.length; i++) {
            int col = i / 6;
            int row = i % 6;
            int iconX = this.x + 15 + col * 90;
            int baseY = this.y + 94 + row * 20;
            int level = levels.getOrDefault(SKILL_ORDER[i], 0);

            graphics.item(new ItemStack(SKILL_ICONS[i]), iconX, baseY);

            String levelText = level + "/" + max;
            graphics.text(font, levelText, this.x + 57 + col * 90 - font.width(levelText) / 2,
                    baseY + 4, INK, false);

            boolean canSpend = (points > 0 || (player != null && player.getAbilities().instabuild)) && level < max;
            drawPlusButton(graphics, font, this.x + 83 + col * 90, baseY + 2, canSpend, mouseX, mouseY);

            String name = SKILL_ORDER[i].substring(0, 1).toUpperCase() + SKILL_ORDER[i].substring(1);
            if (isOver(15 + col * 90, 94 + row * 20, 60, 16, mouseX, mouseY)) {
                hovered = name + " (Lv " + level + "/" + max + "): " + SKILL_TIPS[i];
            }
        }
        if (hovered != null) {
            graphics.setTooltipForNextFrame(Component.literal(hovered), mouseX, mouseY);
        }
    }

    private void drawPlusButton(GuiGraphicsExtractor graphics, Font font, int bx, int by,
            boolean canSpend, int mouseX, int mouseY) {
        boolean hover = mouseX >= bx && mouseX < bx + 13 && mouseY >= by && mouseY < by + 13;
        int face = !canSpend ? 0xFF8B8B8B : hover ? 0xFFE0E0E0 : 0xFFC6C6C6;
        graphics.fill(bx, by, bx + 13, by + 13, 0xFF373737);
        graphics.fill(bx + 1, by + 1, bx + 12, by + 12, face);
        graphics.fill(bx + 1, by + 1, bx + 12, by + 2, 0xFFFFFFFF);
        graphics.fill(bx + 1, by + 1, bx + 2, by + 12, 0xFFFFFFFF);
        graphics.text(font, "+", bx + 4, by + 2, canSpend ? 0xFF2F2F2F : 0xFF6E6E6E, false);
    }

    private void drawHelpButton(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        int bx = this.x + 178;
        int by = this.y + 74;
        boolean hover = mouseX >= bx && mouseX < bx + 13 && mouseY >= by && mouseY < by + 13;
        graphics.fill(bx, by, bx + 13, by + 13, 0xFF373737);
        graphics.fill(bx + 1, by + 1, bx + 12, by + 12, hover ? 0xFFE0E0E0 : 0xFFC6C6C6);
        graphics.text(font, "?", bx + (13 - font.width("?")) / 2, by + 2, 0xFF2F2F2F, false);
        if (hover) {
            graphics.setTooltipForNextFrame(Component.literal("Skill help"), mouseX, mouseY);
        }
    }

    private void drawHelpOverlay(GuiGraphicsExtractor graphics, Font font) {
        graphics.fill(this.x + 3, this.y + 3, this.x + PANEL_W - 3, this.y + PANEL_H - 3, 0xE0202020);
        int tx = this.x + 12;
        int ty = this.y + 24;
        String[] lines = {
                "Skills",
                "",
                "The XP bar fills as you mine, farm, fight,",
                "brew, trade and craft; each level it reaches",
                "banks one skill point.",
                "",
                "Spend a point on [+] to raise a skill by one.",
                "Your hearts, damage and speed bonuses follow",
                "the level you bought.",
                "",
                "Higher skills unlock better tools, weapons,",
                "armor and stations through the content gates.",
                "",
                "Click the ? again to close this page." };
        for (String line : lines) {
            graphics.text(font, line, tx, ty, 0xFFE0E0E0, false);
            ty += 12;
        }
    }

    @Override
    protected boolean onContentClick(MouseButtonEvent event) {
        if (event.button() != 0) {
            return false;
        }
        if (event.x() >= this.x + 177 && event.x() < this.x + 193) {
            double relY = event.y() - this.y;
            if (relY >= 16 && relY < 32) {
                this.click();
                this.toggleAttributes();
                return true;
            }
            if (relY >= 34 && relY < 50) {
                this.click();
                this.openRestrictions(SkillRestrictionScreen.ListKind.MINING);
                return true;
            }
            if (relY >= 52 && relY < 68) {
                this.click();
                this.openRestrictions(SkillRestrictionScreen.ListKind.CRAFTING);
                return true;
            }
        }
        if (this.showAttributes && event.x() >= this.x + 94 && event.x() < this.x + 176) {
            this.click();
            this.showAttributes = false;
            return true;
        }
        if (event.x() >= this.x + 178 && event.x() < this.x + 191
                && event.y() >= this.y + 74 && event.y() < this.y + 87) {
            this.click();
            this.showHelp = !this.showHelp;
            return true;
        }
        if (this.showHelp) {
            this.click();
            this.showHelp = false;
            return true;
        }
        if (openSkillInfo(event)) {
            return true;
        }
        return spendSkillPoint(event);
    }

    /** Aged parity (LevelZ LevelScreen): clicking a skill's icon opens its detail page. */
    private boolean openSkillInfo(MouseButtonEvent event) {
        for (int i = 0; i < SKILL_ORDER.length; i++) {
            int col = i / 6;
            int row = i % 6;
            int ix = this.x + 15 + col * 90;
            int iy = this.y + 94 + row * 20;
            if (event.x() >= ix && event.x() < ix + 16 && event.y() >= iy && event.y() < iy + 16) {
                this.click();
                Minecraft.getInstance().setScreenAndShow(new SkillInfoScreen(SKILL_ORDER[i]));
                return true;
            }
        }
        return false;
    }

    private boolean spendSkillPoint(MouseButtonEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }
        int points = ClientSkillData.points();
        if (points <= 0 && !player.getAbilities().instabuild) {
            return false;
        }
        Map<String, Integer> levels = ClientSkillData.knownLevels();
        for (int i = 0; i < SKILL_ORDER.length; i++) {
            int col = i / 6;
            int row = i % 6;
            int bx = this.x + 83 + col * 90;
            int by = this.y + 96 + row * 20;
            if (event.x() >= bx && event.x() < bx + 13 && event.y() >= by && event.y() < by + 13) {
                int level = levels.getOrDefault(SKILL_ORDER[i], 0);
                if (level < maxLevel()) {
                    this.click();
                    ClientSkillData.onSkillUp(SKILL_ORDER[i], level + 1);
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                            new dev.jmiahman.hearthwind.survival.SkillUpPayload(SKILL_ORDER[i], level + 1));
                    return true;
                }
            }
        }
        return false;
    }

    static String skillName(String skill) {
        String id = normalizeSkill(skill);
        return id.substring(0, 1).toUpperCase() + id.substring(1);
    }

    static Item skillIcon(String skill) {
        String id = normalizeSkill(skill);
        for (int i = 0; i < SKILL_ORDER.length; i++) {
            if (SKILL_ORDER[i].equals(id)) {
                return SKILL_ICONS[i];
            }
        }
        return Items.BOOK;
    }

    static String skillTip(String skill) {
        String id = normalizeSkill(skill);
        for (int i = 0; i < SKILL_ORDER.length; i++) {
            if (SKILL_ORDER[i].equals(id)) {
                return SKILL_TIPS[i];
            }
        }
        return "Improves your survival skills";
    }

    private static String normalizeSkill(String skill) {
        return skill == null || skill.isBlank() ? "health" : skill.toLowerCase(java.util.Locale.ROOT);
    }

    private static int maxLevel() {
        try {
            return dev.jmiahman.hearthwind.skills.SkillXp.maxLevel();
        } catch (Throwable t) {
            return 30;
        }
    }

    private static int nextCost(int level) {
        try {
            return dev.jmiahman.hearthwind.skills.SkillXp.nextCost(Math.min(level, maxLevel() - 1));
        } catch (Throwable t) {
            return 25;
        }
    }

    /** Cumulative xp to reach {@code level}; mirrors the server curve. */
    private static long xpForLevel(int level) {
        try {
            return dev.jmiahman.hearthwind.skills.SkillXp.xpForLevel(Math.min(level, maxLevel() - 1));
        } catch (Throwable t) {
            return 0L;
        }
    }

    /** Draws a 1 px tracked bar with 10 px segment ticks, gallery-style. */
    static void drawSegmentedBar(GuiGraphicsExtractor graphics, int bx, int by, int w, int h, float progress) {
        graphics.fill(bx, by, bx + w, by + h, 0xFF373737);
        graphics.fill(bx + 1, by + 1, bx + w - 1, by + h - 1, 0xFF4A4A50);
        int fill = Math.round((w - 2) * Math.max(0f, Math.min(1f, progress)));
        if (fill > 0) {
            graphics.fill(bx + 1, by + 1, bx + 1 + fill, by + h - 1, 0xFF6E9E3E);
        }
        for (int tick = 10; tick < w; tick += 10) {
            graphics.fill(bx + tick, by + 1, bx + tick + 1, by + h - 1, 0xFF373737);
        }
    }

    /** 0.90 -> "0.9", 1.00 -> "1.0", 0.00 -> "0.0", 0.85 -> "0.85". */
    static String formatStat(double value) {
        String text = String.format(java.util.Locale.ROOT, "%.2f", value);
        if (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
