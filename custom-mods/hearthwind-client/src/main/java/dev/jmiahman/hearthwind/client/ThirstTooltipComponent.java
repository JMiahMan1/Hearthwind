package dev.jmiahman.hearthwind.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import dev.jmiahman.hearthwind.survival.hydration.ThirstPreview;

/**
 * Draws Dehydration's thirst droplet row. A port of
 * {@code net.dehydration.misc.ThirstTooltipComponent}, whose own blit loops
 * are reproduced cell for cell:
 *
 * <pre>
 *   for (int j = 0; j &lt; quench / 2; j++) {
 *       draw(x + j*9 - 1, y, 0, 0, 9, 9, 256, 256);          // the empty cell
 *       draw(x + j*9 - 1, y, quality*18, 9, 9, 9, 256, 256); // the full droplet
 *   }
 *   if (quench % 2 != 0) {
 *       draw(x + (quench/2)*9 - 1, y, 0, 0, 9, 9, 256, 256);
 *       draw(x + (quench/2)*9 - 1, y, quality*18 + 9, 9, 9, 9, 256, 256);
 *   }
 * </pre>
 *
 * <p>Note the {@code - 1}: the reference draws one pixel left of the tooltip
 * so the droplets overhang the cell the tooltip laid out for them. The row is
 * always 11 tall and the icon row is always {@code v = 9} - the reference
 * never uses the {@code v = 18} copy of the same four qualities.
 */
public final class ThirstTooltipComponent implements ClientTooltipComponent {
    private final ThirstPreview preview;

    public ThirstTooltipComponent(ThirstPreview preview) {
        this.preview = preview;
    }

    @Override
    public int getHeight(Font font) {
        return ThirstPreview.HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        return ThirstPreview.widthFor(this.preview.quench());
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height,
            GuiGraphicsExtractor graphics) {
        int quench = this.preview.quench();
        int quality = this.preview.quality();
        for (int j = 0; j < quench / 2; j++) {
            cell(graphics, x + j * ThirstPreview.CELL - 1, y, 0, 0);
            cell(graphics, x + j * ThirstPreview.CELL - 1, y, ThirstPreview.fullU(quality),
                    ThirstPreview.ICON_V);
        }
        if (quench % 2 != 0) {
            int hx = x + (quench / 2) * ThirstPreview.CELL - 1;
            cell(graphics, hx, y, 0, 0);
            cell(graphics, hx, y, ThirstPreview.halfU(quality), ThirstPreview.ICON_V);
        }
    }

    /** One 9x9 cell of the droplet sheet. */
    private static void cell(GuiGraphicsExtractor graphics, int x, int y, int u, int v) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ThirstPreview.ICON, x, y, u, v,
                ThirstPreview.CELL, ThirstPreview.CELL, ThirstPreview.ICON_SIZE,
                ThirstPreview.ICON_SIZE);
    }
}