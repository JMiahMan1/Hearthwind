package net.satisfy.candlelight.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.client.gui.handler.LetterGuiHandler;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class LetterGui extends AbstractContainerScreen<LetterGuiHandler> {
    private static final Identifier TEXTURE = Candlelight.identifier("textures/gui/letter_gui.png");
    private EditBox nameField;

    public LetterGui(LetterGuiHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.nameField = new EditBox(this.font, i + 92, j + 24, 50, 12, Component.empty());
        this.nameField.setCanLoseFocus(false);
        this.nameField.setTextColor(-1);
        this.nameField.setTextColorUneditable(-1);
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(50);
        this.nameField.setResponder(this::onRenamed);
        this.nameField.setValue("");
        this.addWidget(this.nameField);
        this.setInitialFocus(this.nameField);
        this.nameField.setEditable(true);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.nameField.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, Component.translatable("block.candlelight.letter.translatable.text"), (int) (this.width / 1.95F - 1), (int) (this.height / 4.65F), 0xFF5A5A5A);
    }

    private void onRenamed(String name) {
        LetterGuiHandler.name = name;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == 256) {
            assert Objects.requireNonNull(this.minecraft).player != null;
            assert this.minecraft.player != null;
            this.minecraft.player.closeContainer();
        }
        return this.nameField.keyPressed(event) || this.nameField.canConsumeInput() || super.keyPressed(event);
    }
}