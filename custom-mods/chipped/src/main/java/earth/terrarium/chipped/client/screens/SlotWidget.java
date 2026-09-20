package earth.terrarium.chipped.client.screens;

import earth.terrarium.chipped.Chipped;
import earth.terrarium.chipped.common.menus.WorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class SlotWidget extends AbstractWidget {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Chipped.MOD_ID, "textures/gui/sprites/slot.png");
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");

    private final ItemStack stack;
    private final java.util.function.Consumer<ItemStack> selection;
    private final int minY;
    private final int maxY;

    public SlotWidget(ItemStack stack, WorkbenchMenu menu, int minY, int maxY) {
        this(stack, menu::setChosenStack, minY, maxY);
    }

    public SlotWidget(ItemStack stack, java.util.function.Consumer<ItemStack> selection, int minY, int maxY) {
        super(0, 0, WorkbenchGrid.CELL_SIZE, WorkbenchGrid.CELL_SIZE, CommonComponents.EMPTY);
        this.stack = stack;
        this.selection = selection;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), 0, 0, 18, 18, 18, 18);

        boolean isHighlighted = isMouseOver(mouseX, mouseY);
        if (isHighlighted) {
            graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, getX() + 1, getY() + 1, 24, 24);
        }
        graphics.item(stack, getX() + 1, getY() + 1);
        if (isHighlighted) {
            graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, getX() + 1, getY() + 1, 24, 24);
        }
    }

    public void renderTooltip(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
            if (!stack.isEmpty()) {
                graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) && WorkbenchGrid.containsY(mouseY, minY, maxY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (stack.isEmpty() || !isActive() || !isValidClickButton(event.buttonInfo()) || !isMouseOver(event.x(), event.y())) return false;
        playSelectionSound();
        onClick(event, doubleClick);
        return true;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        selection.accept(stack);
    }

    protected void playSelectionSound() {
        playDownSound(Minecraft.getInstance().getSoundManager());
    }
}
