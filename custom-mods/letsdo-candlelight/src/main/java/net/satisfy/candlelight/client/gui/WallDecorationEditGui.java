package net.satisfy.candlelight.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.core.block.entity.WallDecorationBlockEntity;
import net.satisfy.candlelight.core.networking.CandlelightMessages;
import net.satisfy.candlelight.core.networking.packet.SetWallDecorationTextPacket;

import java.util.List;

public class WallDecorationEditGui extends Screen {
    private final WallDecorationBlockEntity entity;
    private EditBox textField;

    public WallDecorationEditGui(WallDecorationBlockEntity entity) {
        super(Component.translatable("gui.candlelight.wall_decoration.edit"));
        this.entity = entity;
    }

    @Override
    protected void init() {
        int offsetX = 82;
        int offsetY = 7;

        this.textField = new EditBox(this.font, this.width / 2 - 100 + offsetX, this.height / 2 - 30 + offsetY, 200, 20, Component.literal(""));
        textField.setValue(entity.getText(0).getString());
        textField.setMaxLength(12);
        textField.setBordered(false);
        textField.setTextColor(0xFADFB0);
        this.addRenderableWidget(textField);

        this.setInitialFocus(textField);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.candlelight.wall_decoration.done"), button -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 40, 100, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        Identifier texture = Candlelight.identifier("textures/item/heart.png");
        int w = 16 * 8;
        int h = 16 * 8;
        int x = (int)(this.width / 2.0 - 65);
        int y = (int)(this.height / 2.0 - 90);

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, w, h, 0, 0, 16, 16, 16, 16);

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

    @Override
    public void onClose() {
        List<String> texts = List.of(textField.getValue());
        CandlelightMessages.sendSetSignTextToServer(new SetWallDecorationTextPacket(entity.getBlockPos(), texts));
        super.onClose();
    }
}