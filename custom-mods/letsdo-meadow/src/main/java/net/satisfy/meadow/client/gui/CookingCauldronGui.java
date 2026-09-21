package net.satisfy.meadow.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.satisfy.meadow.Meadow;
import net.satisfy.meadow.client.gui.handler.CookingCauldronGuiHandler;
import org.joml.Vector2i;

public class CookingCauldronGui extends AbstractContainerScreen<CookingCauldronGuiHandler> {
    private static final Identifier BACKGROUND = Meadow.identifier("textures/gui/cooking_cauldron_gui.png");
    private static final int ARROW_X = 70;
    private static final int ARROW_Y = 27;
    private static final int FLUID_X = 157;
    private static final int FLUID_Y = 23;
    private static final int FLUID_WIDTH = 8;
    private static final int FLUID_HEIGHT = 43;
    private final Vector2i screenPos = new Vector2i();

    public CookingCauldronGui(CookingCauldronGuiHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        screenPos.set(leftPos, topPos);
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, screenPos.x(), screenPos.y(), 0, 0, imageWidth, imageHeight, 256, 256);
        renderProgressArrow(guiGraphics);
        renderBurnIcon(guiGraphics);
        renderFluidBar(guiGraphics);
    }

    private void renderProgressArrow(GuiGraphicsExtractor guiGraphics) {
        int progress = this.menu.getScaledProgress(24);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, screenPos.x() + ARROW_X, screenPos.y() + ARROW_Y, 176, 14, progress, 17, 256, 256);
    }

    private void renderBurnIcon(GuiGraphicsExtractor guiGraphics) {
        if (menu.isBeingBurned()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, screenPos.x() + 108, screenPos.y() + 52, 176, 0, 14, 14, 256, 256);
        }
    }

    private void renderFluidBar(GuiGraphicsExtractor guiGraphics) {
        int fluidLevel = menu.getFluidLevel();
        int filledHeight = (fluidLevel * FLUID_HEIGHT) / 100;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, screenPos.x() + FLUID_X, screenPos.y() + FLUID_Y + FLUID_HEIGHT - filledHeight, 176, 31 + FLUID_HEIGHT - filledHeight, FLUID_WIDTH, filledHeight, 256, 256);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        if (isMouseOverFluidArea(mouseX, mouseY)) {
            int fluidLevel = this.menu.getFluidLevel();
            Component tooltip = Component.translatable("tooltip.meadow.cooking_cauldron.water_level", fluidLevel);
            guiGraphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
        }
        if (isMouseOverProgressArrow(mouseX, mouseY)) {
            int remainingTicks = this.menu.getRequiredDuration() - this.menu.getCookingTime();
            String formattedTime = formatTicks(remainingTicks);
            Component tooltip = Component.translatable("tooltip.meadow.cooking_cauldron.remaining_time", formattedTime);
            guiGraphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
        }
    }

    private boolean isMouseOverFluidArea(int mouseX, int mouseY) {
        int left = screenPos.x() + FLUID_X;
        int top = screenPos.y() + FLUID_Y;
        return mouseX >= left && mouseX < left + FLUID_WIDTH && mouseY >= top && mouseY < top + FLUID_HEIGHT;
    }

    private boolean isMouseOverProgressArrow(int mouseX, int mouseY) {
        int left = screenPos.x() + ARROW_X;
        int top = screenPos.y() + ARROW_Y;
        return mouseX >= left && mouseX < left + 24 && mouseY >= top && mouseY < top + 17;
    }

    private String formatTicks(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
