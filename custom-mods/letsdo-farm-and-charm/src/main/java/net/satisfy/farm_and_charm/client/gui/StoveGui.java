package net.satisfy.farm_and_charm.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.satisfy.farm_and_charm.FarmAndCharm;
import net.satisfy.farm_and_charm.client.gui.handler.StoveGuiHandler;

@Environment(EnvType.CLIENT)
public class StoveGui extends AbstractContainerScreen<StoveGuiHandler> {
    public static final Identifier BACKGROUND  = FarmAndCharm.identifier("textures/gui/stove_gui.png");

    public static final int ARROW_X = 93;
    public static final int ARROW_Y = 32;

    public StoveGui(StoveGuiHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }


    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        int posX = this.leftPos;
        int posY = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, StoveGui.BACKGROUND, posX, posY, 0, 0, this.imageWidth - 1, this.imageHeight, 256, 256);
        this.renderProgressArrow(guiGraphics);
        this.renderBurnIcon(guiGraphics, posX, posY);
    }

    public void renderProgressArrow(GuiGraphicsExtractor guiGraphics) {
        int progress = this.menu.getScaledProgress(18);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos + 93, topPos + 32, 178, 20, progress, 25, 256, 256);
    }

    public void renderBurnIcon(GuiGraphicsExtractor guiGraphics, int posX, int posY) {
        if (this.menu.isBeingBurned()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, posX + 62, posY + 49, 176, 0, 17, 15, 256, 256);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        int iconX = this.leftPos + 62;
        int iconY = this.topPos + 49;
        int iconWidth = 17;
        int iconHeight = 15;
        if (mouseX >= iconX && mouseX <= iconX + iconWidth && mouseY >= iconY && mouseY <= iconY + iconHeight) {
            int burnTime = this.menu.propertyDelegate.get(0);
            int totalSeconds = burnTime / 20;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            String formattedTime = String.format("%02d:%02d", minutes, seconds);
            guiGraphics.setTooltipForNextFrame(Component.translatable("gui.farm_and_charm.stove_burn_time", formattedTime), mouseX, mouseY);
        }
    }
}