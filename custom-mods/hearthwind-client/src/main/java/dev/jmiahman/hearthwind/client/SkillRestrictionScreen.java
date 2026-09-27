package dev.jmiahman.hearthwind.client;

import java.util.List;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Aged 3.1.2 / LevelZ {@code SkillRestrictionScreen} rebuild: the list screen
 * opened from the Level screen's right rail, one page per gate kind (mining,
 * crafting, item use, creatures). Rows show the locked target, its name and
 * the skill level that unlocks it, scrollable with a slider on the right.
 */
@Environment(EnvType.CLIENT)
public class SkillRestrictionScreen extends HearthwindPanelScreen {

    public enum ListKind {
        MINING("Mining restrictions", "Blocks you cannot break or use yet"),
        CRAFTING("Crafting restrictions", "Recipes and stations still locked"),
        ITEMS("Item restrictions", "Items you cannot use yet"),
        ENTITIES("Creature restrictions", "Creatures you cannot interact with yet");

        private final String title;
        private final String subtitle;

        ListKind(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }

        public String title() {
            return this.title;
        }

        public String subtitle() {
            return this.subtitle;
        }
    }

    private static final int LIST_Y = 34;
    private static final int ROW_H = 18;
    private static final int VISIBLE_ROWS = 8;

    private final ListKind kind;
    private final List<Map.Entry<Identifier, ClientSkillGates.Requirement>> rows;
    private int scroll;

    public SkillRestrictionScreen(ListKind kind) {
        super(Component.literal(kind.title()));
        this.kind = kind;
        this.rows = switch (kind) {
            case MINING -> ClientSkillGates.sortedEntries(ClientSkillGates.miningGates());
            case CRAFTING -> ClientSkillGates.sortedEntries(ClientSkillGates.craftingGates());
            case ITEMS -> ClientSkillGates.sortedEntries(ClientSkillGates.itemGates());
            case ENTITIES -> ClientSkillGates.sortedEntries(ClientSkillGates.entityGates());
        };
    }

    public ListKind kind() {
        return this.kind;
    }

    @Override
    protected TabStrip.Tab activeTab() {
        return TabStrip.Tab.SKILLS;
    }

    @Override
    protected void drawContent(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        Component title = Component.literal(this.kind.title());
        graphics.text(font, title, this.x + 100 - font.width(title) / 2, this.y + 7, INK, false);

        String count = this.rows.size() + (this.rows.size() == 1 ? " entry" : " entries");
        int subtitleWidth = Math.max(40, 130 - font.width(count));
        graphics.text(font, font.plainSubstrByWidth(this.kind.subtitle(), subtitleWidth), this.x + 8, this.y + 22,
                0xFF5A5A5A, false);
        graphics.text(font, count, this.x + 186 - font.width(count), this.y + 22, 0xFF5A5A5A, false);

        if (this.rows.isEmpty()) {
            graphics.text(font, "Nothing is restricted here yet.", this.x + 8, this.y + LIST_Y + 4, INK, false);
        }

        String hovered = null;
        int maxScroll = Math.max(0, this.rows.size() - VISIBLE_ROWS);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int index = this.scroll + i;
            if (index >= this.rows.size()) {
                break;
            }
            Map.Entry<Identifier, ClientSkillGates.Requirement> row = this.rows.get(index);
            int rowY = this.y + LIST_Y + i * ROW_H;
            if ((index & 1) == 0) {
                graphics.fill(this.x + 6, rowY - 1, this.x + 190, rowY + 15, 0x14000000);
            }
            graphics.item(icon(row.getKey()), this.x + 8, rowY);
            String name = name(row.getKey());
            if (font.width(name) > 96) {
                name = font.plainSubstrByWidth(name, 88) + "...";
            }
            graphics.text(font, name, this.x + 28, rowY + 4, INK, false);
            ClientSkillGates.Requirement req = row.getValue();
            String need = SkillsScreen.skillName(req.skill()) + " " + req.level();
            graphics.text(font, need, this.x + 186 - font.width(need), rowY + 4, 0xFF7A1F1F, false);
            if (isOver(6, LIST_Y + i * ROW_H, 184, 16, mouseX, mouseY)) {
                hovered = name + " - requires " + SkillsScreen.skillName(req.skill()) + " level " + req.level();
            }
        }

