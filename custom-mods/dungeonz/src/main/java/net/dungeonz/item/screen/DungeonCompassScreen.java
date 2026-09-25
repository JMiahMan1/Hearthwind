package net.dungeonz.item.screen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.dungeonz.init.ItemInit;
import net.dungeonz.network.DungeonClientPacket;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.GameNarrator;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;

@Environment(EnvType.CLIENT)
public class DungeonCompassScreen extends Screen {

    private static final Identifier TEXTURE = Identifier.parse("dungeonz:textures/gui/dungeon_compass.png");

    private Button doneButton;
    private final WidgetButtonPage[] dungeons = new WidgetButtonPage[7];

    private List<String> dungeonIds = new ArrayList<String>();
    private String dungeonType;

    private int selectedIndex;
    private int indexStartOffset;
    private boolean scrolling;
    private int backgroundWidth = 105;
    private int backgroundHeight = 185;
    private int x;
    private int y;

    public DungeonCompassScreen(String dungeonType, List<String> dungeonIds) {
        super(Component.translatable("compass.compass_screen.title"));
        this.dungeonType = dungeonType;
        this.dungeonIds = dungeonIds;
    }

    @Override
    protected void init() {
        super.init();

        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        int k = y + 16 + 2;
        for (int l = 0; l < 7; ++l) {
            this.dungeons[l] = this.addRenderableWidget(new WidgetButtonPage(x + 5, k, l, button -> {
                this.selectedIndex = ((WidgetButtonPage) button).getIndex() + this.indexStartOffset;
                this.dungeonType = this.dungeonIds.get(this.selectedIndex);
                this.updateDoneButtonState();
            }));
            this.dungeons[l].visible = l < this.dungeonIds.size();
            if (!dungeonType.equals("") && this.dungeonIds.size() > l && this.dungeonIds.get(l).equals(dungeonType)) {
                this.dungeons[l].active = false;
            }
            k += 20;
        }

        this.doneButton = this.addRenderableWidget(Button.builder(Component.translatable("compass.compass_screen.calibrate"), button -> {
            this.onDone();
        }).bounds(this.x + this.backgroundWidth / 2 - 48, this.y + this.backgroundHeight - 25, 97, 20).build());
        this.doneButton.active = false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0f, 0.0f, this.backgroundWidth, this.backgroundHeight, 256, 256, 0xFFFFFFFF);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.text(this.font, this.title, this.x + this.backgroundWidth / 2 - this.font.width(this.title) / 2, this.y + 6, 0xFF404040, false);

        if (!this.dungeonIds.isEmpty()) {
            int i = (this.width - this.backgroundWidth) / 2;
            int j = (this.height - this.backgroundHeight) / 2;
            int k = this.y + 16 + 1;
            int l = this.x + 5 + 5;
            this.renderScrollbar(graphics, i, j, this.dungeonIds);
            int m = 0;
            for (String dungeon : this.dungeonIds) {
                if (this.canScroll(this.dungeonIds.size()) && (m < this.indexStartOffset || m >= 7 + this.indexStartOffset)) {
                    ++m;
                    continue;
                }

                int n = k + 7;
                graphics.text(this.font, getDungeonName(dungeon, 78, 9), l, n, 0xFFE5E5E5, false);
                k += 20;
                ++m;
            }

            for (WidgetButtonPage widgetButtonPage : this.dungeons) {
                if (widgetButtonPage.isHovered()) {
                    widgetButtonPage.renderTooltip(graphics, mouseX, mouseY);
                }
                widgetButtonPage.visible = widgetButtonPage.index < this.dungeonIds.size();
            }
        }
        if (this.doneButton.isHovered() && !this.doneButton.active && !StringUtils.isEmpty(this.dungeonType) && minecraft.player != null
                && !InventoryHelper.hasRequiredItemStacks(minecraft.player.getInventory(), ItemInit.getRequiredDungeonCompassCalibrationItems())) {
            graphics.setTooltipForNextFrame(Component.translatable("compass.compass_screen.required"), mouseX, mouseY);
        }
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics, int x, int y, List<String> dungeonIds) {
        int i = dungeonIds.size() + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int m = Math.min(113, this.indexStartOffset * k);
            if (this.indexStartOffset == i - 1) {
                m = 113;
            }
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 94, y + 18 + m, 105.0f, 0.0f, 6, 27, 256, 256, 0xFFFFFFFF);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 94, y + 18, 111.0f, 0.0f, 6, 27, 256, 256, 0xFFFFFFFF);
        }
    }

    private Component getDungeonName(String dungeonType, int length, int substringLength) {
        Component dungeonName = Component.translatable("dungeon." + dungeonType);

        if (this.minecraft.font.width(dungeonName) > length && substringLength != 0) {
            dungeonType = dungeonName.getString().substring(0, substringLength) + "..";
            return Component.nullToEmpty(dungeonType);
        } else {
            return dungeonName;
        }

    }

    private boolean canScroll(int listSize) {
        return listSize > 7;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int i = this.dungeonIds.size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.indexStartOffset = Mth.clamp((int) ((double) this.indexStartOffset - verticalAmount), 0, j);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        int i = this.dungeonIds.size();
        if (this.scrolling) {
            int j = this.y + 18;
            int k = j + 139;
            int l = i - 7;
            float f = ((float) event.y() - (float) j - 13.5f) / ((float) (k - j) - 27.0f);
            f = f * (float) l + 0.5f;
            this.indexStartOffset = Mth.clamp((int) f, 0, l);
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        this.scrolling = false;
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        if (this.canScroll(this.dungeonIds.size()) && event.x() > (double) (i + 94) && event.x() < (double) (i + 94 + 6) && event.y() > (double) (j + 18) && event.y() <= (double) (j + 18 + 139 + 1)) {
            this.scrolling = true;
        }
        return super.mouseClicked(event, doubled);
    }

    private void updateDoneButtonState() {
        this.doneButton.active = !StringUtils.isEmpty(this.dungeonType) && minecraft.player != null
                && InventoryHelper.hasRequiredItemStacks(minecraft.player.getInventory(), ItemInit.getRequiredDungeonCompassCalibrationItems());
    }

    private void onDone() {
        this.onClose();
        this.minecraft.player.playSound(SoundEvents.LODESTONE_COMPASS_LOCK, 1.0f, 1.0f);
        DungeonClientPacket.writeC2SSetDungeonCompassPacket(this.minecraft, this.dungeonType);
    }

    private class WidgetButtonPage extends Button {
        private static final WidgetSprites SPRITES = new WidgetSprites(Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"),
                Identifier.withDefaultNamespace("widget/button_highlighted"));
        final int index;

        public WidgetButtonPage(int x, int y, int index, Button.OnPress onPress) {
            super(x, y, 89, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
            int color = (this.active ? 0xFFFFFF : 0xA0A0A0) | Mth.ceil(this.alpha * 255.0f) << 24;
            graphics.centeredText(DungeonCompassScreen.this.font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, color);
        }

        public void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
            if (this.isHovered) {
                Component text = Component.translatable("dungeon." + DungeonCompassScreen.this.dungeonIds.get(this.index + DungeonCompassScreen.this.indexStartOffset));
                if (minecraft.font.width(text) > 78) {
                    graphics.setTooltipForNextFrame(text, mouseX, mouseY);
                }
            }
        }
    }

}
