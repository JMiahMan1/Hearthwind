package net.satisfy.vinery.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.satisfy.vinery.client.gui.handler.FermentationBarrelGuiHandler;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.platform.PlatformHelper;

@Environment(EnvType.CLIENT)
public class FermentationBarrelGui extends AbstractContainerScreen<FermentationBarrelGuiHandler> {
    public static final Identifier BACKGROUND = Vinery.identifier("textures/gui/fermentation_barrel_gui.png");

    private static final int FLUID_WIDTH = 20;
    private static final int FLUID_X = 82;
    private static final int FLUID_Y = 44;

    private static final int CRAFT_PROGRESS_TEXTURE_X = 176;
    private static final int CRAFT_PROGRESS_TEXTURE_Y = 0;
    private static final int CRAFT_PROGRESS_WIDTH = 11;
    private static final int CRAFT_PROGRESS_HEIGHT = 29;
    private static final int CRAFT_PROGRESS_GUI_X = 122;
    private static final int CRAFT_PROGRESS_GUI_Y = 20;
    private static final int CRAFT_PROGRESS_GUI_HEIGHT = 29;

    public FermentationBarrelGui(FermentationBarrelGuiHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        if (isMouseOverFluidArea(mouseX, mouseY)) {
            guiGraphics.setTooltipForNextFrame(getFluidTooltip(), mouseX, mouseY);
        }
        if (isMouseOverCraftingTimeArea(mouseX, mouseY)) {
            guiGraphics.setTooltipForNextFrame(getCraftingTimeTooltip(), mouseX, mouseY);
        }
    }

    private Component getFluidTooltip() {
        String juiceType = this.menu.getJuiceType();
        int fluidLevel = this.menu.getFluidLevel();
        int maxFluidLevel = PlatformHelper.getMaxFluidLevel();

        double percentage = (double) fluidLevel / maxFluidLevel * 100;
        String percentageStr = String.format("%.2f", percentage);

        if (juiceType.startsWith("red")) {
            String region = juiceType.substring(4);
            return Component.translatable(
                    "tooltip.vinery.fermentation_barrel.red_" + region + "_juice_with_percentage",
                    percentageStr
            );
        } else if (juiceType.startsWith("white")) {
            String region = juiceType.substring(6);
            return Component.translatable(
                    "tooltip.vinery.fermentation_barrel.white_" + region + "_juice_with_percentage",
                    percentageStr
            );
        } else if (juiceType.equals("apple")) {
            return Component.translatable(
                    "tooltip.vinery.fermentation_barrel.apple_juice_with_percentage",
                    percentageStr
            );
        } else {
            return Component.translatable("tooltip.vinery.fermentation_barrel.empty");
        }
    }

    private Component getCraftingTimeTooltip() {
        int totalTicks = this.menu.data.get(1);
        int currentTicks = this.menu.data.get(0);
        int remainingTicks = totalTicks - currentTicks;

        if (remainingTicks > 0) {
            int seconds = remainingTicks / 20;
            int minutes = seconds / 60;
            seconds %= 60;

            String formattedTime = String.format("%d:%02d Seconds", minutes, seconds);
            return Component.translatable("tooltip.vinery.fermentation_barrel.crafting_time", formattedTime);
        } else {
            return Component.translatable("tooltip.vinery.fermentation_barrel.crafting_time", "0:00 Seconds");
        }
    }

    private boolean isMouseOverFluidArea(int mouseX, int mouseY) {
        int fluidAreaLeft = this.leftPos + FLUID_X - 1;
        int fluidAreaTop = this.topPos + FLUID_Y - 5;
        int fluidAreaRight = this.leftPos + FLUID_X + FLUID_WIDTH + 1;
        int fluidAreaBottom = this.topPos + FLUID_Y + 10;

        return mouseX >= fluidAreaLeft && mouseX <= fluidAreaRight &&
                mouseY >= fluidAreaTop && mouseY <= fluidAreaBottom;
    }

    private boolean isMouseOverCraftingTimeArea(int mouseX, int mouseY) {
        int totalTicks = this.menu.data.get(1);
        int currentTicks = this.menu.data.get(0);

        if (totalTicks <= 0 || currentTicks >= totalTicks) {
            return false;
        }

        int craftingTimeAreaLeft = this.leftPos + CRAFT_PROGRESS_GUI_X;
        int craftingTimeAreaTop = this.topPos + CRAFT_PROGRESS_GUI_Y;
        int craftingTimeAreaRight = this.leftPos + CRAFT_PROGRESS_GUI_X + CRAFT_PROGRESS_WIDTH;
        int craftingTimeAreaBottom = this.topPos + CRAFT_PROGRESS_GUI_Y + CRAFT_PROGRESS_GUI_HEIGHT;

        return mouseX >= craftingTimeAreaLeft && mouseX <= craftingTimeAreaRight &&
                mouseY >= craftingTimeAreaTop && mouseY <= craftingTimeAreaBottom;
    }

    public static void drawJuiceBar(GuiGraphicsExtractor guiGraphics, String juiceType, int juiceAmount, int originX, int originY) {

        final int MAX_FLUID = PlatformHelper.getMaxFluidLevel();
        int scaledWidth = (int) ((double) juiceAmount / MAX_FLUID * FLUID_WIDTH);
        scaledWidth = Math.max(0, Math.min(FLUID_WIDTH, scaledWidth));

        final int TEXTURE_X_START = 176;

        int TEXTURE__START;
        if (juiceType.startsWith("red")) {
            TEXTURE__START = 29;
        }
        else if (juiceType.startsWith("white")) {
            TEXTURE__START = 33;
        }
        else if (juiceType.equals("apple")) {
            TEXTURE__START = 37;
        }
        else {
            TEXTURE__START = 0;
        }

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, originX, originY, TEXTURE_X_START, TEXTURE__START, scaledWidth, 4, 256, 256);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        FermentationBarrelGui.drawJuiceBar(guiGraphics, this.menu.getJuiceType(), this.menu.getFluidLevel(), x + FLUID_X, y + FLUID_Y);

        this.renderCraftingProgress(guiGraphics, x, y);
    }

    protected void renderCraftingProgress(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop) {
        int filledHeight = this.menu.getScaledProgress(CRAFT_PROGRESS_HEIGHT);

        int drawY = guiTop + CRAFT_PROGRESS_GUI_Y + (CRAFT_PROGRESS_HEIGHT - filledHeight);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, guiLeft + CRAFT_PROGRESS_GUI_X, drawY, CRAFT_PROGRESS_TEXTURE_X, CRAFT_PROGRESS_TEXTURE_Y + (CRAFT_PROGRESS_HEIGHT - filledHeight), CRAFT_PROGRESS_WIDTH, filledHeight, 256, 256);
    }
}
