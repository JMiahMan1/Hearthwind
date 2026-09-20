package net.dungeonz.block.screen;

import org.apache.commons.lang3.StringUtils;

import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.network.DungeonClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.core.registries.BuiltInRegistries;

@Environment(EnvType.CLIENT)
public class DungeonGateOpScreen extends Screen {

    private static final Component GATE_BLOCK_ID_TEXT = Component.translatable("dungeon.op_screen.gate_block_id");
    private static final Component GATE_PARTICLE_ID_TEXT = Component.translatable("dungeon.op_screen.gate_particle_id");
    private static final Component GATE_UNLOCK_ITEM_ID_TEXT = Component.translatable("dungeon.op_screen.gate_unlock_item_id");
    private final BlockPos dungeonGatePos;

    private Button doneButton;
    private EditBox gateBlockIdTextFieldWidget;
    private EditBox gateParticleIdTextFieldWidget;
    private EditBox gateUnlockItemIdTextFieldWidget;

    private String defaultBlockId = "minecraft:chiseled_stone_bricks";
    private String defaultParticleId = "minecraft:scrape";
    private String defaultItemId = "";

    public DungeonGateOpScreen(BlockPos dungeonGatePos) {
        super(GameNarrator.NO_TITLE);
        this.dungeonGatePos = dungeonGatePos;
    }

    @Override
    protected void init() {
        if (minecraft.level != null && minecraft.level.getBlockEntity(this.dungeonGatePos) != null && minecraft.level.getBlockEntity(this.dungeonGatePos) instanceof DungeonGateEntity) {
            DungeonGateEntity dungeonGateEntity = (DungeonGateEntity) minecraft.level.getBlockEntity(this.dungeonGatePos);
            if (!dungeonGateEntity.getDisguiseBlockState().getBlock().equals(Blocks.AIR)) {
                defaultBlockId = BuiltInRegistries.BLOCK.getKey(dungeonGateEntity.getDisguiseBlockState().getBlock()).toString();
            }
            if (dungeonGateEntity.getParticleEffect() != null) {

                defaultParticleId = BuiltInRegistries.PARTICLE_TYPE.getKey(dungeonGateEntity.getParticleEffect().getType()).toString();
            }
            if (dungeonGateEntity.getUnlockItem() != null) {
                defaultItemId = BuiltInRegistries.ITEM.getKey(dungeonGateEntity.getUnlockItem()).toString();
            }
        }

        this.gateBlockIdTextFieldWidget = new EditBox(this.font, this.width / 2 - 152, 50, 300, 20, GATE_BLOCK_ID_TEXT);
        this.gateBlockIdTextFieldWidget.setMaxLength(128);
        this.gateBlockIdTextFieldWidget.setValue(defaultBlockId);
        this.gateBlockIdTextFieldWidget.setResponder(pool -> this.updateDoneButtonState());
        this.addRenderableWidget(this.gateBlockIdTextFieldWidget);

        this.gateParticleIdTextFieldWidget = new EditBox(this.font, this.width / 2 - 152, 85, 300, 20, GATE_PARTICLE_ID_TEXT);
        this.gateParticleIdTextFieldWidget.setMaxLength(128);
        this.gateParticleIdTextFieldWidget.setValue(defaultParticleId);
        this.gateParticleIdTextFieldWidget.setResponder(name -> this.updateDoneButtonState());
        this.addRenderableWidget(this.gateParticleIdTextFieldWidget);

        this.gateUnlockItemIdTextFieldWidget = new EditBox(this.font, this.width / 2 - 152, 120, 300, 20, GATE_UNLOCK_ITEM_ID_TEXT);
        this.gateUnlockItemIdTextFieldWidget.setMaxLength(128);
        this.gateUnlockItemIdTextFieldWidget.setValue(defaultItemId);
        this.gateUnlockItemIdTextFieldWidget.setResponder(name -> this.updateDoneButtonState());
        this.addRenderableWidget(this.gateUnlockItemIdTextFieldWidget);

        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.onDone();
        }).bounds(this.width / 2 - 75, 156, 150, 20).build());
        this.setInitialFocus(this.gateBlockIdTextFieldWidget);
        this.updateDoneButtonState();
    }

    @Override
    public void resize(int width, int height) {
        String string = this.gateBlockIdTextFieldWidget.getValue();
        String string2 = this.gateParticleIdTextFieldWidget.getValue();
        String string3 = this.gateUnlockItemIdTextFieldWidget.getValue();

        super.resize(width, height);
        this.gateBlockIdTextFieldWidget.setValue(string);
        this.gateParticleIdTextFieldWidget.setValue(string2);
        this.gateUnlockItemIdTextFieldWidget.setValue(string3);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.font, GATE_BLOCK_ID_TEXT, this.width / 2 - 153, 40, 0xFFA0A0A0, true);
        graphics.text(this.font, GATE_PARTICLE_ID_TEXT, this.width / 2 - 153, 75, 0xFFA0A0A0, true);
        graphics.text(this.font, GATE_UNLOCK_ITEM_ID_TEXT, this.width / 2 - 153, 110, 0xFFA0A0A0, true);
    }

    private void updateDoneButtonState() {
        Identifier blockId = Identifier.tryParse(this.gateBlockIdTextFieldWidget.getValue());
        this.doneButton.active = !StringUtils.isEmpty(this.gateBlockIdTextFieldWidget.getValue()) && blockId != null
                && !BuiltInRegistries.BLOCK.get(blockId).map(ref -> ref.value()).orElse(Blocks.AIR).equals(Blocks.AIR);
    }

    private void onDone() {
        this.onClose();
        DungeonClientPacket.writeC2SSetGateBlockPacket(minecraft, this.gateBlockIdTextFieldWidget.getValue(), this.gateParticleIdTextFieldWidget.getValue(), this.gateUnlockItemIdTextFieldWidget.getValue(),
                this.dungeonGatePos);
    }

}
