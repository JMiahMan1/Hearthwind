package net.satisfy.vinery.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.satisfy.vinery.client.gui.handler.ApplePressGuiHandler;
import net.satisfy.vinery.core.Vinery;

@Environment(EnvType.CLIENT)
public class ApplePressGui extends AbstractContainerScreen<ApplePressGuiHandler> {
    public static final Identifier TEXTURE = Vinery.identifier("textures/gui/apple_press_gui.png");

    public static final int MASHING_BAR_X = 40;
    public static final int MASHING_BAR_Y = 17;
    public static final int MASHING_BAR_WIDTH = 24;
    public static final int MASHING_BAR_HEIGHT = 38;
    public static final int MASHING_BAR_U = 176;
    public static final int MASHING_BAR_V = 0;

    public static final int FERMENTING_BAR_X = 101;
    public static final int FERMENTING_BAR_Y = 18;
    public static final int FERMENTING_BAR_WIDTH = 10;
    public static final int FERMENTING_BAR_HEIGHT = 28;
    public static final int FERMENTING_BAR_U = 176;
    public static final int FERMENTING_BAR_V = 47;

    public ApplePressGui(ApplePressGuiHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        renderProgressArrows(guiGraphics, x, y);
    }

    private void renderProgressArrows(GuiGraphicsExtractor guiGraphics, int x, int y) {
        if (menu.isCrafting(0)) {
            int height = menu.getScaledProgress(0);
            int xPosition = x + MASHING_BAR_X;
            int yPosition = y + MASHING_BAR_Y + height;
            int textureV = MASHING_BAR_V + height;
            int renderHeight = MASHING_BAR_HEIGHT - height;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xPosition, yPosition, MASHING_BAR_U, textureV, MASHING_BAR_WIDTH, renderHeight, 256, 256);
        }
        if (menu.isCrafting(1)) {
            int height = menu.getScaledProgress(1);
            int xPosition = x + FERMENTING_BAR_X;
            int yPosition = y + FERMENTING_BAR_Y + FERMENTING_BAR_HEIGHT - height;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xPosition, yPosition, FERMENTING_BAR_U, FERMENTING_BAR_V + FERMENTING_BAR_HEIGHT - height, FERMENTING_BAR_WIDTH, height, 256, 256);
        }
    }

}