        drawSlider(graphics, maxScroll);
        drawBackButton(graphics, font, mouseX, mouseY);

        if (hovered != null) {
            graphics.setTooltipForNextFrame(Component.literal(hovered), mouseX, mouseY);
        }
    }

    private void drawSlider(GuiGraphicsExtractor graphics, int maxScroll) {
        int trackTop = this.y + LIST_Y - 2;
        int trackHeight = VISIBLE_ROWS * ROW_H;
        graphics.fill(this.x + 192, trackTop, this.x + 195, trackTop + trackHeight, 0xFF373737);
        if (maxScroll > 0) {
            int thumbHeight = Math.max(12, trackHeight * VISIBLE_ROWS / this.rows.size());
            int thumbY = trackTop + (trackHeight - thumbHeight) * this.scroll / maxScroll;
            graphics.fill(this.x + 192, thumbY, this.x + 195, thumbY + thumbHeight, 0xFFC6C6C6);
        }
    }

    private void drawBackButton(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        int bx = this.x + 8;
        int by = this.y + 190;
        boolean hover = mouseX >= bx && mouseX < bx + 52 && mouseY >= by && mouseY < by + 16;
        graphics.fill(bx, by, bx + 52, by + 16, 0xFF373737);
        graphics.fill(bx + 1, by + 1, bx + 51, by + 15, hover ? 0xFFE0E0E0 : 0xFFC6C6C6);
        graphics.text(font, "Back", bx + 26 - font.width("Back") / 2, by + 4, INK, false);
    }

    @Override
    protected boolean onContentClick(MouseButtonEvent event) {
        if (event.button() != 0) {
            return false;
        }
        int bx = this.x + 8;
        int by = this.y + 190;
        if (event.x() >= bx && event.x() < bx + 52 && event.y() >= by && event.y() < by + 16) {
            click();
            Minecraft.getInstance().setScreenAndShow(new SkillsScreen());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, this.rows.size() - VISIBLE_ROWS);
        if (maxScroll > 0 && scrollY != 0 && isOver(6, LIST_Y, 184, VISIBLE_ROWS * ROW_H, mouseX, mouseY)) {
            this.scroll = Math.max(0, Math.min(maxScroll, this.scroll + (scrollY > 0 ? -1 : 1)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(new SkillsScreen());
    }

    /** Item/block icon for a gate target; barrier when the id is an entity. */
    static ItemStack icon(Identifier id) {
        Item item = BuiltInRegistries.ITEM.get(id).map(net.minecraft.core.Holder::value).orElse(null);
        if (item == null || item == Items.AIR) {
            Block block = BuiltInRegistries.BLOCK.get(id).map(net.minecraft.core.Holder::value).orElse(null);
            item = block == null ? null : block.asItem();
        }
        return new ItemStack(item == null || item == Items.AIR ? Items.BARRIER : item);
    }

    /** Display name for a gate target: item, block or entity registry key. */
    static String name(Identifier id) {
        Item item = BuiltInRegistries.ITEM.get(id).map(net.minecraft.core.Holder::value).orElse(null);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item).getHoverName().getString();
        }
        Block block = BuiltInRegistries.BLOCK.get(id).map(net.minecraft.core.Holder::value).orElse(null);
        if (block != null) {
            return block.getName().getString();
        }
        var entity = BuiltInRegistries.ENTITY_TYPE.get(id).map(net.minecraft.core.Holder::value).orElse(null);
        if (entity != null) {
            return entity.getDescription().getString();
        }
        return id.toString();
    }
}
