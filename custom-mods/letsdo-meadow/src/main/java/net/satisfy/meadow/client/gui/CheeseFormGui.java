package net.satisfy.meadow.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.satisfy.meadow.Meadow;
import net.satisfy.meadow.client.gui.handler.CheeseFormGuiHandler;
import org.joml.Vector2i;

public class CheeseFormGui extends AbstractContainerScreen<CheeseFormGuiHandler> {
    public static final Identifier BACKGROUND;

    public static final int ARROW_X = 78;
    public static final int ARROW_Y = 36;
    public static final int TIME_X = 81;
    public static final int TIME_Y = 8;
    private final Vector2i screenPos = new Vector2i();

    public CheeseFormGui(CheeseFormGuiHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int posX = this.leftPos;
        int posY = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CheeseFormGui.BACKGROUND, posX, posY, 0, 0, this.imageWidth - 1, this.imageHeight, 256, 256);
        this.renderProgressArrow(guiGraphics);
    }

    protected void renderProgressArrow(GuiGraphicsExtractor guiGraphics) {
        final int progressX = this.menu.getScaledXProgress();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos + ARROW_X, topPos + ARROW_Y, 176, 4, progressX, 10, 256, 256);
        final int progressY = this.menu.getScaledYProgress();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos + TIME_X, topPos + TIME_Y, 180, 22, 16, progressY, 256, 256);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        screenPos.set(leftPos, topPos);

        if (isMouseOverProgressArrow(mouseX, mouseY)) {
            int remainingTicks = Math.max(this.menu.getRequiredDuration() - this.menu.getCookingTime(), 0);
            String formattedTime = formatTicks(remainingTicks);
            Component tooltip = Component.translatable("tooltip.meadow.cooking_cauldron.remaining_time", formattedTime);
            guiGraphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
        }
    }

    private boolean isMouseOverProgressArrow(int mouseX, int mouseY) {
        int left = screenPos.x() + ARROW_X;
        int top = screenPos.y() + ARROW_Y;
        return mouseX >= left && mouseX < left + 24 && mouseY >= top && mouseY < top + 17;
    }

    private String formatTicks(int ticks) {
        int seconds = Math.max(ticks / 20, 0);
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    static {
        BACKGROUND = Meadow.identifier("textures/gui/cheese_form_gui.png");
    }
}
