package net.dungeonz.block.screen;

import org.apache.commons.lang3.StringUtils;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.network.DungeonClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

@Environment(EnvType.CLIENT)
public class DungeonPortalOpScreen extends Screen {

    private static final Component DUNGEON_TYPE_TEXT = Component.translatable("dungeon.op_screen.dungeon_type");
    private static final Component DEFAULT_DIFFICULTY_TEXT = Component.translatable("dungeon.op_screen.default_difficulty");
    private final BlockPos dungeonPortalPos;

    private Button doneButton;
    private EditBox dungeonTypeTextFieldWidget;
    private EditBox dungeonDefaultDifficultyTextFieldWidget;

    private String defaultDungeonType = "dark_dungeon";
    private String defaultDungeonDifficulty = "normal";

    public DungeonPortalOpScreen(BlockPos dungeonPortalPos) {
        super(GameNarrator.NO_TITLE);
        this.dungeonPortalPos = dungeonPortalPos;
    }

    @Override
    protected void init() {
        if (minecraft.level != null && minecraft.level.getBlockEntity(this.dungeonPortalPos) != null && minecraft.level.getBlockEntity(this.dungeonPortalPos) instanceof DungeonPortalEntity) {
            DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) minecraft.level.getBlockEntity(this.dungeonPortalPos);
            if (!dungeonPortalEntity.getDungeonType().equals("")) {
                defaultDungeonType = dungeonPortalEntity.getDungeonType();
            }
            if (!dungeonPortalEntity.getDifficulty().equals("")) {
                defaultDungeonDifficulty = dungeonPortalEntity.getDifficulty();
            }
        }

        this.dungeonTypeTextFieldWidget = new EditBox(this.font, this.width / 2 - 152, 50, 300, 20, DUNGEON_TYPE_TEXT);
        this.dungeonTypeTextFieldWidget.setMaxLength(128);
        this.dungeonTypeTextFieldWidget.setValue(defaultDungeonType);
        this.dungeonTypeTextFieldWidget.setResponder(pool -> this.updateDoneButtonState());
        this.addRenderableWidget(this.dungeonTypeTextFieldWidget);
        this.dungeonDefaultDifficultyTextFieldWidget = new EditBox(this.font, this.width / 2 - 152, 85, 300, 20, DEFAULT_DIFFICULTY_TEXT);
        this.dungeonDefaultDifficultyTextFieldWidget.setMaxLength(128);
        this.dungeonDefaultDifficultyTextFieldWidget.setValue(defaultDungeonDifficulty);
        this.dungeonDefaultDifficultyTextFieldWidget.setResponder(name -> this.updateDoneButtonState());
        this.addRenderableWidget(this.dungeonDefaultDifficultyTextFieldWidget);

        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.onDone();
        }).bounds(this.width / 2 - 75, 126, 150, 20).build());
        this.setInitialFocus(this.dungeonTypeTextFieldWidget);
        this.updateDoneButtonState();
    }

    @Override
    public void resize(int width, int height) {
        String string = this.dungeonTypeTextFieldWidget.getValue();
        String string2 = this.dungeonDefaultDifficultyTextFieldWidget.getValue();

        super.resize(width, height);
        this.dungeonTypeTextFieldWidget.setValue(string);
        this.dungeonDefaultDifficultyTextFieldWidget.setValue(string2);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.font, DUNGEON_TYPE_TEXT, this.width / 2 - 153, 40, 0xFFA0A0A0, true);
        graphics.text(this.font, DEFAULT_DIFFICULTY_TEXT, this.width / 2 - 153, 75, 0xFFA0A0A0, true);
    }

    private void updateDoneButtonState() {
        this.doneButton.active = !StringUtils.isEmpty(this.dungeonTypeTextFieldWidget.getValue()) && !StringUtils.isEmpty(this.dungeonDefaultDifficultyTextFieldWidget.getValue());
    }

    private void onDone() {
        this.onClose();
        DungeonClientPacket.writeC2SSetDungeonTypePacket(minecraft, this.dungeonTypeTextFieldWidget.getValue(), this.dungeonDefaultDifficultyTextFieldWidget.getValue(), dungeonPortalPos);
    }

}
