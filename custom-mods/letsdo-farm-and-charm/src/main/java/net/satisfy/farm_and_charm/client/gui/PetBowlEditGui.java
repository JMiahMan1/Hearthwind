package net.satisfy.farm_and_charm.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.satisfy.farm_and_charm.core.block.entity.PetBowlBlockEntity;
import net.satisfy.farm_and_charm.core.network.PacketHandler;
import net.satisfy.farm_and_charm.core.network.packet.SetTextPacket;

import java.util.List;

public class PetBowlEditGui extends Screen {
    private final PetBowlBlockEntity entity;
    private EditBox textField;

    public PetBowlEditGui(PetBowlBlockEntity entity) {
        super(Component.translatable("gui.farm_and_charm.pet_bowl.edit"));
        this.entity = entity;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        textField = new EditBox(this.font, centerX - 100, centerY - 10, 200, 20, Component.literal(""));
        textField.setMaxLength(6);
        textField.setValue(entity.getText().getString());
        this.addRenderableWidget(textField);
        this.setInitialFocus(textField);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.farm_and_charm.text_edit.done"), button -> this.onClose())
                .bounds(centerX - 50, centerY + 20, 100, 20)
                .build());
    }

    @Override
    public void onClose() {
        PacketHandler.sendToServer(new SetTextPacket(entity.getBlockPos(), List.of(textField.getValue())));
        super.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 20, 16777215);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == 256 || event.key() == 257 || event.key() == 335) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}