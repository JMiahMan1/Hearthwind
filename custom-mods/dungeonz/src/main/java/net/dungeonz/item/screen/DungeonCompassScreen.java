package net.dungeonz.item.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.lang3.StringUtils;

import net.dungeonz.init.ItemInit;
import net.dungeonz.network.DungeonClientPacket;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_333;
import net.minecraft.class_3417;
import net.minecraft.class_3532;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5244;

@Environment(EnvType.CLIENT)
public class DungeonCompassScreen extends class_437 {

    private static final class_2960 TEXTURE = class_2960.method_60654("dungeonz:textures/gui/dungeon_compass.png");
    private final class_2561 title = class_2561.method_43471("compass.compass_screen.title");

    private class_4185 doneButton;
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
        super(class_333.field_18967);
        this.dungeonType = dungeonType;
        this.dungeonIds = dungeonIds;
    }

    @Override
    protected void method_25426() {
        super.method_25426();

        this.x = (this.field_22789 - this.backgroundWidth) / 2;
        this.y = (this.field_22790 - this.backgroundHeight) / 2;

        int k = y + 16 + 2;
        for (int l = 0; l < 7; ++l) {
            this.dungeons[l] = this.method_37063(new WidgetButtonPage(x + 5, k, l, button -> {
                this.selectedIndex = ((WidgetButtonPage) button).getIndex() + this.indexStartOffset;
                this.dungeonType = this.dungeonIds.get(this.selectedIndex);
                this.updateDoneButtonState();
            }));
            if (!dungeonType.equals("") && this.dungeonIds.size() > l && this.dungeonIds.get(l).equals(dungeonType)) {
                this.dungeons[l].field_22763 = false;
            }
            k += 20;
        }

        this.doneButton = this.method_37063(class_4185.method_46430(class_2561.method_43471("compass.compass_screen.calibrate"), button -> {
            this.onDone();
        }).method_46434(this.x + this.backgroundWidth / 2 - 48, this.y + this.backgroundHeight - 25, 97, 20).method_46431());
        this.doneButton.field_22763 = false;
    }

    @Override
    public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
        super.method_25420(context, mouseX, mouseY, delta);

        int i = (this.field_22789 - this.backgroundWidth) / 2;
        int j = (this.field_22790 - this.backgroundHeight) / 2;
        context.method_25290(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        super.method_25394(context, mouseX, mouseY, delta);

        context.method_51439(this.field_22793, this.field_22785, this.x + this.backgroundWidth / 2 - this.field_22793.method_27525(this.field_22785) / 2, this.y + 6, 0x404040, false);

        if (!this.dungeonIds.isEmpty()) {
            int i = (this.field_22789 - this.backgroundWidth) / 2;
            int j = (this.field_22790 - this.backgroundHeight) / 2;
            int k = this.y + 16 + 1;
            int l = this.x + 5 + 5;
            this.renderScrollbar(context, i, j, this.dungeonIds);
            int m = 0;
            for (String dungeon : this.dungeonIds) {
                if (this.canScroll(this.dungeonIds.size()) && (m < this.indexStartOffset || m >= 7 + this.indexStartOffset)) {
                    ++m;
                    continue;
                }

                int n = k + 7;
                context.method_51439(this.field_22793, getDungeonName(dungeon, 78, 9), l, n, 0xE5E5E5, false);
                k += 20;
                ++m;
            }

            for (WidgetButtonPage widgetButtonPage : this.dungeons) {
                if (widgetButtonPage.method_49606()) {
                    widgetButtonPage.renderTooltip(context, mouseX, mouseY);
                }
                widgetButtonPage.field_22764 = widgetButtonPage.index < this.dungeonIds.size();
            }
            RenderSystem.enableDepthTest();
        }
        if (this.doneButton.method_49606() && !this.doneButton.field_22763 && !StringUtils.isEmpty(this.dungeonType) && field_22787.field_1724 != null
                && !InventoryHelper.hasRequiredItemStacks(field_22787.field_1724.method_31548(), ItemInit.REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS)) {
            context.method_51438(this.field_22793, class_2561.method_43471("compass.compass_screen.required"), mouseX, mouseY);
        }
    }

    private void renderScrollbar(class_332 context, int x, int y, List<String> dungeonIds) {
        int i = dungeonIds.size() + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int m = Math.min(113, this.indexStartOffset * k);
            if (this.indexStartOffset == i - 1) {
                m = 113;
            }
            context.method_25290(TEXTURE, x + 94, y + 18 + m, 105.0f, 0.0f, 6, 27, 256, 256);
        } else {
            context.method_25290(TEXTURE, x + 94, y + 18, 111.0f, 0.0f, 6, 27, 256, 256);
        }
    }

    private class_2561 getDungeonName(String dungeonType, int length, int substringLength) {
        class_2561 dungeonName = class_2561.method_43471("dungeon." + dungeonType);

        if (this.field_22787.field_1772.method_27525(dungeonName) > length && substringLength != 0) {
            dungeonType = dungeonName.getString().substring(0, substringLength) + "..";
            return class_2561.method_30163(dungeonType);
        } else {
            return dungeonName;
        }

    }

    private boolean canScroll(int listSize) {
        return listSize > 7;
    }

    @Override
    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int i = this.dungeonIds.size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.indexStartOffset = class_3532.method_15340((int) ((double) this.indexStartOffset - verticalAmount), 0, j);
        }
        return true;
    }

    @Override
    public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int i = this.dungeonIds.size();
        if (this.scrolling) {
            int j = this.y + 18;
            int k = j + 139;
            int l = i - 7;
            float f = ((float) mouseY - (float) j - 13.5f) / ((float) (k - j) - 27.0f);
            f = f * (float) l + 0.5f;
            this.indexStartOffset = class_3532.method_15340((int) f, 0, l);
            return true;
        }
        return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean method_25402(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        int i = (this.field_22789 - this.backgroundWidth) / 2;
        int j = (this.field_22790 - this.backgroundHeight) / 2;
        if (this.canScroll(this.dungeonIds.size()) && mouseX > (double) (i + 94) && mouseX < (double) (i + 94 + 6) && mouseY > (double) (j + 18) && mouseY <= (double) (j + 18 + 139 + 1)) {
            this.scrolling = true;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    private void updateDoneButtonState() {
        this.doneButton.field_22763 = !StringUtils.isEmpty(this.dungeonType) && field_22787.field_1724 != null
                && InventoryHelper.hasRequiredItemStacks(field_22787.field_1724.method_31548(), ItemInit.REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS);
    }

    private void onDone() {
        this.field_22787.method_1507(null);
        this.field_22787.field_1724.method_5783(class_3417.field_23199, 1.0f, 1.0f);
        DungeonClientPacket.writeC2SSetDungeonCompassPacket(this.field_22787, this.dungeonType);
    }

    private class WidgetButtonPage extends class_4185 {
        final int index;

        public WidgetButtonPage(int x, int y, int index, class_4185.class_4241 onPress) {
            super(x, y, 89, 20, class_5244.field_39003, onPress, field_40754);
            this.index = index;
            this.field_22764 = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderTooltip(class_332 context, int mouseX, int mouseY) {
            if (this.field_22762) {
                class_2561 text = class_2561.method_43471("dungeon." + DungeonCompassScreen.this.dungeonIds.get(this.index + DungeonCompassScreen.this.indexStartOffset));
                if (field_22787.field_1772.method_27525(text) > 78) {
                    context.method_51438(field_22793, text, mouseX, mouseY);
                }
            }
        }
    }

}
