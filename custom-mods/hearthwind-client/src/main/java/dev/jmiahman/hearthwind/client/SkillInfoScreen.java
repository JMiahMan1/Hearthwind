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
public class SkillInfoScreen extends AgedPanelScreen {
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
        graphics.text(font, "Description", this.x + 14, this.y + 82, 0xFF5A5A5A, false);
        drawWrapped(graphics, font, SkillsScreen.skillTip(this.skillId) + ".", this.x + 14, this.y + 96, 172, 12);
        graphics.text(font, "Higher levels unlock better tools,", this.x + 14, this.y + 126, 0xFF5A5A5A, false);
        graphics.text(font, "equipment, stations and recipes.", this.x + 14, this.y + 138, 0xFF5A5A5A, false);
        drawButton(graphics, font, "Back", this.x + 14, this.y + 188, mouseX, mouseY);
    }

    @Override
    protected boolean onContentClick(MouseButtonEvent event) {
        if (event.button() != 0) {
            return false;
        }
        int bx = this.x + 14;
        int by = this.y + 188;
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
            int width, int lineHeight) {
        StringBuilder line = new StringBuilder();
        int cursorY = y;
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (font.width(candidate) > width && !line.isEmpty()) {
                graphics.text(font, line.toString(), x, cursorY, INK, false);
                cursorY += lineHeight;
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
