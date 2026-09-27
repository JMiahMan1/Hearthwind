package dev.jmiahman.hearthwind.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class SkillInfoScreen extends HearthwindPanelScreen {
    private final String skillId;
    private final Component title;

    public SkillInfoScreen(String skillId) {
        super(Component.translatable("screen.hearthwind.skill_info"));
        this.skillId = skillId == null || skillId.isBlank() ? "health" : skillId.toLowerCase(java.util.Locale.ROOT);
        this.title = Component.literal(SkillsScreen.skillName(this.skillId));
    }

    @Override
    protected TabStrip.Tab activeTab() {
        return TabStrip.Tab.SKILLS;
    }

    @Override
    protected void drawContent(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        int level = ClientSkillData.knownLevels().getOrDefault(this.skillId, 0);
        int max = maxLevel();
        graphics.text(font, this.title, this.x + 100 - font.width(this.title) / 2, this.y + 8, INK, false);
        graphics.item(new ItemStack(SkillsScreen.skillIcon(this.skillId)), this.x + 16, this.y + 24);
        graphics.text(font, "Level " + level + " / " + max, this.x + 42, this.y + 28, INK, false);
        SkillsScreen.drawSegmentedBar(graphics, this.x + 14, this.y + 50, 172, 5, max == 0 ? 0.0f : (float) level / max);
        graphics.text(font, "Progress", this.x + 14, this.y + 62, 0xFF5A5A5A, false);

        graphics.text(font, "Description", this.x + 14, this.y + 78, 0xFF5A5A5A, false);
        drawWrapped(graphics, font, SkillsScreen.skillTip(this.skillId) + ".", this.x + 14, this.y + 90, 172, 11, 2);

        graphics.text(font, "Bonuses", this.x + 14, this.y + 116, 0xFF5A5A5A, false);
        String[] bonuses = bonusLines();
        for (int i = 0; i < bonuses.length && i < 2; i++) {
            graphics.text(font, bonuses[i], this.x + 14, this.y + 128 + i * 11, INK, false);
        }

        graphics.text(font, "Unlocks", this.x + 14, this.y + 154, 0xFF5A5A5A, false);
        java.util.LinkedHashMap<Integer, java.util.List<net.minecraft.resources.Identifier>> byLevel =
                new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<net.minecraft.resources.Identifier, ClientSkillGates.Requirement> entry
                : ClientSkillGates.allForSkill(this.skillId)) {
            byLevel.computeIfAbsent(entry.getValue().level(), k -> new java.util.ArrayList<>()).add(entry.getKey());
        }
        String more = byLevel.size() > 2 ? "+" + (byLevel.size() - 2) + " more levels" : null;
        if (more != null) {
            graphics.text(font, more, this.x + 186 - font.width(more), this.y + 154, 0xFF5A5A5A, false);
        }
        drawUnlocks(graphics, font, mouseX, mouseY, byLevel);

        drawButton(graphics, font, "Back", this.x + 14, this.y + 194, mouseX, mouseY);
    }

    /** LevelZ-style per-level bonus text, read from the live skills config. */
    private String[] bonusLines() {
        var bonuses = dev.jmiahman.hearthwind.skills.SkillsConfig.get().bonuses;
        return switch (this.skillId) {
            case "health" -> new String[] { "+" + SkillsScreen.formatStat(bonuses.healthHpPerLevel) + " max HP per level" };
            case "strength" -> new String[] { "+" + SkillsScreen.formatStat(bonuses.strengthDamagePerLevel) + " attack damage per level" };
            case "agility" -> new String[] { "+" + SkillsScreen.formatStat(bonuses.agilitySpeedFractionPerLevel * 100.0) + "% movement speed per level" };
            case "defense" -> new String[] { "+" + SkillsScreen.formatStat(bonuses.defenseArmorPerLevel) + " armor per level" };
            case "mining" -> new String[] { "+" + SkillsScreen.formatStat(bonuses.miningSpeedFractionPerLevel * 100.0) + "% mining speed per level" };
            case "luck" -> new String[] { "+" + SkillsScreen.formatStat(bonuses.luckPerLevel) + " luck per level" };
            case "stamina" -> new String[] { "Spend stamina on digs and sprints", "before exhaustion slows you down." };
            default -> new String[] { "Levels unlock recipes, tools,", "stations and equipment." };
        };
    }

    /** One line per level (up to 8 icons), Aged's restriction list layout. */
    private void drawUnlocks(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
            java.util.LinkedHashMap<Integer, java.util.List<net.minecraft.resources.Identifier>> byLevel) {
        java.util.List<java.util.Map.Entry<net.minecraft.resources.Identifier, ClientSkillGates.Requirement>> all =
                ClientSkillGates.allForSkill(this.skillId);

        if (all.isEmpty()) {
            graphics.text(font, "Nothing is locked for this skill.", this.x + 14, this.y + 162, INK, false);
            return;
        }

        String hovered = null;
        int line = 0;
        for (java.util.Map.Entry<Integer, java.util.List<net.minecraft.resources.Identifier>> group : byLevel.entrySet()) {
            if (line >= 2) {
                break;
            }
            int rowY = this.y + 160 + line * 14;
            String label = "Lv " + group.getKey();
            graphics.text(font, label, this.x + 14, rowY + 4, 0xFF7A1F1F, false);
            int iconX = this.x + 40;
            for (int i = 0; i < group.getValue().size() && i < 8; i++) {
                graphics.item(SkillRestrictionScreen.icon(group.getValue().get(i)), iconX, rowY);
                if (isOver(40 + i * 14, 160 + line * 14, 14, 14, mouseX, mouseY)) {
                    hovered = SkillRestrictionScreen.name(group.getValue().get(i));
                }
                iconX += 14;
            }
            if (group.getValue().size() > 8) {
                graphics.text(font, "+" + (group.getValue().size() - 8), iconX + 2, rowY + 4, 0xFF5A5A5A, false);
            }
            line++;
        }

        if (hovered != null) {
            graphics.setTooltipForNextFrame(Component.literal(hovered), mouseX, mouseY);
        }
    }

    @Override
    protected boolean onContentClick(MouseButtonEvent event) {
        if (event.button() != 0) {
            return false;
        }
        int bx = this.x + 14;
        int by = this.y + 194;
        if (event.x() >= bx && event.x() < bx + 52 && event.y() >= by && event.y() < by + 16) {
            click();
            Minecraft.getInstance().setScreenAndShow(new SkillsScreen());
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(new SkillsScreen());
    }

    private void drawButton(GuiGraphicsExtractor graphics, Font font, String label, int bx, int by,
            int mouseX, int mouseY) {
        boolean hover = mouseX >= bx && mouseX < bx + 52 && mouseY >= by && mouseY < by + 16;
        graphics.fill(bx, by, bx + 52, by + 16, 0xFF373737);
        graphics.fill(bx + 1, by + 1, bx + 51, by + 15, hover ? 0xFFE0E0E0 : 0xFFC6C6C6);
        graphics.text(font, label, bx + 26 - font.width(label) / 2, by + 4, INK, false);
    }

    private void drawWrapped(GuiGraphicsExtractor graphics, Font font, String text, int x, int y,
            int width, int lineHeight, int maxLines) {
        StringBuilder line = new StringBuilder();
        int cursorY = y;
        int lines = 0;
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (font.width(candidate) > width && !line.isEmpty()) {
                graphics.text(font, line.toString(), x, cursorY, INK, false);
                cursorY += lineHeight;
                lines++;
                if (lines >= maxLines) {
                    return;
                }
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            graphics.text(font, line.toString(), x, cursorY, INK, false);
        }
    }

    private static int maxLevel() {
        try {
            return dev.jmiahman.hearthwind.skills.SkillXp.maxLevel();
        } catch (Throwable ignored) {
            return 30;
        }
    }
}
