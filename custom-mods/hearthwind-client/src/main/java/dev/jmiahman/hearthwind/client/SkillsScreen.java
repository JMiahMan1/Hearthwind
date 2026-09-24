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
public class SkillsScreen extends AgedPanelScreen {

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

    public SkillsScreen() {
        super(Component.translatable("screen.hearthwind.skills"));
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

        int points = player != null ? player.experienceLevel : 0;
        String pointsText = "Points " + points;
        graphics.text(font, pointsText, this.x + 156 - font.width(pointsText) / 2, this.y + 56, INK, false);

        // XP bar: vanilla XP progress toward the next spendable point, priced
        // with the LevelZ curve (25 at level 0, matching the reference).
        int next = nextCost(overall);
        float progress = player != null ? player.experienceProgress : 0f;
        int current = Math.round(progress * next);
        drawSegmentedBar(graphics, this.x + 58, this.y + 68, 131, 5, progress);

        String xpText = "Xp " + current + " / " + next;
        graphics.text(font, xpText, this.x + 123 - font.width(xpText) / 2, this.y + 78, INK, false);

        drawHelpButton(graphics, font, mouseX, mouseY);
        drawSkillRows(graphics, font, levels, player, mouseX, mouseY);

        if (this.showHelp) {
            drawHelpOverlay(graphics, font);
        }
    }

    private void drawStats(GuiGraphicsExtractor graphics, Font font, Map<String, Integer> levels,
            LocalPlayer player, int mouseX, int mouseY) {
        var bonuses = dev.jmiahman.hearthwind.skills.SkillsConfig.get().bonuses;
        int health = levels.getOrDefault("health", 0);
        int strength = levels.getOrDefault("strength", 0);
        int agility = levels.getOrDefault("agility", 0);
        int defense = levels.getOrDefault("defense", 0);
        int luck = levels.getOrDefault("luck", 0);

        String[] values = {
                String.valueOf(Math.round(bonuses.baseStartingHealth + health * bonuses.healthHpPerLevel)),
                formatStat(defense * bonuses.defenseArmorPerLevel),
                formatStat((bonuses.agilityBaseMovement + agility * bonuses.agilitySpeedFractionPerLevel) * 10.0),
                formatStat(1.0 + strength * bonuses.strengthDamagePerLevel),
                String.valueOf(player != null ? player.getFoodData().getFoodLevel() : 0),
                formatStat(luck * bonuses.luckPerLevel) };

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

    private void drawSkillRows(GuiGraphicsExtractor graphics, Font font, Map<String, Integer> levels,
            LocalPlayer player, int mouseX, int mouseY) {
        int max = maxLevel();
        int points = player != null ? player.experienceLevel : 0;
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
                "Your skills level up as you play: mine, farm,",
                "fight, brew, trade and craft to earn skill XP.",
                "",
                "Points are your experience levels. Spend one",
                "on the [+] button to raise any skill by 1.",
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
        return spendSkillPoint(event);
    }

    private boolean spendSkillPoint(MouseButtonEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }
        int points = player.experienceLevel;
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
