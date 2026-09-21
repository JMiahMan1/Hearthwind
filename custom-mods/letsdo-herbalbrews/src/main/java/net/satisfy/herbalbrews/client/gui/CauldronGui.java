package net.satisfy.herbalbrews.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.satisfy.herbalbrews.client.gui.handler.CauldronGuiHandler;
import net.satisfy.herbalbrews.core.registry.ObjectRegistry;
import net.satisfy.herbalbrews.core.util.HerbalBrewsIdentifier;
import org.joml.Vector2i;

@Environment(EnvType.CLIENT)
public class CauldronGui extends AbstractContainerScreen<CauldronGuiHandler> {
    private static final Identifier BACKGROUND = HerbalBrewsIdentifier.identifier("textures/gui/cauldron.png");
    private static final int PROGRESS_X = 114;
    private static final int PROGRESS_Y = 38;
    private static final int PROGRESS_WIDTH = 11;
    private static final int PROGRESS_HEIGHT = 29;
    private static final int PROGRESS_TEX_X = 176;
    private static final int PROGRESS_TEX_Y = 0;

    private static final int CATALYST_X = 110;
    private static final int CATALYST_Y = 69;
    private static final int CATALYST_TEX_X = 176;
    private static final int CATALYST_TEX_Y = 29;
    private static final int CATALYST_WIDTH = 18;
    private static final int CATALYST_HEIGHT = 29;

    private final Vector2i screenPos = new Vector2i();

    public CauldronGui(CauldronGuiHandler handler, net.minecraft.world.entity.player.Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        screenPos.set(leftPos, topPos);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, screenPos.x(), screenPos.y(), 0, 0, imageWidth, imageHeight, 256, 256);

        int progress = this.menu.getScaledProgress(PROGRESS_HEIGHT);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, screenPos.x() + PROGRESS_X, screenPos.y() + PROGRESS_Y + (PROGRESS_HEIGHT - progress), PROGRESS_TEX_X, PROGRESS_TEX_Y + (PROGRESS_HEIGHT - progress), PROGRESS_WIDTH, progress, 256, 256);

        ItemStack catalyst = this.menu.getSlot(4).getItem();
        if (!catalyst.isEmpty() && catalyst.getItem() == ObjectRegistry.HERBAL_INFUSION.get()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, screenPos.x() + CATALYST_X, screenPos.y() + CATALYST_Y, CATALYST_TEX_X, CATALYST_TEX_Y, CATALYST_WIDTH, CATALYST_HEIGHT, 256, 256);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        if (isMouseOverProgress(mouseX, mouseY)) {
            int remainingTicks = this.menu.getRequiredDuration() - this.menu.getCookingTime();
            String formattedTime = formatTicks(remainingTicks);
            Component tooltip = Component.translatable("tooltip.herbalbrews.cauldron.remaining_time", formattedTime);
            guiGraphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
        }
        if (isMouseOverInputSlot(mouseX, mouseY)) {
            Component tooltip = Component.translatable("tooltip.herbalbrews.cauldron.input_slot");
            guiGraphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
        }
    }

    private boolean isMouseOverProgress(int mouseX, int mouseY) {
        int left = screenPos.x() + PROGRESS_X;
        int top = screenPos.y() + PROGRESS_Y;
        return mouseX >= left && mouseX < left + PROGRESS_WIDTH && mouseY >= top && mouseY < top + PROGRESS_HEIGHT;
    }


    private boolean isMouseOverInputSlot(int mouseX, int mouseY) {
        int left = screenPos.x() + 132;
        int top = screenPos.y() + 53;
        return mouseX >= left && mouseX < left + 18 && mouseY >= top && mouseY < top + 18;
    }

    private String formatTicks(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
