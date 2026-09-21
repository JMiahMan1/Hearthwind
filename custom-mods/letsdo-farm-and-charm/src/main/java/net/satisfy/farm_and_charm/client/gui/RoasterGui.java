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
import net.satisfy.farm_and_charm.client.gui.handler.RoasterGuiHandler;

@Environment(EnvType.CLIENT)
public class RoasterGui extends AbstractContainerScreen<RoasterGuiHandler> {
    public static final Identifier BACKGROUND;

    public static final int ARROW_X = 95;
    public static final int ARROW_Y = 14;

    static {
        BACKGROUND = FarmAndCharm.identifier("textures/gui/roaster_gui.png");
    }

    public RoasterGui(RoasterGuiHandler handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        int posX = this.leftPos;
        int posY = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RoasterGui.BACKGROUND, posX, posY, 0, 0, this.imageWidth - 1, this.imageHeight, 256, 256);
        this.renderProgressArrow(guiGraphics);
        this.renderBurnIcon(guiGraphics, posX, posY);
    }


    public void renderProgressArrow(GuiGraphicsExtractor guiGraphics) {
        int progress = this.menu.getScaledProgress(23);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos + 95, this.topPos + 14, 178, 15, progress, 30, 256, 256);
    }

    public void renderBurnIcon(GuiGraphicsExtractor guiGraphics, int posX, int posY) {
        if (this.menu.isBeingBurned()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, posX + 124, posY + 56, 176, 0, 17, 15, 256, 256);
        }
    }
}
