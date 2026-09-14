package net.dungeonz.block.screen;

import org.apache.commons.lang3.StringUtils;

import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.network.DungeonClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_333;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5244;
import net.minecraft.class_7923;

@Environment(EnvType.CLIENT)
public class DungeonGateOpScreen extends class_437 {

    private static final class_2561 GATE_BLOCK_ID_TEXT = class_2561.method_43471("dungeon.op_screen.gate_block_id");
    private static final class_2561 GATE_PARTICLE_ID_TEXT = class_2561.method_43471("dungeon.op_screen.gate_particle_id");
    private static final class_2561 GATE_UNLOCK_ITEM_ID_TEXT = class_2561.method_43471("dungeon.op_screen.gate_unlock_item_id");
    private final class_2338 dungeonGatePos;

    private class_4185 doneButton;
    private class_342 gateBlockIdTextFieldWidget;
    private class_342 gateParticleIdTextFieldWidget;
    private class_342 gateUnlockItemIdTextFieldWidget;

    private String defaultBlockId = "minecraft:chiseled_stone_bricks";
    private String defaultParticleId = "minecraft:scrape";
    private String defaultItemId = "";

    public DungeonGateOpScreen(class_2338 dungeonGatePos) {
        super(class_333.field_18967);
        this.dungeonGatePos = dungeonGatePos;
    }

    @Override
    protected void method_25426() {
        if (field_22787.field_1687 != null && field_22787.field_1687.method_8321(this.dungeonGatePos) != null && field_22787.field_1687.method_8321(this.dungeonGatePos) instanceof DungeonGateEntity) {
            DungeonGateEntity dungeonGateEntity = (DungeonGateEntity) field_22787.field_1687.method_8321(this.dungeonGatePos);
            if (!dungeonGateEntity.getBlockState().method_26204().equals(class_2246.field_10124)) {
                defaultBlockId = class_7923.field_41175.method_10221(dungeonGateEntity.getBlockState().method_26204()).toString();
            }
            if (dungeonGateEntity.getParticleEffect() != null) {

                defaultParticleId = class_7923.field_41180.method_10221(dungeonGateEntity.getParticleEffect().method_10295()).toString();
            }
            if (dungeonGateEntity.getUnlockItem() != null) {
                defaultItemId = class_7923.field_41178.method_10221(dungeonGateEntity.getUnlockItem()).toString();
            }
        }

        this.gateBlockIdTextFieldWidget = new class_342(this.field_22793, this.field_22789 / 2 - 152, 50, 300, 20, GATE_BLOCK_ID_TEXT);
        this.gateBlockIdTextFieldWidget.method_1880(128);
        this.gateBlockIdTextFieldWidget.method_1852(defaultBlockId);
        this.gateBlockIdTextFieldWidget.method_1863(pool -> this.updateDoneButtonState());
        this.method_25429(this.gateBlockIdTextFieldWidget);

        this.gateParticleIdTextFieldWidget = new class_342(this.field_22793, this.field_22789 / 2 - 152, 85, 300, 20, GATE_PARTICLE_ID_TEXT);
        this.gateParticleIdTextFieldWidget.method_1880(128);
        this.gateParticleIdTextFieldWidget.method_1852(defaultParticleId);
        this.gateParticleIdTextFieldWidget.method_1863(name -> this.updateDoneButtonState());
        this.method_25429(this.gateParticleIdTextFieldWidget);

        this.gateUnlockItemIdTextFieldWidget = new class_342(this.field_22793, this.field_22789 / 2 - 152, 120, 300, 20, GATE_UNLOCK_ITEM_ID_TEXT);
        this.gateUnlockItemIdTextFieldWidget.method_1880(128);
        this.gateUnlockItemIdTextFieldWidget.method_1852(defaultItemId);
        this.gateUnlockItemIdTextFieldWidget.method_1863(name -> this.updateDoneButtonState());
        this.method_25429(this.gateUnlockItemIdTextFieldWidget);

        this.doneButton = this.method_37063(class_4185.method_46430(class_5244.field_24334, button -> {
            this.onDone();
        }).method_46434(this.field_22789 / 2 - 75, 156, 150, 20).method_46431());
        this.method_48265(this.gateBlockIdTextFieldWidget);
        this.updateDoneButtonState();
    }

    @Override
    public void method_25410(class_310 client, int width, int height) {
        String string = this.gateBlockIdTextFieldWidget.method_1882();
        String string2 = this.gateParticleIdTextFieldWidget.method_1882();
        String string3 = this.gateUnlockItemIdTextFieldWidget.method_1882();

        this.method_25423(client, width, height);
        this.gateBlockIdTextFieldWidget.method_1852(string);
        this.gateParticleIdTextFieldWidget.method_1852(string2);
        this.gateUnlockItemIdTextFieldWidget.method_1852(string3);
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        super.method_25394(context, mouseX, mouseY, delta);
        context.method_27535(this.field_22793, GATE_BLOCK_ID_TEXT, this.field_22789 / 2 - 153, 40, 0xA0A0A0);
        this.gateBlockIdTextFieldWidget.method_25394(context, mouseX, mouseY, delta);
        context.method_27535(this.field_22793, GATE_PARTICLE_ID_TEXT, this.field_22789 / 2 - 153, 75, 0xA0A0A0);
        this.gateParticleIdTextFieldWidget.method_25394(context, mouseX, mouseY, delta);
        context.method_27535(this.field_22793, GATE_UNLOCK_ITEM_ID_TEXT, this.field_22789 / 2 - 153, 110, 0xA0A0A0);
        this.gateUnlockItemIdTextFieldWidget.method_25394(context, mouseX, mouseY, delta);
    }

    private void updateDoneButtonState() {
        this.doneButton.field_22763 = !StringUtils.isEmpty(this.gateBlockIdTextFieldWidget.method_1882()) && !class_7923.field_41175.method_10223(class_2960.method_60654(this.gateBlockIdTextFieldWidget.method_1882())).equals(class_2246.field_10124);
    }

    private void onDone() {
        this.field_22787.method_1507(null);
        DungeonClientPacket.writeC2SSetGateBlockPacket(field_22787, this.gateBlockIdTextFieldWidget.method_1882(), this.gateParticleIdTextFieldWidget.method_1882(), this.gateUnlockItemIdTextFieldWidget.method_1882(),
                this.dungeonGatePos);
    }

}
