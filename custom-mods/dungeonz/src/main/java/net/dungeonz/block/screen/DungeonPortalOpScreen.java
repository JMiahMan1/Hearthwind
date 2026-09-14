package net.dungeonz.block.screen;

import org.apache.commons.lang3.StringUtils;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.network.DungeonClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_333;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5244;

@Environment(EnvType.CLIENT)
public class DungeonPortalOpScreen extends class_437 {

    private static final class_2561 DUNGEON_TYPE_TEXT = class_2561.method_43471("dungeon.op_screen.dungeon_type");
    private static final class_2561 DEFAULT_DIFFICULTY_TEXT = class_2561.method_43471("dungeon.op_screen.default_difficulty");
    private final class_2338 dungeonPortalPos;

    private class_4185 doneButton;
    private class_342 dungeonTypeTextFieldWidget;
    private class_342 dungeonDefaultDifficultyTextFieldWidget;

    private String defaultDungeonType = "dark_dungeon";
    private String defaultDungeonDifficulty = "normal";

    public DungeonPortalOpScreen(class_2338 dungeonPortalPos) {
        super(class_333.field_18967);
        this.dungeonPortalPos = dungeonPortalPos;
    }

    @Override
    protected void method_25426() {
        if (field_22787.field_1687 != null && field_22787.field_1687.method_8321(this.dungeonPortalPos) != null && field_22787.field_1687.method_8321(this.dungeonPortalPos) instanceof DungeonPortalEntity) {
            DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) field_22787.field_1687.method_8321(this.dungeonPortalPos);
            if (!dungeonPortalEntity.getDungeonType().equals("")) {
                defaultDungeonType = dungeonPortalEntity.getDungeonType();
            }
            if (!dungeonPortalEntity.getDifficulty().equals("")) {
                defaultDungeonDifficulty = dungeonPortalEntity.getDifficulty();
            }
        }

        this.dungeonTypeTextFieldWidget = new class_342(this.field_22793, this.field_22789 / 2 - 152, 50, 300, 20, DUNGEON_TYPE_TEXT);
        this.dungeonTypeTextFieldWidget.method_1880(128);
        this.dungeonTypeTextFieldWidget.method_1852(defaultDungeonType);
        this.dungeonTypeTextFieldWidget.method_1863(pool -> this.updateDoneButtonState());
        this.method_25429(this.dungeonTypeTextFieldWidget);
        this.dungeonDefaultDifficultyTextFieldWidget = new class_342(this.field_22793, this.field_22789 / 2 - 152, 85, 300, 20, DEFAULT_DIFFICULTY_TEXT);
        this.dungeonDefaultDifficultyTextFieldWidget.method_1880(128);
        this.dungeonDefaultDifficultyTextFieldWidget.method_1852(defaultDungeonDifficulty);
        this.dungeonDefaultDifficultyTextFieldWidget.method_1863(name -> this.updateDoneButtonState());
        this.method_25429(this.dungeonDefaultDifficultyTextFieldWidget);

        this.doneButton = this.method_37063(class_4185.method_46430(class_5244.field_24334, button -> {
            this.onDone();
        }).method_46434(this.field_22789 / 2 - 75, 126, 150, 20).method_46431());
        this.method_48265(this.dungeonTypeTextFieldWidget);
        this.updateDoneButtonState();
    }

    @Override
    public void method_25410(class_310 client, int width, int height) {
        String string = this.dungeonTypeTextFieldWidget.method_1882();
        String string2 = this.dungeonDefaultDifficultyTextFieldWidget.method_1882();

        this.method_25423(client, width, height);
        this.dungeonTypeTextFieldWidget.method_1852(string);
        this.dungeonDefaultDifficultyTextFieldWidget.method_1852(string2);
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        super.method_25394(context, mouseX, mouseY, delta);
        context.method_27535(this.field_22793, DUNGEON_TYPE_TEXT, this.field_22789 / 2 - 153, 40, 0xA0A0A0);
        this.dungeonTypeTextFieldWidget.method_25394(context, mouseX, mouseY, delta);
        context.method_27535(this.field_22793, DEFAULT_DIFFICULTY_TEXT, this.field_22789 / 2 - 153, 75, 0xA0A0A0);
        this.dungeonDefaultDifficultyTextFieldWidget.method_25394(context, mouseX, mouseY, delta);
    }

    private void updateDoneButtonState() {
        this.doneButton.field_22763 = !StringUtils.isEmpty(this.dungeonTypeTextFieldWidget.method_1882()) && !StringUtils.isEmpty(this.dungeonDefaultDifficultyTextFieldWidget.method_1882());
    }

    private void onDone() {
        this.field_22787.method_1507(null);
        DungeonClientPacket.writeC2SSetDungeonTypePacket(field_22787, this.dungeonTypeTextFieldWidget.method_1882(), this.dungeonDefaultDifficultyTextFieldWidget.method_1882(), dungeonPortalPos);
    }

}
