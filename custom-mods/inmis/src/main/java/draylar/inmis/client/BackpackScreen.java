package draylar.inmis.client;

import draylar.inmis.Inmis;
import draylar.inmis.menu.BackpackMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * The backpack GUI: upstream's 9-patch container texture (8x8 corners,
 * stretched edges, v offset 66) plus the 18x18 slot overlay drawn over every
 * slot, in the upstream title colour.
 */
@Environment(EnvType.CLIENT)
public class BackpackScreen extends AbstractContainerScreen<BackpackMenu> {

    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(Inmis.MOD_ID, "textures/gui/backpack_container.png");
    private static final Identifier SLOT_TEXTURE = Identifier.fromNamespaceAndPath(Inmis.MOD_ID, "textures/gui/backpack_slot.png");
    private static final int TEXTURE_V = 66;

    private final int titleColor;

    public BackpackScreen(BackpackMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, menu.getBackpackStack().getHoverName(),
                menu.getImageWidth(), menu.getImageHeight());
        this.titleColor = decodeTitleColor();
        this.titleLabelY = 7;
        this.inventoryLabelX = menu.getPlayerInvSlotX();
        this.inventoryLabelY = menu.getImageHeight() - 94;
    }

    private static int decodeTitleColor() {
        try {
            return Integer.decode(Inmis.CONFIG.guiTitleColor) | 0xFF000000;
        } catch (NumberFormatException ignored) {
            return 0xFF404040;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;
        int width = this.imageWidth;
        int height = this.imageHeight;
        int v = TEXTURE_V;

        // Corners
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 106, 124 + v, 8, 8, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + width - 8, y, 248, 124 + v, 8, 8, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y + height - 8, 106, 182 + v, 8, 8, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + width - 8, y + height - 8, 248, 182 + v, 8, 8, 256, 256);

        // Stretched edges
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 8, y, 114, 124 + v, width - 16, 8, 134, 8, 256, 256, -1);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 8, y + height - 8, 114, 182 + v, width - 16, 8, 134, 8, 256, 256, -1);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y + 8, 106, 132 + v, 8, height - 16, 8, 50, 256, 256, -1);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + width - 8, y + 8, 248, 132 + v, 8, height - 16, 8, 50, 256, 256, -1);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 8, y + 8, 114, 132 + v, width - 16, height - 16, 134, 50, 256, 256, -1);
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        super.extractSlot(graphics, slot, mouseX, mouseY);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, slot.x - 1, slot.y - 1, 0, 0, 18, 18, 18, 18);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, this.titleColor, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, this.titleColor, false);
    }
}
